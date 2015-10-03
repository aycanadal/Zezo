package com.zezo.music;

import com.zezo.music.tabs.browser.Browser;
import com.zezo.music.tabs.playlist.Playlist;
import com.zezo.music.tabs.queue.Queue;

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

		tabs.put(Tab.FOLDERS.ordinal(), new Browser());
		tabs.put(Tab.PLAYLIST.ordinal(), new Playlist());
		tabs.put(Tab.QUEUE.ordinal(), new Queue());
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

	public Browser getBrowserFragment() {

		return (Browser) tabs.get(Tab.FOLDERS.ordinal());

	}

	public Playlist getPlaylistFragment() {

		return (Playlist) tabs.get(Tab.PLAYLIST.ordinal());

	}

}