package com.zezo.music;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zezo.music.MusicService.MusicBinder;
import com.zezo.music.domain.Song;
import com.zezo.music.tabs.TabPagerAdapter;
import com.zezo.music.tabs.TabPagerAdapter.Tabs;
import com.zezo.music.tabs.folders.FoldersFragment;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.util.Util;
import com.zezo.music.util.YesNoDialogFragment;
import com.zezo.music.util.YesNoDialogFragment.OnDeleteConfirmedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity implements OnDeleteConfirmedListener {

    public static final String PACKAGE_NAME = "com.zezo.music";
    public static final String KEY_DIRECTORY_SELECTED = MusicPlayerActivity.PACKAGE_NAME + ".DIRECTORY_SELECTED";

    private TabPagerAdapter tabPagerAdapter;
    private ViewPager viewPager;
    private SharedPreferences sharedPreferences;
    private MusicService musicService;
    private ArrayList<Song> playlist = new ArrayList<Song>();
    private Intent musicServiceIntent;
    private Menu optionsMenu;

    private ServiceConnection musicServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();
            musicService.setPlaylist(playlist);
            tabPagerAdapter.getNowPlayingFragment().initController(musicService);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            tabPagerAdapter.getNowPlayingFragment().unbindController();

        }
    };

    private BroadcastReceiver onMediaPlayerPlayingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context c, Intent i) {

            if (i.getAction() != "MEDIA_PLAYER_PLAYING")
                return;

            Song song = musicService.getCurrentSong();

            tabPagerAdapter.getNowPlayingFragment().setCurrentSong(song);

            if (viewPager.getCurrentItem() == 1) {

                PlaylistFragment playlistFragment = (PlaylistFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
                playlistFragment.setCurrentSong(song);

            }

            musicService.setPlayerPrepared(true);

            if (viewPager.getCurrentItem() == Tabs.NOWPLAYING.ordinal())
                tabPagerAdapter.getNowPlayingFragment().updateController();

            tabPagerAdapter.getQueueFragment().setQueue(musicService.getQueue());

        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        String musicFolder = sharedPreferences.getString(KEY_DIRECTORY_SELECTED,
                Environment.getExternalStorageDirectory().toString());

        playlist = getAllSongsInFolder(musicFolder);

        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), this);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setCurrentItem(Tabs.PLAYLIST.ordinal());
        viewPager.setOffscreenPageLimit(4);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == Tabs.NOWPLAYING.ordinal())
                    tabPagerAdapter.getNowPlayingFragment().show();
                else
                    tabPagerAdapter.getNowPlayingFragment().hide();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initMusicService();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        optionsMenu = menu;
        return true;

    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            final MenuItem menuItem = optionsMenu.findItem(R.id.search);
            View actionView = menuItem.getActionView();
            final SearchView searchView = (SearchView) actionView;
            searchView.setQuery(query, false);
        }
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

                /*MediaExtractor mex = new MediaExtractor();

                try {

                    AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(song.getUri(), "r");
                    mex.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int count = mex.getTrackCount();

                MediaFormat mf = mex.getTrackFormat(0);
                song.setSampleRate(mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));*/

                songs.add(song);

            } while (musicCursor.moveToNext());

        }

        musicCursor.close();
        return songs;

    }

    private void initMusicService() {

        LocalBroadcastManager.getInstance(this).registerReceiver(onMediaPlayerPlayingReceiver,
                new IntentFilter("MEDIA_PLAYER_PLAYING"));

        if (musicServiceIntent == null) {

            musicServiceIntent = new Intent(this, MusicService.class);
            bindService(musicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
            startService(musicServiceIntent);

        }

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDeleteConfirmed(long songId) {

        Song song = musicService.getSongById(songId);

        if (song == musicService.getCurrentSong())
            return;

        tabPagerAdapter.getPlaylistFragment().remove(song);

        //Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());

        getContentResolver().delete(song.getUri(), null, null);

        musicService.removeFromPlaylist(song);

    }

    @Override
    protected void onDestroy() {

        // hideKeyboard();
        unbindService(musicServiceConnection);
        stopService(musicServiceIntent);
        musicService = null;

        super.onDestroy();

    }

    public void onOpenContextMenu(View view) {
        openContextMenu(view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_set_folder:

                FoldersFragment browser = tabPagerAdapter.getBrowserFragment();
                String musicFolderPath = browser.getCurrentFolderPath();

                Toast.makeText(MusicPlayerActivity.this, "Selected music folder:" + musicFolderPath, Toast.LENGTH_SHORT)
                        .show();

                playlist = getAllSongsInFolder(musicFolderPath);
                tabPagerAdapter.getPlaylistFragment().loadPlaylist(playlist);
                musicService.setPlaylist(playlist);
                sharedPreferences.edit().putString(KEY_DIRECTORY_SELECTED, musicFolderPath).commit();
                return true;

            case R.id.action_shuffle:

                toggleShuffle();
                return true;

            case R.id.action_exit:

                showExitDialog();
                return true;

        }

        return false;

    }

    public void play(Song song) {

        if (song != null && musicService.audioFocusGranted()) {
            musicService.playSong(song);
            Toast.makeText(this, "Playing.", Toast.LENGTH_SHORT).show();
        }

    }

    private void toggleShuffle() {

        musicService.toggleShuffle();
        updateShuffleIcon();

    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public void scrollToCurrent(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            tabPagerAdapter.getPlaylistFragment().scrollToCurrent();

    }

    private void exit() {

        // hideKeyboard();
        unbindService(musicServiceConnection);
        stopService(musicServiceIntent);
        musicService = null;
        System.exit(0);

    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(ArrayList<Song> playlist) {

        this.playlist = playlist;

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

        if (item == null)
            return;

        int shuffleIconIndex = R.drawable.shufflegrey40;

        if (isShuffling())
            shuffleIconIndex = R.drawable.shufflewhite40;

        Drawable shuffleIcon = getResources().getDrawable(shuffleIconIndex);
        item.setIcon(shuffleIcon);

    }

}