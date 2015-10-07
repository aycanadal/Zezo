package com.zezo.music;

import com.zezo.music.tabs.folders.FoldersFragment;
import com.zezo.music.tabs.playlist.PlaylistFragment;
import com.zezo.music.tabs.queue.QueueFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

public class TabPagerAdapter extends FragmentPagerAdapter {

	private final Context context;

	public enum Tab {

		FOLDERS, PLAYLIST, QUEUE

	};

	private SparseArray<Fragment> tabs = new SparseArray<Fragment>();

	public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

		super(fragmentManager);
		this.context = context;

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

		case FOLDERS:
			return context.getString(R.string.Folders);
		case PLAYLIST:
			return context.getString(R.string.Playlist);
		case QUEUE:
			return context.getString(R.string.Queue);

		}

		return null;
	}

	public FoldersFragment getBrowserFragment() {

		return (FoldersFragment) tabs.get(Tab.FOLDERS.ordinal());

	}

	public PlaylistFragment getPlaylistFragment() {

		return (PlaylistFragment) tabs.get(Tab.PLAYLIST.ordinal());

	}

}