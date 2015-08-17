package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
import android.content.DialogInterface.OnClickListener;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog.Builder;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zezo.zezomusicplayer.FolderSelector.MusicFolderUpdatedListener;
import com.zezo.zezomusicplayer.MusicService.MusicBinder;
import com.zezo.zezomusicplayer.SearchFragment.SearchListener;
import com.zezo.zezomusicplayer.YesNoDialogFragment.OnDeleteConfirmedListener;

public class MainActivity extends AppCompatActivity implements SearchListener, OnDeleteConfirmedListener {

	public static final String PACKAGE_NAME = "com.zezo.zezomusicplayer";
	public static final String KEY_DIRECTORY_SELECTED = PACKAGE_NAME + ".DIRECTORY_SELECTED";
	private SharedPreferences sharedPreferences;
	private FrameLayout controllerFrame;
	private TextView currentArtistView;
	private TextView currentTitleView;
	private Menu menu;
	private MusicController musicController;
	private MusicService musicService;
	private ServiceConnection musicServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			MusicBinder binder = (MusicBinder) service;
			musicService = binder.getService();
			musicService.setSongLibrary(songLibrary);
			musicController.init(musicService);
			musicController.setMusicBound(true);
			showController();

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

			ArrayList<Song> songs = songAdapter.getFilteredSongs();
			songAdapter.setItemChecked(song.getId());
			songListView.setItemChecked(songs.indexOf(song), true);
			currentArtistView.setText(song.getArtist());
			currentTitleView.setText(song.getTitle());
			musicController.show(0);
			musicService.setPlayerPrepared(true);
			processingPick = false;
		}
	};
	private boolean processingPick = false;
	private SearchFragment searchFragment;

	private SongAdapter songAdapter;

	private ArrayList<Song> songLibrary;
	private ListView songListView;

	@Override
	public void onBackPressed() {

		// super.onBackPressed();
		//
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

		songAdapter.getSongs().remove(song);
		songAdapter.getFilteredSongs().remove(song);
		songAdapter.notifyDataSetChanged();

		Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());

		getContentResolver().delete(uri, null, null);

		musicService.removeFromPlaylist(song);

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
			folderSelector.showFileListDialog(musicFolder, MainActivity.this);

			folderSelector.setDialogResult(new MusicFolderUpdatedListener() {
				public void onMusicFolderUpdated(String musicFolderPath) {

					Toast.makeText(MainActivity.this, "Selected music folder:" + musicFolderPath, Toast.LENGTH_SHORT)
							.show();

					loadSongsIntoPlaylist(getAllSongsInFolder(musicFolderPath));
					songListView.setAdapter(songAdapter);
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

	@Override
	public void onSearchTextChanged(CharSequence cs) {

		songAdapter.getFilter().filter(cs);

	}

	public void onSongClicked(View view) {

		if (processingPick)
			return;

		processingPick = true;

		Song song = songAdapter.getItem(Integer.parseInt(((View) view.getParent()).getTag().toString()));

		if (song != null && musicService.audioFocusGranted()) {
			musicService.playSong(song);
			Toast.makeText(this, "Playing.", Toast.LENGTH_SHORT).show();
		}

	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public void scrollToCurrent(View view) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			songListView.setSelection(songListView.getCheckedItemPosition());

	}

	private void exit() {

		hideKeyboard();
		unbindService(musicServiceConnection);
		stopService(musicServiceIntent);
		musicService = null;
		System.exit(0);

	}

	private ArrayList<Song> getAllSongsOnDevice() {

		ArrayList<Song> songs = new ArrayList<Song>();

		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

		if (musicCursor != null && musicCursor.moveToFirst()) {

			int titleColumn = musicCursor.getColumnIndex(MediaColumns.TITLE);
			int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
			int artistColumn = musicCursor.getColumnIndex(AudioColumns.ARTIST);
			int durationColumn = musicCursor.getColumnIndex(AudioColumns.DURATION);
			int dataColumn = musicCursor.getColumnIndex(AudioColumns.DATA);

			do {

				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String duration = Util.getTimeStringFromMs(musicCursor.getInt(durationColumn));
				String data = musicCursor.getString(dataColumn);
				songs.add(new Song(thisId, thisTitle, thisArtist, duration));

			} while (musicCursor.moveToNext());

		}

		musicCursor.close();

		return songs;

	}

	private ArrayList<Song> getAllSongsInFolder(String folderPath) {

		ArrayList<Song> songs = new ArrayList<Song>();

		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		Cursor musicCursor = musicResolver.query(musicUri, null,
				android.provider.MediaStore.Audio.Media.DATA + " like ? ", new String[] { "%" + folderPath + "%" },
				null);

		if (musicCursor != null && musicCursor.moveToFirst()) {

			int titleColumn = musicCursor.getColumnIndex(MediaColumns.TITLE);
			int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
			int artistColumn = musicCursor.getColumnIndex(AudioColumns.ARTIST);
			int durationColumn = musicCursor.getColumnIndex(AudioColumns.DURATION);

			do {

				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String duration = Util.getTimeStringFromMs(musicCursor.getInt(durationColumn));
				songs.add(new Song(thisId, thisTitle, thisArtist, duration));

			} while (musicCursor.moveToNext());

		}

		musicCursor.close();

		// returns empty list
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

			// musicServiceIntent = new Intent();
			// musicServiceIntent.setComponent(new ComponentName(
			// "com.zezo.zezomusicplayer",
			// "comcom.zezo.zezomusicplayer.MusicService"));

			musicServiceIntent = new Intent(this, MusicService.class);

			bindService(musicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);

			startService(musicServiceIntent);

		}

	}

	private void loadSongsIntoPlaylist(ArrayList<Song> songs) {

		songLibrary = songs;

		Collections.sort(songLibrary, new Comparator<Song>() {
			@Override
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		songAdapter = new SongAdapter(this, songLibrary);

	}

	private void initViews() {

		setContentView(R.layout.activity_main);
		songListView = (ListView) findViewById(R.id.song_list);
		currentTitleView = (TextView) findViewById(R.id.currentTitle);
		currentArtistView = (TextView) findViewById(R.id.currentArtist);
		controllerFrame = (FrameLayout) findViewById(R.id.controllerFrame);

		songListView.setAdapter(songAdapter);
		registerForContextMenu(songListView);

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

		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really wish to end the application?")
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		sharedPreferences = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);

		SpannableString s = new SpannableString("Zezo -");

		s.setSpan(new TypefaceSpan(this, "electrical.ttf"), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		s.setSpan(new RelativeSizeSpan(0.6f), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			// Update the action bar title with the TypefaceSpan instance
			ActionBar actionBar = getSupportActionBar();
			actionBar.setTitle(s);

		}

		String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
			    Environment.getExternalStorageDirectory().toString());
		loadSongsIntoPlaylist(getAllSongsInFolder(musicFolder));
		initViews();

		musicController = new MusicController(this);
		musicController.setAnchorView(controllerFrame);

		initMusicService();

		if (findViewById(R.id.fragmentContainer) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create a new Fragment to be placed in the activity layout
			searchFragment = new SearchFragment();

			// In case this activity was started with special instructions from
			// an
			// Intent, pass the Intent's extras to the fragment as arguments
			searchFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, searchFragment, "searchFragment")
					.commit();

			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().hide(searchFragment).commit();
		}

		initKeyboard();

	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_MENU) {
	// hideController();
	// return false;
	// }
	// return super.onKeyUp(keyCode, event);
	// }

	// private OnItemClickListener onSongClickListener = new
	// OnItemClickListener() {
	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// if (processingPick)
	// return;
	//
	// processingPick = true;
	//
	// // Song song = songAdapter.getItem(Integer.parseInt(((View) view
	// // .getParent()).getTag().toString()));
	//
	// Song song = songAdapter.getItem(Integer.parseInt(view.getTag()
	// .toString()));
	//
	// if (song != null && musicService.audioFocusGranted())
	// musicService.playSong(song);
	// }
	// };

	// !! RESUME TO SEARCH SCREEN BUG FIX !? !!

	// @Override
	// protected void onResume() {
	//
	// findViewById(R.id.song_list).postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// searchBox.requestFocus();
	// InputMethodManager inputMethodManager = (InputMethodManager)
	// getSystemService(INPUT_METHOD_SERVICE);
	// inputMethodManager.showSoftInput(searchBox,
	// InputMethodManager.SHOW_IMPLICIT);
	//
	// hideKeyboard();
	// }
	// }, 100);
	//
	// super.onResume();
	//
	// }

	@Override
	protected void onDestroy() {

		// stopService(playIntent);
		// musicService = null;
		// hideKeyboard();
		// unregisterReceiver(onPrepareReceiver);

		unbindService(musicServiceConnection);
		super.onDestroy();

	}

	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * if (keyCode == KeyEvent.KEYCODE_BACK) { hideKeyboard();
	 * super.onKeyDown(keyCode, event); } return true;
	 * 
	 * }
	 */

}