package com.zezo.zezomusicplayer;

import java.io.IOException;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;

import java.util.ArrayList;

import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

	private boolean shuffle = false;
	private Random rand;

	private String songTitle = "";
	private static final int NOTIFY_ID = 1;

	private final IBinder musicBind = new MusicBinder();

	// media player
	private MediaPlayer player;
	// song list
	private ArrayList<Song> songs;
	// current position
	private long songId;
	private int pauseDuration = 0;
	private int pausePosition = 0;

	public void onCreate() {
		// create the service
		super.onCreate();
		// initialize position
		songId = 0;
		// create player
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

	}

	public void setList(ArrayList<Song> theSongs) {
		songs = theSongs;
	}

	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}

	public void playSong() {

		player.reset();

		// get song
		Song playSong = getSongById(songId);

		songTitle = playSong.getTitle();

		// get id
		long currSong = playSong.getID();
		// set uri
		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				currSong);

		try {
			player.setDataSource(getApplicationContext(), trackUri);
		} catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}

		try {

			player.prepareAsync();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			Log.d("", e.getMessage());
		}

		int a = 0;

	}

	private Song getSongById(long songId) {
		for (Song song : songs) {
			if (song.getID() == songId) {
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

		int currentPosition = player.getCurrentPosition();

		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// start playback
		mp.start();

		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Builder builder = new Notification.Builder(this);

		builder.setContentIntent(pendInt).setSmallIcon(R.drawable.play)
				.setTicker(songTitle).setOngoing(true)
				.setContentTitle("Playing").setContentText(songTitle);
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
		return false;
	}

	public void setCurrentSongId(long songId) {
		this.songId = songId;
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

	public void pausePlayer() {
		pauseDuration = getDur();
		pausePosition = getPosn();
		player.pause();
	}

	public void seek(int posn) {
		player.seekTo(posn);
	}

	public void go() {
		player.start();
	}

	public void playPrevious() {
		Song song = getSongById(songId);
		int songIndex = songs.indexOf(song);

		songIndex--;
		if (songIndex < 0)
			songIndex = songs.size() - 1;

		songId = songs.get(songIndex).getID();
		playSong();
	}

	public void playNext() {

		Song song = getSongById(songId);
		int songIndex = songs.indexOf(song);

		if (shuffle) {

			int newSongIndex;
			long newSongId = songId;

			while (newSongId == songId) {
				newSongIndex = rand.nextInt(songs.size());
				newSongId = songs.get(newSongIndex).getID();
			}

			songId = newSongId;

		} else {

			songIndex++;

			if (songIndex >= songs.size())
				songId = songs.get(0).getID();
			else
				songId = songs.get(songIndex).getID();

		}

		playSong();

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

}
