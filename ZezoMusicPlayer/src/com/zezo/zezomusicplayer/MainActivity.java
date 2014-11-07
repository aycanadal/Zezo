package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.zezo.zezomusicplayer.MusicService.MusicBinder;

public class MainActivity extends Activity {

	private ArrayList<Song> songList;
	private ListView songView;

	private LinearLayout searchPane;
	private EditText searchBox;

	private boolean searchEnabled;
	private MusicController controller;

	private VoiceRecognitionHelper voiceRecognitionHelper;

	private MusicService musicService;
	
	private Intent playIntent;
	private SongAdapter songAdapter;
	private boolean processingPick = false;

	private HeadsetStateReceiver headsetStateReceiver;

	private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

		public void onAudioFocusChange(int focusChange) {

			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				getController().pause();
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				// onResume();
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

			if (i.getAction() != "MEDIA_PLAYER_PREPARED")
				return;

			getController().show(0);
			processingPick = false;

		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		songView = (SongListView) findViewById(R.id.song_list);
		songList = getAllSongsOnDevice();

		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		songAdapter = new SongAdapter(this, songList);
		songView.setAdapter(songAdapter);

		setController(new MusicController(this));
		getController().setAnchorView(songView);

		initSearch();

		IntentFilter receiverFilter = new IntentFilter(
				Intent.ACTION_HEADSET_PLUG);
		headsetStateReceiver = new HeadsetStateReceiver();
		registerReceiver(headsetStateReceiver, receiverFilter);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				onPrepareReceiver, new IntentFilter("MEDIA_PLAYER_PREPARED"));
		
		voiceRecognitionHelper = new VoiceRecognitionHelper(searchBox);
		
		if (playIntent == null) {

			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);

		}

	}

	private class HeadsetStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
				case 0:
					getController().pause();
					break;
				case 1:
					break;
				}
			}
		}
	}
	
	

	private ServiceConnection musicConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			MusicBinder binder = (MusicBinder) service;
			musicService = binder.getService();
			musicService.setSongs(songList);
			getController().init(musicService);
			getController().setMusicBound(true);

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

			getController().setMusicBound(false);

		}
	};

	@Override
	protected void onResume() {

		super.onResume();

		/*
		 * AudioManager am = (AudioManager)
		 * getSystemService(Context.AUDIO_SERVICE);
		 * 
		 * // Request audio focus for playback int result =
		 * am.requestAudioFocus(afChangeListener, // Use the music stream.
		 * AudioManager.STREAM_MUSIC, // Request permanent focus.
		 * AudioManager.AUDIOFOCUS_GAIN);
		 * 
		 * if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
		 * ComponentName mRemoteControlResponder = new ComponentName(
		 * getPackageName(), RemoteControlReceiver.class.getName());
		 * am.registerMediaButtonEventReceiver(mRemoteControlResponder);
		 */

		// Start playback.

		// if (paused) {
		// setController();
		// paused = false;
		// }

		// Set up receiver for media player onPrepared broadcast

		/*
		 * LocalBroadcastManager.getInstance(this).registerReceiver(
		 * onPrepareReceiver, new IntentFilter("MEDIA_PLAYER_PREPARED"));
		 */
		// }

	}
	
	@Override
	protected void onDestroy() {

		stopService(playIntent);
		musicService = null;
		hideKeyboard();
		super.onDestroy();

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

	public void onSongPicked(View view) {

		// searchBox.clearFocus();

		if (processingPick)
			return;

		processingPick = true;

		Song song = songAdapter.getItem(Integer.parseInt(((View) view
				.getParent()).getTag().toString()));

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

	public void onTalkButtonClick(View view) {
		
		startActivityForResult(voiceRecognitionHelper.getIntent(),
				voiceRecognitionHelper.getRequestCode());
		
	}

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

	private void initSearch() {

		searchPane = (LinearLayout) findViewById(R.id.searchPane);
		searchBox = (EditText) findViewById(R.id.inputSearch);
		searchPane.setVisibility(View.GONE);
		// searchBox.setVisibility(View.GONE);
		searchEnabled = false;

		searchBox.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
				// When user changed the Text
				MainActivity.this.songAdapter.getFilter().filter(cs);
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

	private void enableSearch() {

		searchEnabled = true;
		searchPane.setVisibility(View.VISIBLE);
		searchBox.requestFocus();
		showKeyboard();
		getController().setVisibility(View.GONE);

	}

	private void disableSearch() {

		searchEnabled = false;
		searchBox.setText("");
		searchPane.setVisibility(View.GONE);
		hideKeyboard();
		getController().setVisibility(View.VISIBLE);

	}

	private void showKeyboard() {

		((InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE))
				.toggleSoftInput(0, 0);

	}

	/*
	 * public void onSearchBoxClick(View view) {
	 * 
	 * searchBox.requestFocus(); //showKeyboard();
	 * 
	 * }
	 */

	private void hideKeyboard() {

		// searchBox.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

	}

	private ArrayList<Song> getAllSongsOnDevice() {

		ArrayList<Song> songs = new ArrayList<Song>();

		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null,
				null);

		if (musicCursor != null && musicCursor.moveToFirst()) {

			int titleColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor
					.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

			do {

				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songs.add(new Song(thisId, thisTitle, thisArtist));

			} while (musicCursor.moveToNext());

		}

		return songs;

	}

	private void exit() {

		hideKeyboard();
		stopService(playIntent);
		musicService = null;
		System.exit(0);

	}

	public MusicController getController() {
		return controller;
	}

	public void setController(MusicController controller) {
		this.controller = controller;
	}

}
