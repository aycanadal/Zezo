package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import com.zezo.zezomusicplayer.MusicService.MusicBinder;

public class MainActivity extends Activity implements MediaPlayerControl {

	private boolean processingPick = false;

	private SongAdapter songAdt;

	private boolean paused = false, playbackPaused = false;

	private MusicController controller;

	private MusicService musicService;
	private Intent playIntent;
	private boolean musicBound = false;

	private ArrayList<Song> songList;
	private ListView songView;
	private EditText searchBox;
	private boolean searchEnabled;

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

		public void onAudioFocusChange(int focusChange) {

			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				pause();
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				onResume();
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				ComponentName mRemoteControlResponder = new ComponentName(
						getPackageName(), RemoteControlReceiver.class.getName());
				am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
				am.abandonAudioFocus(afChangeListener);
				// Stop playback
			}
		}
	};

	// Broadcast receiver to determine when music player has been prepared
	private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			// When music player has been prepared, show controller

			if (playbackPaused) {
				// setController();
				playbackPaused = false;
			}
			initController();
			controller.show(0);
			processingPick = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		songView = (ListView) findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();

		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);

		initController();
		initSearch();
	}

	private void initSearch() {

		searchBox = (EditText) findViewById(R.id.inputSearch);
		searchBox.setVisibility(View.GONE);
		// searchBox.setEnabled(false);
		searchEnabled = false;

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				MainActivity.this.songAdt.getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});

		// searchBox.setText("a");
		// searchBox.setText("");

	}

	// connect to the service
	private ServiceConnection musicConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			// get service
			musicService = binder.getService();
			// pass list
			musicService.setSongs(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		if (playIntent == null) {
			playIntent = new Intent(this, MusicService.class);
			boolean bound = bindService(playIntent, musicConnection,
					Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_shuffle:

			musicService.toggleShuffle();
			break;

		case R.id.action_end:

			exit();
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

	private void exit() {

		hideKeyboard();
		stopService(playIntent);
		musicService = null;
		System.exit(0);

	}

	private void enableSearch() {

		searchEnabled = true;
		searchBox.setVisibility(View.VISIBLE);
		boolean tookFocus = searchBox.requestFocus();
		showKeyboard();
		controller.setVisibility(View.GONE);

	}

	private void disableSearch() {

		searchEnabled = false;
		searchBox.setText("");
		searchBox.setVisibility(View.GONE);
		hideKeyboard();
		controller.setVisibility(View.VISIBLE);

	}

	private void showKeyboard() {

		((InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.toggleSoftInput(0, 0);

	}

	public void getSongList() {

		// retrieve song info
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null,
				null);
		if (musicCursor != null && musicCursor.moveToFirst()) {
			// get columns
			int titleColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			// add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			} while (musicCursor.moveToNext());
		}
	}

	public void songPicked(View view) {

		// searchBox.clearFocus();

		if (processingPick)
			return;

		processingPick = true;

		Song song = songAdt.getItem(Integer.parseInt(view.getTag().toString()));

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Request audio focus for playback
		int result = am.requestAudioFocus(afChangeListener,
		// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

			ComponentName mRemoteControlResponder = new ComponentName(
					getPackageName(), RemoteControlReceiver.class.getName());
			am.registerMediaButtonEventReceiver(mRemoteControlResponder);
			// Start playback.
			musicService.playSong(song);

		}

	}

	public void onSearchBoxClick(View view) {

		searchBox.requestFocus();

		showKeyboard();

	}

	@Override
	public void onBackPressed() {

		if (searchEnabled)
			disableSearch();

		super.onBackPressed();

	}

	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
	 * 
	 * if (keyCode == KeyEvent.KEYCODE_BACK) { hideKeyboard();
	 * super.onKeyDown(keyCode, event); } return true;
	 * 
	 * }
	 */
	private void hideKeyboard() {

		// searchBox.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

	}

	private void initController() {

		if (controller == null)
			controller = new MusicController(this);

		controller.setPrevNextListeners(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});

		controller.setMediaPlayer(this);
		controller.setEnabled(true);
		controller.setAnchorView(songView);

		// controller.setAnchorView(findViewById(android.R.id.content).getRootView());
		// controller.setAnchorView(findViewById(android.R.id.song_list));

		// controller.setPadding(0, 0, 0, controller.getHeight());

	}

	@Override
	protected void onPause() {

		super.onPause();
		paused = true;

	}

	@Override
	protected void onResume() {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Request audio focus for playback
		int result = am.requestAudioFocus(afChangeListener,
		// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			ComponentName mRemoteControlResponder = new ComponentName(
					getPackageName(), RemoteControlReceiver.class.getName());
			am.registerMediaButtonEventReceiver(mRemoteControlResponder);
			// Start playback.

			super.onResume();
			if (paused) {
				// setController();
				paused = false;
			}

			// Set up receiver for media player onPrepared broadcast

			LocalBroadcastManager.getInstance(this).registerReceiver(
					onPrepareReceiver,
					new IntentFilter("MEDIA_PLAYER_PREPARED"));
		}

	}

	@Override
	protected void onStop() {
		controller.hide();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicService = null;
		super.onDestroy();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (musicService != null && musicBound && musicService.isPng())
			// if (musicSrv != null && musicBound)
			return musicService.getPosn();
		else if (musicService != null && musicBound)
			return musicService.getPausePosition();
		else
			return 0;
	}

	@Override
	public int getDuration() {

		// Log.d(musicSrv.);

		if (musicService != null && musicBound && musicService.isPng())
			// if (musicSrv != null && musicBound)
			return musicService.getDur();
		else if (musicService != null && musicBound) {
			int pauseDuration = musicService.getPauseDuration();
			return musicService.getPauseDuration();
		} else
			return 0;
	}

	@Override
	public boolean isPlaying() {
		if (musicService != null & musicBound)
			return musicService.isPng();
		return false;
	}

	@Override
	public void pause() {
		playbackPaused = true;
		musicService.pausePlayer();
	}

	@Override
	public void seekTo(int pos) {
		musicService.seek(pos);
	}

	@Override
	public void start() {
		musicService.go();
	}

	private void playNext() {
		musicService.playNext();
		if (playbackPaused) {
			initController();
			playbackPaused = false;
		}
		controller.show(0);
	}

	private void playPrev() {
		musicService.playPrevious();
		if (playbackPaused) {
			initController();
			playbackPaused = false;
		}
		controller.show(0);
	}

}
