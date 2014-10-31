package com.zezo.zezomusicplayer;

import android.widget.EditText;
import android.widget.MediaController.MediaPlayerControl;
import android.app.Activity;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.zezo.zezomusicplayer.MusicService.MusicBinder;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
	private EditText inputSearch;

	// Broadcast receiver to determine when music player has been prepared
	private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent i) {
			// When music player has been prepared, show controller

			if (playbackPaused) {
				// setController();
				playbackPaused = false;
			}
			setController();
			controller.show(0);
			processingPick = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		songView = (ListView) findViewById(R.id.song_list);

		inputSearch = (EditText) findViewById(R.id.inputSearch);

		inputSearch.addTextChangedListener(new TextWatcher() {

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

		songList = new ArrayList<Song>();
		getSongList();
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});

		songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		setController();
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
			stopService(playIntent);
			musicService = null;
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
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

		if (processingPick)
			return;

		processingPick = true;

		Song song = songAdt.getItem(Integer.parseInt(view.getTag().toString()));
		// musicSrv.setCurrentSongId(songId);
		musicService.playSong(song);

	}

	private void setController() {
		// set the controller up
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
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
		controller.setAnchorView(songView);

	}

	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (paused) {
			// setController();
			paused = false;
		}

		// Set up receiver for media player onPrepared broadcast
		LocalBroadcastManager.getInstance(this).registerReceiver(
				onPrepareReceiver, new IntentFilter("MEDIA_PLAYER_PREPARED"));
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
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}

	private void playPrev() {
		musicService.playPrevious();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}

}
