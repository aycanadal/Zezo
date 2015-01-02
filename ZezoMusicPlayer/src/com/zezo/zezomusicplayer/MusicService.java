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
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;

import com.zezo.zezomusicplayer.MediaButtonReceiver.MediaButtonReceiverListener;

// Service to play music even after application loses focus.

public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener, MediaButtonReceiverListener,
		OnAudioFocusChangeListener {

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	private class HeadsetStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)
					&& !isInitialStickyBroadcast()) {
				int state = intent.getIntExtra("state", -1);
				switch (state) {
				case 0:
					pause();
					break;
				case 1:
					break;
				}
			}
		}
	}

	private static final int NOTIFY_ID = 1;

	private Song currentSong;
	private HeadsetStateReceiver headsetStateReceiver;
	private final IBinder musicBind = new MusicBinder();
	private final BroadcastReceiver onBluetoothStateChangeReceiver = new BroadcastReceiver() {

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

	private int pauseDuration = 0;

	private int pausePosition = 0;

	private MediaPlayer player;
	private ArrayList<Song> playQueue = new ArrayList<Song>();

	private Random rand;

	private boolean shuffle = false;

	private ArrayList<Song> songLibrary;

	// private OnAudioFocusChangeListener mOnAudioFocusChangeListener;

	public void addToQueue(Song song) {
		playQueue.add(song);

	}

	public boolean audioFocusGranted() {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
			return false;

		registerMediaButtonListener();
		return true;
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
		for (Song song : songLibrary) {
			if (song.getId() == songId) {
				return song;
			}
		}
		return null;
	}

	public Song getSongByIndex(int index) {
		return songLibrary.get(index);
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

		audioFocusGranted();

	}

	public boolean isPlaying() {

		return player.isPlaying();

	}

	public boolean isShuffling() {
		return shuffle;
	}

	@Override
	public void onAudioFocusChange(int focusChange) {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			pause();
			ComponentName mRemoteControlResponder = new ComponentName(
					getPackageName(), MediaButtonReceiver.class.getName());
			am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
			MediaButtonReceiver.removeBroadcastReceiveListener(this);
			am.abandonAudioFocus(this);
		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

			registerMediaButtonListener();

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

			ComponentName mRemoteControlResponder = new ComponentName(
					getPackageName(), MediaButtonReceiver.class.getName());
			am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
			MediaButtonReceiver.removeBroadcastReceiveListener(this);
			am.abandonAudioFocus(this);

			pause();

		}
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
	public void onMediaButtonReceived(int keyCode) {

		switch (keyCode) {

		case KeyEvent.KEYCODE_MEDIA_STOP:
			pause();
			break;

		case KeyEvent.KEYCODE_HEADSETHOOK:

		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:

			if (isPlaying())
				pause();
			else
				play();
			break;

		case KeyEvent.KEYCODE_MEDIA_NEXT:

			playNext();
			break;

		case KeyEvent.KEYCODE_MEDIA_PREVIOUS:

			playPrevious();
			break;

		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		mp.start();

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			Notification.Builder builder = new Notification.Builder(this);

			builder.setContentIntent(pendInt)
					.setSmallIcon(R.drawable.ic_launcher)
					.setTicker(
							getCurrentSong().getArtist() + " - "
									+ getCurrentSong().getTitle())
					.setOngoing(true)
					.setContentTitle(getCurrentSong().getTitle())
					.setContentText(getCurrentSong().getArtist());

			Notification not = builder.build();

			startForeground(NOTIFY_ID, not);
		}

		Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PLAYING");
		LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(onBluetoothStateChangeReceiver, filter);
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

	public void play() {

		if (audioFocusGranted() && currentSong != null)

			player.start();

	}

	public void playNext() {

		if (songLibrary.size() <= 0)
			return;

		if (playQueue.size() > 0) {

			currentSong = playQueue.get(0);
			playQueue.remove(0);
			playSong(currentSong);

			return;

		}

		int songIndex = songLibrary.indexOf(getCurrentSong());

		if (isShuffling()) {

			int newSongIndex = rand.nextInt(songLibrary.size());
			long newSongId = getCurrentSong().getId();

			while (newSongId == getCurrentSong().getId()) {

				newSongIndex = rand.nextInt(songLibrary.size());
				newSongId = songLibrary.get(newSongIndex).getId();

			}

			playSong(songLibrary.get(newSongIndex));

		} else {

			songIndex++;

			if (songIndex >= songLibrary.size())
				playSong(songLibrary.get(0));
			else
				playSong(songLibrary.get(songIndex));

		}
	}

	public void playPrevious() {

		if (songLibrary.size() <= 0)
			return;

		// Song song = getSongById(songId);
		int songIndex = songLibrary.indexOf(getCurrentSong());

		songIndex--;
		if (songIndex < 0)
			songIndex = songLibrary.size() - 1;

		// songId = songs.get(songIndex).getID();
		playSong(songLibrary.get(songIndex));
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

		songLibrary.remove(song);

	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void setPausePosition(int pausePosition) {
		this.pausePosition = pausePosition;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public void setSong(Song song) {
		this.currentSong = song;
	}

	public void setSongLibrary(ArrayList<Song> songs) {
		this.songLibrary = songs;
		if (songs != null && songs.size() > 0)
			setSong(songs.get(0));
	}

	public void toggleShuffle() {
		if (isShuffling())
			setShuffle(false);
		else
			setShuffle(true);
	}

	private void registerMediaButtonListener() {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		ComponentName mRemoteControlResponder = new ComponentName(
				getPackageName(), MediaButtonReceiver.class.getName());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
			am.registerMediaButtonEventReceiver(mRemoteControlResponder);

		MediaButtonReceiver.addBroadcastReceiveListener(this);

	}
}
