package com.zezo.music.tabs;

import com.zezo.music.R;
import com.zezo.music.tabs.folders.FoldersFragment;
import com.zezo.music.tabs.nowplaying.NowPlayingFragment;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.tabs.queue.QueueFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

public class TabPagerAdapter extends FragmentPagerAdapter {

	private final Context context;

	// Order has to match with tab indexes.
	public enum Tab {

		NOWPLAYING, PLAYLIST, QUEUE, FOLDERS

	};

	private SparseArray<Fragment> tabs = new SparseArray<Fragment>();

	public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

		super(fragmentManager);
		this.context = context;

		tabs.put(Tab.NOWPLAYING.ordinal(), new NowPlayingFragment());
		tabs.put(Tab.FOLDERS.ordinal(), new FoldersFragment());
		tabs.put(Tab.PLAYLIST.ordinal(), new PlaylistFragment());
		tabs.put(Tab.QUEUE.ordinal(), new QueueFragment());
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

		Tab tab = Tab.values()[position];

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

		return (FoldersFragment) tabs.get(Tab.FOLDERS.ordinal());

	}

	public PlaylistFragment getPlaylistFragment() {

		return (PlaylistFragment) tabs.get(Tab.PLAYLIST.ordinal());

	}

	public QueueFragment getQueueFragment() {

		return (QueueFragment) tabs.get(Tab.QUEUE.ordinal());

	}

	public NowPlayingFragment getNowPlayingFragment() {

		return (NowPlayingFragment) tabs.get(Tab.NOWPLAYING.ordinal());

	}
}