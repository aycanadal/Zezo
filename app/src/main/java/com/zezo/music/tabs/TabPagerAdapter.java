package com.zezo.music.tabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.zezo.music.R;
import com.zezo.music.tabs.folders.FoldersFragment;
import com.zezo.music.tabs.nowplaying.NowPlayingFragment;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.tabs.queue.QueueFragment;

public class TabPagerAdapter extends FragmentPagerAdapterExt implements ViewPager.OnPageChangeListener {

    private final Context context;

    // Order has to match with tab indexes.
    public enum Tabs {

        NOWPLAYING, PLAYLIST, QUEUE, FOLDERS

    }

    public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

        super(fragmentManager);
        this.context = context;

    }

    @Override
    public Fragment getItem(int tabIndex) {

        Tabs tabName = Tabs.values()[tabIndex];

        switch (tabName) {

            case NOWPLAYING:
                return new NowPlayingFragment();
            case PLAYLIST:
                return new PlaylistFragment();
            case QUEUE:
                return new QueueFragment();
            case FOLDERS:
                return new FoldersFragment();

        }

        return null;

    }

    @Override
    public int getCount() {

        return Tabs.values().length;

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

        return (FoldersFragment) getFragments().get(Tabs.FOLDERS.ordinal());

    }

    public PlaylistFragment getPlaylistFragment() {

        return (PlaylistFragment) getFragments().get(Tabs.PLAYLIST.ordinal());

    }

    public QueueFragment getQueueFragment() {

        return (QueueFragment) getFragments().get(Tabs.QUEUE.ordinal());

    }

    public NowPlayingFragment getNowPlayingFragment() {

        return (NowPlayingFragment) getFragments().get(Tabs.NOWPLAYING.ordinal());

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        NowPlayingFragment nowPlayingFragment = getNowPlayingFragment();

        if (nowPlayingFragment == null)
            return;

        if (position == Tabs.NOWPLAYING.ordinal())
            getNowPlayingFragment().show();
        else
            getNowPlayingFragment().hide();

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}