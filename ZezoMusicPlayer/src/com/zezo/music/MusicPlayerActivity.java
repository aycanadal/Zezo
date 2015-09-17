package com.zezo.music;

import java.util.ArrayList;

import com.zezo.music.MusicService.MusicBinder;
import com.zezo.music.domain.Song;
import com.zezo.music.tabs.browser.Browser;
import com.zezo.music.tabs.playlist.Playlist;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.Toast;

public class MusicPlayerActivity extends AppCompatActivity implements OnDeleteConfirmedListener {

	public static final String PACKAGE_NAME = "com.zezo.music";
	public static final String KEY_DIRECTORY_SELECTED = MusicPlayerActivity.PACKAGE_NAME + ".DIRECTORY_SELECTED";

	private NowPlayingFragment nowPlayingFragment;
	private TabPagerAdapter tabPagerAdapter;
	private ViewPager viewPager;
	private SharedPreferences sharedPreferences;
	private MusicService musicService;
	private ServiceConnection musicServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			MusicBinder binder = (MusicBinder) service;
			musicService = binder.getService();
			musicService.setPlaylist(playlist);
			nowPlayingFragment.initController(musicService);

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			nowPlayingFragment.unbindController();

		}
	};
	private Intent musicServiceIntent;
	private BroadcastReceiver onMediaPlayerPlayingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent i) {

			if (i.getAction() != "MEDIA_PLAYER_PLAYING")
				return;

			Song song = musicService.getCurrentSong();

			nowPlayingFragment.setCurrentSong(song);

			if (viewPager.getCurrentItem() == 1) {

				Playlist playlistFragment = (Playlist) getSupportFragmentManager()
						.findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
				playlistFragment.setCurrentSong(song);

			}

			musicService.setPlayerPrepared(true);

		}
	};

	private ArrayList<Song> playlist;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sharedPreferences = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
		String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
				Environment.getExternalStorageDirectory().toString());

		playlist = getAllSongsInFolder(musicFolder);

		tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), this);
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(tabPagerAdapter);
		viewPager.setCurrentItem(1);

		initMusicService();

		initKeyboard();

		nowPlayingFragment = (NowPlayingFragment) getSupportFragmentManager().findFragmentById(R.id.nowplaying);
		nowPlayingFragment.setRetainInstance(true);
		tabPagerAdapter.getPlaylistFragment().setInitialPlaylist(playlist);

		ImageButton nowPlayingToggle = (ImageButton) findViewById(R.id.nowPlayingToggle);

		nowPlayingToggle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View nowPlayingToggle) {

				if (nowPlayingFragment.isVisible()) {

					((ImageButton) nowPlayingToggle).setImageResource(R.drawable.arrowsup);
					nowPlayingFragment.hide();

				} else {

					((ImageButton) nowPlayingToggle).setImageResource(R.drawable.arrowsdown);
					nowPlayingFragment.show();

				}

			}

		});

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

	private void hideKeyboard() {

		InputMethodManager inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		inputMethodManager.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);

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
			Browser browser = tabPagerAdapter.getBrowserFragment();
			String musicFolderPath = browser.getCurrentFolderPath();

			Toast.makeText(MusicPlayerActivity.this, "Selected music folder:" + musicFolderPath, Toast.LENGTH_SHORT)
					.show();

			playlist = getAllSongsInFolder(musicFolderPath);
			tabPagerAdapter.getPlaylistFragment().loadPlaylist(playlist);
			sharedPreferences.edit().putString(KEY_DIRECTORY_SELECTED, musicFolderPath).commit();
			break;

		case R.id.action_shuffle:
			toggleShuffle();
			break;

		case R.id.action_exit:
			showExitDialog();
			break;

		case R.id.action_search:
			return false;

		}

		return super.onOptionsItemSelected(item);

	}

	public void play(Song song) {

		if (song != null && musicService.audioFocusGranted()) {
			musicService.playSong(song);
			Toast.makeText(this, "Playing.", Toast.LENGTH_SHORT).show();
		}

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

	private void toggleShuffle() {

		tabPagerAdapter.getPlaylistFragment().setShuffle(musicService.isShuffling());
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

}