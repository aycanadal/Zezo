package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
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
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zezo.zezomusicplayer.MusicService.MusicBinder;

public class MainActivity extends Activity {

	private static final int DEFAULT_MUSIC_CONTROLLER_TIMEOUT = 3000;
	private TextView currentArtistView;
	private TextView currentTitleView;
	private MusicController musicController;

	private MusicService musicService;

	private ServiceConnection musicServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			MusicBinder binder = (MusicBinder) service;
			musicService = binder.getService();
			musicService.setSongs(songList);
			musicController.init(musicService);
			musicController.setMusicBound(true);
			musicController.show(0);
			// player.reset();

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			musicController.setMusicBound(false);

		}
	};

	private Intent musicServiceIntent;

	// Broadcast receiver to determine when music player has been prepared
	private BroadcastReceiver onMediaPlayerPlayingReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context c, Intent i) {

			if (i.getAction() != "MEDIA_PLAYER_PLAYING")
				return;

			Song song = musicService.getCurrentSong();
			ArrayList<Song> songs = songAdapter.getSongs();
			songListView.setItemChecked(songs.indexOf(song), true);
			currentArtistView.setText(song.getArtist());
			currentTitleView.setText(song.getTitle());
			musicController.show(0);
			// musicController.setFocusable(false);
			// musicController.setFocusableInTouchMode(false);
			// musicController.setClickable(false);
			// musicController.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			processingPick = false;

		}
	};

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

	public void onOpenContextMenu(View view) {
		openContextMenu(view);
	}

	public void onSongClicked(View view) {

		if (processingPick)
			return;

		processingPick = true;

		Song song = songAdapter.getItem(Integer.parseInt(((View) view
				.getParent()).getTag().toString()));

		// Song song = songAdapter.getItem(Integer.parseInt(view.getTag()
		// .toString()));

		if (song != null && musicService.audioFocusGranted())
			musicService.playSong(song);

	}

	private boolean processingPick = false;

	private EditText searchBox;

	private boolean searchEnabled;

	private LinearLayout searchPane;
	private SongAdapter songAdapter;
	private ArrayList<Song> songList;

	private SongListView songListView;

	private Song songToBeDeleted;

	private VoiceRecognitionHelper voiceRecognitionHelper;
	private FrameLayout controllerFrame;

	private void deleteSongToBeDeleted() {

		if (songToBeDeleted == musicService.getCurrentSong())
			return;

		songAdapter.getSongs().remove(songToBeDeleted);
		songAdapter.getFilteredSongs().remove(songToBeDeleted);
		songAdapter.notifyDataSetChanged();

		Uri uri = ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				songToBeDeleted.getId());

		getContentResolver().delete(uri, null, null);

		musicService.removeFromPlaylist(songToBeDeleted);

	}

	private void disableSearch() {

		searchEnabled = false;
		searchBox.setText("");
		searchPane.setVisibility(View.GONE);
		hideKeyboard();
		musicController.setVisibility(View.VISIBLE);

	}

	private void enableSearch() {

		searchEnabled = true;
		searchPane.setVisibility(View.VISIBLE);
		boolean focused = searchBox.requestFocus();
		musicController.setVisibility(View.GONE);
		showKeyboard();

	}

	private void exit() {

		hideKeyboard();
		stopService(musicServiceIntent);
		musicService = null;
		System.exit(0);

	}

	private ArrayList<Song> getAllSongsOnDevice() {

		ArrayList<Song> songs = new ArrayList<Song>();

		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null,
				null);

		if (musicCursor != null && musicCursor.moveToFirst()) {

			int titleColumn = musicCursor.getColumnIndex(MediaColumns.TITLE);
			int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
			int artistColumn = musicCursor.getColumnIndex(AudioColumns.ARTIST);

			do {

				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songs.add(new Song(thisId, thisTitle, thisArtist));

			} while (musicCursor.moveToNext());

		}

		musicCursor.close();

		return songs;

	}

	private void hideKeyboard() {

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

	}

	private void initMusicService() {

		LocalBroadcastManager.getInstance(this).registerReceiver(
				onMediaPlayerPlayingReceiver,
				new IntentFilter("MEDIA_PLAYER_PLAYING"));

		if (musicServiceIntent == null) {

			musicServiceIntent = new Intent(this, MusicService.class);
			bindService(musicServiceIntent, musicServiceConnection,
					Context.BIND_AUTO_CREATE);
			startService(musicServiceIntent);

		}

	}

	private void initSearch() {

		searchPane = (LinearLayout) findViewById(R.id.searchPane);
		searchBox = (EditText) findViewById(R.id.searchBox);
		searchPane.setVisibility(View.GONE);
		// searchBox.setVisibility(View.GONE);
		searchEnabled = false;
		voiceRecognitionHelper = new VoiceRecognitionHelper(searchBox);

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				MainActivity.this.songAdapter.getFilter().filter(cs);
			}
		});

	}

	private void initSongAdapter() {

		if (songList == null || songList.size() < 1)
			songList = getAllSongsOnDevice();

		Collections.sort(songList, new Comparator<Song>() {
			@Override
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		songAdapter = new SongAdapter(this, songList);

	}

	private void initViews() {

		setContentView(R.layout.activity_main);
		songListView = (SongListView) findViewById(R.id.song_list);
		currentTitleView = (TextView) findViewById(R.id.currentTitle);
		currentArtistView = (TextView) findViewById(R.id.currentArtist);
		controllerFrame = (FrameLayout) findViewById(R.id.controllerFrame);
		songListView.setAdapter(songAdapter);
		registerForContextMenu(songListView);
		// songListView.setOnItemClickListener(onSongClickListener);

	}

	/*
	 * @Override public void onBackPressed() {
	 * 
	 * super.onBackPressed();
	 * 
	 * if (searchEnabled) disableSearch();
	 * 
	 * }
	 */

	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * if (keyCode == KeyEvent.KEYCODE_BACK) { hideKeyboard();
	 * super.onKeyDown(keyCode, event); } return true;
	 * 
	 * }
	 */

	// Put voice recognition result to searchBox.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == voiceRecognitionHelper.getRequestCode()
				&& resultCode == RESULT_OK) {

			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			searchBox.setText(matches.get(0));

		}

		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		initSongAdapter();
		initViews();
		musicController = new MusicController(this);
		musicController.setAnchorView(controllerFrame);
		initSearch();
		initMusicService();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context, menu);

		menu.add(R.string.AddToQueue).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
								.getMenuInfo();

						musicService.addToQueue(musicService
								.getSongById(info.id));

						return true;

					}
				});

		menu.add(R.string.Delete).setOnMenuItemClickListener(
				new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {

						AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
								.getMenuInfo();

						songToBeDeleted = musicService.getSongById(info.id);

						showDeleteDialog();

						return true;

					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {

		// stopService(playIntent);
		// musicService = null;
		hideKeyboard();
		// unregisterReceiver(onPrepareReceiver);
		super.onDestroy();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_shuffle:
			
			Drawable shuffleIcon;
			
			if (musicService.isShuffling())			
			 shuffleIcon = getResources().getDrawable(
					R.drawable.shufflewhite40);
			else
				shuffleIcon = getResources().getDrawable(
						R.drawable.shufflegrey40);
			
			item.setIcon(shuffleIcon);
			musicService.toggleShuffle();
			break;

		case R.id.action_exit:

			showExitDialog();
			break;

		case R.id.action_search:

			if (searchEnabled)				
				disableSearch();
			else
				enableSearch();

			break;

		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onResume() {

		findViewById(R.id.song_list).postDelayed(new Runnable() {
			@Override
			public void run() {
				searchBox.requestFocus();
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.showSoftInput(searchBox,
						InputMethodManager.SHOW_IMPLICIT);

				hideKeyboard();
			}
		}, 100);

		super.onResume();

	}

	public void onTalkButtonClick(View view) {

		startActivityForResult(voiceRecognitionHelper.getIntent(),
				voiceRecognitionHelper.getRequestCode());

	}

	private void showDeleteDialog() {

		new AlertDialog.Builder(this)
				.setTitle("Delete Song")
				.setMessage(
						"Do you really wish to delete the song from the device?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								deleteSongToBeDeleted();
							}
						}).setNegativeButton(android.R.string.no, null).show();

	}

	private void showExitDialog() {

		new AlertDialog.Builder(this)
				.setTitle("Exit")
				.setMessage("Do you really wish to end the application?")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								exit();
							}
						}).setNegativeButton(android.R.string.no, null).show();

	}

	private void showKeyboard() {

		((InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.toggleSoftInputFromWindow(searchBox.getWindowToken(), 0, 0);

		/*
		 * InputMethodManager imm = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * imm.showSoftInput(searchBox, InputMethodManager.SHOW_FORCED);
		 */

	}

}
