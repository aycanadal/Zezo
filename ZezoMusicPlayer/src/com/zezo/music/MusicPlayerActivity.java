package com.zezo.music;

import java.util.ArrayList;

import com.zezo.music.MusicService.MusicBinder;
import com.zezo.music.SearchFragment.SearchListener;
import com.zezo.music.domain.Song;
import com.zezo.music.playlist.Playlist;
import com.zezo.music.util.FolderSelector;
import com.zezo.music.util.FolderSelector.MusicFolderUpdatedListener;
import com.zezo.music.util.TypefaceSpan;
import com.zezo.music.util.Util;
import com.zezo.music.util.YesNoDialogFragment;
import com.zezo.music.util.YesNoDialogFragment.OnDeleteConfirmedListener;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MusicPlayerActivity extends AppCompatActivity implements OnDeleteConfirmedListener, SearchListener {

	public static final String PACKAGE_NAME = "com.zezo.music";
	public static final String KEY_DIRECTORY_SELECTED = PACKAGE_NAME + ".DIRECTORY_SELECTED";

	private TabPagerAdapter tabPagerAdapter;
	private ViewPager viewPager;
	private SharedPreferences sharedPreferences;
	private TextView currentArtistView;
	private TextView currentTitleView;
	private Menu menu;
	private FrameLayout controllerFrame;
	private MusicController musicController;
	private MusicService musicService;
	private ServiceConnection musicServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			MusicBinder binder = (MusicBinder) service;
			musicService = binder.getService();
			musicService.setPlaylist(playlist);
			musicController.init(musicService);
			musicController.setMusicBound(true);

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			musicController.setMusicBound(false);

		}
	};
	private Intent musicServiceIntent;
	private BroadcastReceiver onMediaPlayerPlayingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent i) {

			if (i.getAction() != "MEDIA_PLAYER_PLAYING")
				return;

			Song song = musicService.getCurrentSong();
			currentArtistView.setText(song.getArtist());
			currentTitleView.setText(song.getTitle());

			if (viewPager.getCurrentItem() == 1) {

				Playlist playlistFragment = (Playlist) getSupportFragmentManager()
						.findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
				playlistFragment.setCurrentSong(song);

			}

			musicController.show(0);
			musicService.setPlayerPrepared(true);

		}
	};

	private final SearchFragment searchFragment = new SearchFragment();;
	private ArrayList<Song> playlist;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SpannableString s = new SpannableString("Zezo");
		s.setSpan(new TypefaceSpan(this, "electrical.ttf"), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		s.setSpan(new RelativeSizeSpan(0.6f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(s);

		sharedPreferences = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
		String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
				Environment.getExternalStorageDirectory().toString());

		playlist = getAllSongsInFolder(musicFolder);

		tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), this);
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(tabPagerAdapter);
		viewPager.setCurrentItem(1);

		initMusicService();

		if (findViewById(R.id.searchContainer) != null) {

			if (savedInstanceState != null) {
				return;
			}

			searchFragment.setArguments(getIntent().getExtras());

			getSupportFragmentManager().beginTransaction().add(R.id.searchContainer, searchFragment, "searchFragment")
					.commit();

			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().hide(searchFragment).commit();
		}

		controllerFrame = (FrameLayout) findViewById(R.id.controllerFrame);
		musicController = new MusicController(this);
		musicController.setAnchorView(controllerFrame);

		initKeyboard();

		tabPagerAdapter.getPlaylistFragment().setPlaylist(playlist);

		currentTitleView = (TextView) findViewById(R.id.currentTitle);
		currentArtistView = (TextView) findViewById(R.id.currentArtist);

	}

	private ArrayList<Song> getAllSongsInFolder(String folderPath) {

		ArrayList<Song> songs = new ArrayList<Song>();

		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		Cursor musicCursor = musicResolver.query(musicUri, null, MediaColumns.DATA + " like ? ",
				new String[] { "%" + folderPath + "%" }, MediaColumns.DATA + " ASC");

		if (musicCursor != null && musicCursor.moveToFirst()) {

			int titleColumn = musicCursor.getColumnIndex(MediaColumns.TITLE);
			int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
			int artistColumn = musicCursor.getColumnIndex(AudioColumns.ARTIST);
			int durationColumn = musicCursor.getColumnIndex(AudioColumns.DURATION);

			int dataColumn = musicCursor.getColumnIndex(MediaColumns.DATA);

			do {

				long id = musicCursor.getLong(idColumn);
				String title = musicCursor.getString(titleColumn);
				String artist = musicCursor.getString(artistColumn);
				String duration = Util.getTimeStringFromMs(musicCursor.getInt(durationColumn));
				String data = musicCursor.getString(dataColumn);

				songs.add(new Song(id, title, artist, duration, data));

			} while (musicCursor.moveToNext());

		}

		musicCursor.close();
		return songs;

	}

	private void hideController() {

		musicController.setVisibility(View.GONE);
		musicController.hideSuper();
		controllerFrame.setVisibility(View.GONE);

	}

	private void hideKeyboard() {

		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

	}

	private void hideSearch() {

		searchFragment.hide(getSupportFragmentManager(),
				(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE));

		showController();

	}

	private void initKeyboard() {

		// Show and hide keyboard once to work around the first time show
		// doesn't work bug.

		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.toggleSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0, 0);
		inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

	}

	private void initMusicService() {

		LocalBroadcastManager.getInstance(this).registerReceiver(onMediaPlayerPlayingReceiver,
				new IntentFilter("MEDIA_PLAYER_PLAYING"));

		if (musicServiceIntent == null) {

			musicServiceIntent = new Intent(this, MusicService.class);
			bindService(musicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
			startService(musicServiceIntent);

		}

	}

	@Override
	public void onBackPressed() {

		// super.onBackPressed();

		if (searchFragment.isOn())
			hideSearch();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context, menu);

		menu.add(R.string.AddToQueue).setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

				musicService.addToQueue(musicService.getSongById(info.id));

				return true;

			}
		});

		menu.add(R.string.Delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

				showDeleteDialog(musicService.getSongById(info.id));

				return true;

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		this.menu = menu;
		getMenuInflater().inflate(R.menu.main, menu);
		return true;

	}

	@Override
	public void onDeleteConfirmed(long songId) {

		Song song = musicService.getSongById(songId);

		if (song == musicService.getCurrentSong())
			return;

		if (viewPager.getCurrentItem() == 1) {

			tabPagerAdapter.getPlaylistFragment().remove(song);

		}

		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());

		getContentResolver().delete(uri, null, null);

		musicService.removeFromPlaylist(song);

	}

	@Override
	protected void onDestroy() {

		hideKeyboard();
		unbindService(musicServiceConnection);
		stopService(musicServiceIntent);
		musicService = null;

		super.onDestroy();

	}

	public void onOpenContextMenu(View view) {
		openContextMenu(view);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_set_folder:

			Toast.makeText(this, "Select music folder.", Toast.LENGTH_SHORT).show();
			FolderSelector folderSelector = new FolderSelector();
			String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
					Environment.getExternalStorageDirectory().toString());
			folderSelector.showFileListDialog(musicFolder, MusicPlayerActivity.this);

			folderSelector.setDialogResult(new MusicFolderUpdatedListener() {
				@Override
				public void onMusicFolderUpdated(String musicFolderPath) {

					Toast.makeText(MusicPlayerActivity.this, "Selected music folder:" + musicFolderPath,
							Toast.LENGTH_SHORT).show();

					playlist = getAllSongsInFolder(musicFolderPath);
					tabPagerAdapter.getPlaylistFragment().loadPlaylist(playlist);

					sharedPreferences.edit().putString(KEY_DIRECTORY_SELECTED, musicFolderPath).commit();

				}
			});

			break;

		case R.id.action_shuffle:

			toggleShuffle();
			break;

		case R.id.action_exit:

			showExitDialog();
			break;

		case R.id.action_search:

			if (searchFragment.isVisible()) {

				hideSearch();

			} else {

				showSearch();

			}

			break;

		}

		return super.onOptionsItemSelected(item);

	}

	public void play(Song song) {

		if (song != null && musicService.audioFocusGranted()) {
			musicService.playSong(song);
			Toast.makeText(this, "Playing.", Toast.LENGTH_SHORT).show();
		}

	}

	private void showController() {

		musicController.show(0);
		musicController.setVisibility(View.VISIBLE);
		controllerFrame.setVisibility(View.VISIBLE);

	}

	private void showDeleteDialog(Song song) {

		DialogFragment dialog = new YesNoDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", "Delete File?");
		args.putString("message", "Do you really wish to delete the song from the device?");
		args.putLong("songId", song.getId());
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "deleteDialog");

	}

	private void showExitDialog() {

		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really wish to quit the application?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						exit();
					}
				}).setNegativeButton(android.R.string.no, null).show();

	}

	private void showSearch() {

		hideController();

		searchFragment.show(getSupportFragmentManager(),
				(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE));
	}

	private void toggleShuffle() {

		Drawable shuffleIcon;
		MenuItem item = menu.findItem(R.id.action_shuffle);

		if (musicService.isShuffling()) {
			shuffleIcon = getResources().getDrawable(R.drawable.shufflegrey40);
			Toast.makeText(this, "Shuffle is now off.", Toast.LENGTH_SHORT).show();
		} else {
			shuffleIcon = getResources().getDrawable(R.drawable.shufflewhite40);
			Toast.makeText(this, "Shuffle is now on.", Toast.LENGTH_SHORT).show();
		}

		item.setIcon(shuffleIcon);
		musicService.toggleShuffle();
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public void scrollToCurrent(View view) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			tabPagerAdapter.getPlaylistFragment().scrollToCurrent();
		
	}

	private void exit() {

		hideKeyboard();
		unbindService(musicServiceConnection);
		stopService(musicServiceIntent);
		musicService = null;
		System.exit(0);

	}

	@Override
	public void onSearchTextChanged(CharSequence cs) {
		tabPagerAdapter.getPlaylistFragment().onSearchTextChanged(cs);
		
	}

}