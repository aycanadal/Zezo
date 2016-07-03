package com.zezo.music.tabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.zezo.music.R;
import com.zezo.music.tabs.folders.FoldersFragment;
import com.zezo.music.tabs.nowplaying.NowPlayingFragment;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.tabs.queue.QueueFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {

    private final Context context;

    // Order has to match with tab indexes.
    public enum Tabs {

        NOWPLAYING, PLAYLIST, QUEUE, FOLDERS

    }

    private SparseArray<Fragment> tabs = new SparseArray<Fragment>();

    public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

        super(fragmentManager);
        this.context = context;

        NowPlayingFragment nowPlayingFragment = new NowPlayingFragment();
        PlaylistFragment playlistFragment = new PlaylistFragment();
        nowPlayingFragment.setNowPlayingClickListener(playlistFragment);
        nowPlayingFragment.setRetainInstance(true);
        tabs.put(Tabs.NOWPLAYING.ordinal(), nowPlayingFragment);
        tabs.put(Tabs.FOLDERS.ordinal(), new FoldersFragment());
        tabs.put(Tabs.PLAYLIST.ordinal(), playlistFragment);
        tabs.put(Tabs.QUEUE.ordinal(), new QueueFragment());
    }

    @Override
    public Fragment getItem(int tabIndex) {

        return tabs.get(tabIndex);

    }

    @Override
    public int getCount() {

        return tabs.size();

    }

    @Override
    public CharSequence getPageTitle(int position) {

        Tabs tab = Tabs.values()[position];

        switch (tab) {

            case NOWPLAYING:
                return context.getString(R.string.NowPlaying);
            case PLAYLIST:
                return context.getString(R.string.Playlist);
            case QUEUE:
                return context.getString(R.string.Queue);
            case FOLDERS:
                return context.getString(R.string.Folders);

        }

        return null;
    }

    public FoldersFragment getBrowserFragment() {

        return (FoldersFragment) tabs.get(Tabs.FOLDERS.ordinal());

    }

    public PlaylistFragment getPlaylistFragment() {

        return (PlaylistFragment) tabs.get(Tabs.PLAYLIST.ordinal());

    }

    public QueueFragment getQueueFragment() {

        return (QueueFragment) tabs.get(Tabs.QUEUE.ordinal());

    }

    public NowPlayingFragment getNowPlayingFragment() {

        return (NowPlayingFragment) tabs.get(Tabs.NOWPLAYING.ordinal());

    }
}