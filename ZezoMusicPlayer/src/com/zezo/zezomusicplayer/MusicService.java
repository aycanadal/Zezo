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
import android.media.MediaPlayer;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

	private boolean shuffle = false;
	private Random rand;
	private static final int NOTIFY_ID = 1;
	private final IBinder musicBind = new MusicBinder();
	private MediaPlayer player;
	private ArrayList<Song> songs;
	private int pauseDuration = 0;
	private int pausePosition = 0;
	private Song song;

	private HeadsetStateReceiver headsetStateReceiver;

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

	private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {

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

	public void onCreate() {

		super.onCreate();

		player = new MediaPlayer();
		initMusicPlayer();
		rand = new Random();

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

	public void setSongs(ArrayList<Song> songs) {
		this.songs = songs;
		if (songs != null && songs.size() > 0)
			setSong(songs.get(0));
	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	public void playSong(Song song) {

		player.reset();
		this.setSong(song);

		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				song.getId());

		try {

			player.setDataSource(getApplicationContext(), trackUri);

		} catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}

		try {

			player.prepareAsync();

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	private Song getSongById(long songId) {
		for (Song song : songs) {
			if (song.getId() == songId) {
				return song;
			}
		}
		return null;
	}

	public void toggleShuffle() {
		if (shuffle)
			shuffle = false;
		else
			shuffle = true;
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
	public void onPrepared(MediaPlayer mp) {

		mp.start();

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(this);

		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play)
				.setTicker(getSong().getTitle()).setOngoing(true)
				.setContentTitle("Playing")
				.setContentText(getSong().getTitle());

		Notification not = builder.build();

		startForeground(NOTIFY_ID, not);

		Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
		LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

	}

	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return true;
	}

	public int getPosn() {
		return player.getCurrentPosition();
	}

	public int getDur() {
		return player.getDuration();
	}

	public boolean isPng() {
		return player.isPlaying();
	}

	public void pause() {

		pauseDuration = getDur();
		pausePosition = getPosn();
		player.pause();

	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void go() {

		if (audioFocusGranted())
			player.start();

	}

	public boolean audioFocusGranted() {

		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = am.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

	}

	public void playPrevious() {
		// Song song = getSongById(songId);
		int songIndex = songs.indexOf(getSong());

		songIndex--;
		if (songIndex < 0)
			songIndex = songs.size() - 1;

		// songId = songs.get(songIndex).getID();
		playSong(songs.get(songIndex));
	}

	public void playNext() {

		int songIndex = songs.indexOf(getSong());

		if (shuffle) {

			int newSongIndex = rand.nextInt(songs.size());
			long newSongId = getSong().getId();

			while (newSongId == getSong().getId()) {

				newSongIndex = rand.nextInt(songs.size());
				newSongId = songs.get(newSongIndex).getId();

			}

			playSong(songs.get(newSongIndex));

		} else {

			songIndex++;

			if (songIndex >= songs.size())
				playSong(songs.get(0));
			else
				playSong(songs.get(songIndex));

		}
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
	}

	public int getPauseDuration() {
		return pauseDuration;
	}

	public int getPausePosition() {
		return pausePosition;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(bluetoothTurnedOnOff, filter);
		return START_STICKY;
	}

	public Song getSong() {
		return song;
	}

	public void setSong(Song song) {
		this.song = song;
	}

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

}
