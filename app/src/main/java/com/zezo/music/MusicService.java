package com.zezo.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.Toast;

import com.zezo.music.MediaButtonReceiver.MediaButtonReceiverListener;
import com.zezo.music.shared.Song;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaButtonReceiverListener, OnAudioFocusChangeListener {

    private static final int NOTIFY_ID = 1;
    private Song currentSong;
    private ArrayList<Song> playlist = new ArrayList<Song>();
    private ArrayList<Song> queue = new ArrayList<Song>();
    private LinkedList<Long> history = new LinkedList<Long>();
    private HeadsetStateReceiver headsetStateReceiver;
    private final IBinder musicBind = new MusicBinder();
    private int pauseDuration = 0;
    private int pausePosition = 0;
    private MediaPlayer mediaPlayer;
    private Random randomGenerator = new Random();
    private boolean shuffle = false;
    private boolean isPlayerPrepared = false;

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private class HeadsetStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) && !isInitialStickyBroadcast()) {

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
            if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")
                    || action.equals("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")) {

                pause();
                Log.d("Z", "Received: Bluetooth Disconnected");

            }

        }
    };

    public void addToQueue(Song song) {

        queue.add(song);

    }

    public void removeFromQueue(Song song) {

        queue.remove(song);

    }

    public boolean getAudioFocus() {

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return false;

        registerMediaButtonListener();
        return true;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getPauseDuration() {
        return pauseDuration;
    }

    public int getPausePosition() {
        return pausePosition;
    }

    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public Song getSongById(long songId) {

        for (Song song : playlist) {
            if (song.getId() == songId) {
                return song;
            }
        }

        for (Song song : queue) {
            if (song.getId() == songId) {
                return song;
            }
        }

        return null;
    }

    public boolean isPlaying() {

        return mediaPlayer.isPlaying();

    }

    public boolean isShuffling() {
        return shuffle;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        Log.d("AudioFocus", "onAudioFocusChange");

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

            registerMediaButtonListener();

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

            ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
                    MediaButtonReceiver.class.getName());
            am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
            MediaButtonReceiver.removeBroadcastReceiveListener(this);
            am.abandonAudioFocus(this);

            if (getCurrentSong() == null)
                return;

            pause();

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

            if (isPlaying()) {
                pause();
            }

            ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
                    MediaButtonReceiver.class.getName());
            am.unregisterMediaButtonEventReceiver(mRemoteControlResponder);
            MediaButtonReceiver.removeBroadcastReceiveListener(this);
            am.abandonAudioFocus(this);

        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return musicBind;

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (mediaPlayer.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }

    }

    @Override
    public void onCreate() {

        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        headsetStateReceiver = new HeadsetStateReceiver();
        registerReceiver(headsetStateReceiver, receiverFilter);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(onBluetoothStateChangeReceiver, filter1);
        this.registerReceiver(onBluetoothStateChangeReceiver, filter2);
        this.registerReceiver(onBluetoothStateChangeReceiver, filter3);

        getAudioFocus();

    }

    @Override
    public void onDestroy() {

        unregisterReceiver(headsetStateReceiver);
        unregisterReceiver(onBluetoothStateChangeReceiver);
        setCurrentSong(null);
        stopForeground(true);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        if (what == 1 && extra == -2147483648)
            Toast.makeText(this, "File type not supported.", Toast.LENGTH_SHORT).show();
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
                break;

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
    public void onPrepared(MediaPlayer mediaPlayer) {

        mediaPlayer.start();

        Intent notIntent = new Intent(this, MusicPlayerActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Notification.Builder builder = new Notification.Builder(this);

            builder.setContentIntent(pendInt).setSmallIcon(R.drawable.ic_launcher)
                    .setTicker(getCurrentSong().getArtist() + " - " + getCurrentSong().getTitle()).setOngoing(true)
                    .setContentTitle(getCurrentSong().getTitle()).setContentText(getCurrentSong().getArtist());

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

    public void pause() {

        pauseDuration = getDuration();
        pausePosition = getPosition();
        mediaPlayer.pause();

    }

    public void play() {

        if (!getAudioFocus() || getCurrentSong() == null)
            return;

        if (isPlayerPrepared)
            mediaPlayer.start();
        else
            playSong(currentSong);

    }

    public void playNext() {

        if (playlist.size() <= 0)
            return;

        if (queue.size() > 0) {

            currentSong = queue.get(0);
            queue.remove(0);
            playSong(currentSong);

            return;

        }

        int songIndex = playlist.indexOf(getCurrentSong());

        if (isShuffling()) {

            Song currentSong = getCurrentSong();
            int newSongIndex;

            if (currentSong == null) {

                newSongIndex = randomGenerator.nextInt(playlist.size());
                playSong(playlist.get(newSongIndex));
                return;

            }

            long newSongId;

            do {

                newSongIndex = randomGenerator.nextInt(playlist.size());
                newSongId = playlist.get(newSongIndex).getId();

            } while (newSongId == currentSong.getId());

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

        if (playlist.size() <= 0 || history.size() <= 1)
            return;

        history.pollLast();
        Song song = getSongById(history.pollLast());

        playSong(song);

    }

    public void playSong(Song song) {

        mediaPlayer.reset();
        history.add(song.getId());

        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getId());

        try {

            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
            setCurrentSong(song); // Set this as late as possible otherwise illegal state exception.
            mediaPlayer.prepareAsync();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play media.", Toast.LENGTH_SHORT).show();
        }

    }

    public void removeFromPlaylist(Song song) {

        playlist.remove(song);

    }

    public void seek(int posn) {
        mediaPlayer.seekTo(posn);
    }

    public void setPausePosition(int pausePosition) {
        this.pausePosition = pausePosition;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void setCurrentSong(Song song) {
        this.currentSong = song;
    }

    public void setPlaylist(ArrayList<Song> songs) {

        this.playlist = songs;

    }

    public void toggleShuffle() {

        if (isShuffling())
            setShuffle(false);
        else
            setShuffle(true);

    }

    private void registerMediaButtonListener() {

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        ComponentName mRemoteControlResponder = new ComponentName(getPackageName(),
                MediaButtonReceiver.class.getName());

        am.registerMediaButtonEventReceiver(mRemoteControlResponder);

        MediaButtonReceiver.addBroadcastReceiveListener(this);

    }

    public void setPlayerPrepared(boolean isPlayerPrepared) {

        this.isPlayerPrepared = isPlayerPrepared;

    }

    public ArrayList<Song> getQueue() {

        return queue;

    }

    public ArrayList<Song> getPlaylist() {

        return playlist;

    }
}