package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;

// Service to play music even after application loses focus.

public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

	private class HeadsetStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)
					&& !isInitialStickyBroadcast()) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
				case 0:
					player.pause();
					break;
				case 1:
					break;
				}
			}
		}
	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	private class RemoteControlReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
				KeyEvent event = (KeyEvent) intent
						.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MEDIA_STOP:
					pause();
					break;
				case KeyEvent.KEYCODE_HEADSETHOOK:
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					if (isPng())
						pause();
					else
						go();
					break;
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					playNext();
					break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					playPrevious();
					break;

				}
			}
		}
	}

	private static final int NOTIFY_ID = 1;

	private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

		@Override
		public void onAudioFocusChange(int focusChange) {

			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				pause();
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				// onResume();
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

				ComponentName mRemoteControlResponder = new ComponentName(
						getPackageName(), RemoteControlReceiver.class.getName());
				am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
				am.abandonAudioFocus(afChangeListener);

				pause();

			}
		}
	};
	
	private final BroadcastReceiver bluetoothTurnedOnOff = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {

				pause();

			}
			if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {

				Log.d("Z", "Received: Bluetooth Connected");

			}
			if (action
					.equals("android.bluetooth.device.action.ACL_DISCONNECTED")
					|| action
							.equals("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")) {

				pause();
				Log.d("Z", "Received: Bluetooth Disconnected");

			}

		}
	};
	private HeadsetStateReceiver headsetStateReceiver;
	private final IBinder musicBind = new MusicBinder();
	private int pauseDuration = 0;

	private int pausePosition = 0;

	private MediaPlayer player;

	private ArrayList<Song> playlist;
	private ArrayList<Song> playQueue = new ArrayList<Song>();

	private Random rand;

	private boolean shuffle = false;

	private Song currentSong;

	public boolean audioFocusGranted() {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = am.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

	}

	public Song getCurrentSong() {
		return currentSong;
	}

	public int getDuration() {
		return player.getDuration();
	}

	public int getPauseDuration() {
		return pauseDuration;
	}

	public int getPausePosition() {
		return pausePosition;
	}

	public int getPosition() {
		return player.getCurrentPosition();
	}

	public Song getSongById(long songId) {
		for (Song song : playlist) {
			if (song.getId() == songId) {
				return song;
			}
		}
		return null;
	}

	public Song getSongByIndex(int index) {
		return playlist.get(index);
	}

	public void go() {

		if (audioFocusGranted())
			player.start();

	}

	public void initMusicPlayer() {

		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);

		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);

		IntentFilter receiverFilter = new IntentFilter(
				Intent.ACTION_HEADSET_PLUG);
		headsetStateReceiver = new HeadsetStateReceiver();
		registerReceiver(headsetStateReceiver, receiverFilter);

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// Request audio focus for playback
		int result = am.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			ComponentName mRemoteControlResponder = new ComponentName(
					getPackageName(), RemoteControlReceiver.class.getName());
			am.registerMediaButtonEventReceiver(mRemoteControlResponder);
		}

	}

	public boolean isPng() {
		return player.isPlaying();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {

		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}
	}

	@Override
	public void onCreate() {

		super.onCreate();

		player = new MediaPlayer();
		initMusicPlayer();
		rand = new Random();

	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		mp.start();

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(this);

		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play)
				.setTicker(getCurrentSong().getTitle()).setOngoing(true)
				.setContentTitle("Playing")
				.setContentText(getCurrentSong().getTitle());

		Notification not = builder.build();

		startForeground(NOTIFY_ID, not);

		Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PLAYING");
		LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(bluetoothTurnedOnOff, filter);
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	public void pause() {

		pauseDuration = getDuration();
		pausePosition = getPosition();
		player.pause();

	}

	public void playNext() {
		
		if(playQueue.size() > 0){
			
			currentSong = playQueue.get(0);
			playQueue.remove(0);
			playSong(currentSong);
			
			return;
			
		}
		

		int songIndex = playlist.indexOf(getCurrentSong());

		if (shuffle) {

			int newSongIndex = rand.nextInt(playlist.size());
			long newSongId = getCurrentSong().getId();

			while (newSongId == getCurrentSong().getId()) {

				newSongIndex = rand.nextInt(playlist.size());
				newSongId = playlist.get(newSongIndex).getId();

			}

			playSong(playlist.get(newSongIndex));

		} else {

			songIndex++;

			if (songIndex >= playlist.size())
				playSong(playlist.get(0));
			else
				playSong(playlist.get(songIndex));

		}
	}

	public void playPrevious() {
		// Song song = getSongById(songId);
		int songIndex = playlist.indexOf(getCurrentSong());

		songIndex--;
		if (songIndex < 0)
			songIndex = playlist.size() - 1;

		// songId = songs.get(songIndex).getID();
		playSong(playlist.get(songIndex));
	}

	public void playSong(Song song) {

		player.reset();
		this.setSong(song);

		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				song.getId());

		try {

			player.setDataSource(getApplicationContext(), trackUri);

			try {

				player.prepareAsync();

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
	}

	public void removeFromPlaylist(Song song) {

		playlist.remove(song);

	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void setSong(Song song) {
		this.currentSong = song;
	}

	public void setSongs(ArrayList<Song> songs) {
		this.playlist = songs;
		if (songs != null && songs.size() > 0)
			setSong(songs.get(0));
	}

	public void toggleShuffle() {
		if (shuffle)
			shuffle = false;
		else
			shuffle = true;
	}

	public void addToQueue(Song song) {
		playQueue.add(song);
		
	}

}
