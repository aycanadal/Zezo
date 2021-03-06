package com.zezo.music;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zezo.music.MusicService.MusicBinder;
import com.zezo.music.shared.MusicControllerFragment;
import com.zezo.music.shared.Song;
import com.zezo.music.tabs.TabPagerAdapter;
import com.zezo.music.tabs.TabPagerAdapter.Tabs;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.util.Util;
import com.zezo.music.util.YesNoDialogFragment;
import com.zezo.music.util.YesNoDialogFragment.OnDeleteConfirmedListener;

import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity implements OnDeleteConfirmedListener {

    public static final String PACKAGE_NAME = "com.zezo.music";
    public static final String KEY_DIRECTORY_SELECTED = MusicPlayerActivity.PACKAGE_NAME + ".DIRECTORY_SELECTED";

    private Menu optionsMenu;
    private ViewPager viewPager;
    private Intent musicServiceIntent;
    private MusicService musicService;
    private TabPagerAdapter tabPagerAdapter;
    private SharedPreferences sharedPreferences;
    private MusicControllerFragment musicControllerFragment;

    private ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();

            LocalBroadcastManager.getInstance(MusicPlayerActivity.this).registerReceiver(onMediaPlayerPlayingReceiver,
                    new IntentFilter("MEDIA_PLAYER_PLAYING"));

            String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
                    Environment.getExternalStorageDirectory().toString());

            musicService.setPlaylist(getAllSongsInFolder(musicFolder));
            PlaylistFragment playlistFragment = tabPagerAdapter.getPlaylistFragment();

            if (playlistFragment != null)
                playlistFragment.loadPlaylist(musicService.getPlaylist());

            musicControllerFragment.initController(musicService);
            updateViewsWithCurrentState();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            musicService = null;

        }
    };

    private BroadcastReceiver onMediaPlayerPlayingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context c, Intent i) {

            if (i.getAction() != "MEDIA_PLAYER_PLAYING")
                return;

            musicService.setPlayerPrepared(true);
            tabPagerAdapter.getQueueFragment().setQueue(musicService.getQueue());
            updateViewsWithCurrentState();

        }
    };

    public void updateViewsWithCurrentState() {

        updateShuffleIcon();
        //musicControllerFragment.show();
        Song currentSong = musicService.getCurrentSong();

        if (currentSong == null)
            return;

        tabPagerAdapter.getNowPlayingFragment().setInfo(currentSong);
        tabPagerAdapter.getPlaylistFragment().setItemChecked(currentSong);
        musicControllerFragment.initController(musicService);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getTheme().applyStyle(new Preferences(this).getFontStyle().getResId(), true);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setCurrentItem(Tabs.PLAYLIST.ordinal());
        viewPager.setOffscreenPageLimit(5);
        musicControllerFragment = (MusicControllerFragment) getSupportFragmentManager().findFragmentById(R.id.mediaController);

        ImageButton nowPlayingToggle = (ImageButton) findViewById(R.id.mediaControllerToggle);
        nowPlayingToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View nowPlayingToggle) {

                if (musicControllerFragment.isVisible())
                    hideController();
                else
                    showController();

            }

        });

    }

    public void hideController(){

        ImageButton nowPlayingToggle = (ImageButton) findViewById(R.id.mediaControllerToggle);
        ((ImageButton) nowPlayingToggle).setImageResource(R.drawable.arrowsup);
        musicControllerFragment.hide();

    }

    private void showController(){

        ImageButton nowPlayingToggle = (ImageButton) findViewById(R.id.mediaControllerToggle);
        ((ImageButton) nowPlayingToggle).setImageResource(R.drawable.arrowsdown);
        musicControllerFragment.show();

    }

    @Override
    protected void onResume() {

        super.onResume();

        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(

                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Log.d("Activity", "onGlobalLayout");
                        viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (musicService == null)
                            startMusicService();
                        else
                            updateViewsWithCurrentState();

                        showController();

                    }
                });

        // To get onGlobalLayout called for sure because it doesn't get called every other time activity is started from notification.
        viewPager.requestLayout();

    }

    @Override
    protected void onStop() {

        musicControllerFragment.hideController();
        super.onStop();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onMediaPlayerPlayingReceiver);
        unbindService(musicServiceConnection);

    }

    private void startMusicService() {

            musicServiceIntent = new Intent(this, MusicService.class);
            startService(musicServiceIntent);
            bindService(musicServiceIntent, musicServiceConnection, 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        optionsMenu = menu;
        return true;

    }

    private ArrayList<Song> getAllSongsInFolder(String folderPath) {

        ArrayList<Song> songs = new ArrayList<Song>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor musicCursor = musicResolver.query(musicUri, null, MediaColumns.DATA + " like ? ",
                new String[]{"%" + folderPath + "%"}, MediaColumns.DATA + " ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {

            int titleColumn = musicCursor.getColumnIndex(MediaColumns.TITLE);
            int idColumn = musicCursor.getColumnIndex(BaseColumns._ID);
            int artistColumn = musicCursor.getColumnIndex(AudioColumns.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(AudioColumns.DURATION);
            int dataColumn = musicCursor.getColumnIndex(MediaColumns.DATA);

            do {

                long id = musicCursor.getLong(idColumn);
                String title = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                String duration = Util.getTimeStringFromMs(musicCursor.getInt(durationColumn));
                String data = musicCursor.getString(dataColumn);
                Song song = new Song(id, title, artist, duration, data);
                songs.add(song);

            } while (musicCursor.moveToNext());

        }

        musicCursor.close();
        return songs;

    }

    @Override
    public void onBackPressed() {

        // Do nothing.

    }

    @Override
    public void onDeleteConfirmed(long songId) {

        Song song = musicService.getSongById(songId);

        if (song == musicService.getCurrentSong())
            return;

        tabPagerAdapter.getPlaylistFragment().remove(song);
        getContentResolver().delete(song.getUri(), null, null);
        musicService.removeFromPlaylist(song);

    }


    public void onContextMenuButtonClicked(View view) {

        openContextMenu(view);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

           case R.id.action_shuffle:

                toggleShuffle();
                return true;

            case R.id.action_exit:

                showExitDialog();
                return true;

        }

        return false;

    }

    public void loadFolder(String musicFolderPath){

        Toast.makeText(MusicPlayerActivity.this, "Selected music folder:" + musicFolderPath, Toast.LENGTH_SHORT)
                .show();

        musicService.setPlaylist(getAllSongsInFolder(musicFolderPath));
        tabPagerAdapter.getPlaylistFragment().loadPlaylist(musicService.getPlaylist());
        sharedPreferences.edit().putString(KEY_DIRECTORY_SELECTED, musicFolderPath).apply();

    }

    public void play(Song song) {

        if (song != null && musicService.getAudioFocus()) {

            musicService.playSong(song);

        }

    }

    private void toggleShuffle() {

        musicService.toggleShuffle();
        updateShuffleIcon();

    }

    private void exit() {

        stopService(new Intent(MusicPlayerActivity.this, MusicService.class));
        System.exit(0);

    }

    public ArrayList<Song> getPlaylist() {

        if (musicService == null)
            return new ArrayList<>();

        return musicService.getPlaylist();

    }

    public void addToQueue(long songId) {

        Song song = musicService.getSongById(songId);
        musicService.addToQueue(song);
        tabPagerAdapter.getQueueFragment().setQueue(musicService.getQueue());

    }

    public void removeFromQueue(long songId) {

        Song song = musicService.getSongById(songId);
        musicService.removeFromQueue(song);
        tabPagerAdapter.getQueueFragment().setQueue(musicService.getQueue());

    }

    public void showDeleteDialog(long songId) {

        showDeleteDialog(musicService.getSongById(songId));

    }

    private void showDeleteDialog(Song song) {

        DialogFragment dialog = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", "Delete File?");
        args.putString("message", "Do you really wish to delete " + song.getTitle() + " by " + song.getArtist() + " from the device?");
        args.putLong("songId", song.getId());
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), "deleteDialog");

    }

    private void showExitDialog() {

        new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really wish to quit the application?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        exit();
                    }
                }).setNegativeButton(android.R.string.no, null).show();

    }

    public boolean isShuffling() {

        return musicService.isShuffling();

    }

    public void updateShuffleIcon() {

        MenuItem item = optionsMenu.findItem(R.id.action_shuffle);

        if (item == null || musicService == null)
            return;

        int shuffleIconIndex = R.drawable.shufflegrey40;

        if (isShuffling())
            shuffleIconIndex = R.drawable.shufflewhite40;

        Drawable shuffleIcon = getResources().getDrawable(shuffleIconIndex);
        item.setIcon(shuffleIcon);

    }
}