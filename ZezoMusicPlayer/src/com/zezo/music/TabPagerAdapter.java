package com.zezo.music;

import com.zezo.music.tabs.browser.Browser;
import com.zezo.music.tabs.playlist.Playlist;
import com.zezo.music.tabs.queue.Queue;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {

	private final Context context;

	private final Playlist playlist;
	private final Browser browser;

	public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

		super(fragmentManager);
		this.context = context;

		playlist = new Playlist();
		playlist.setRetainInstance(true);

		browser = new Browser();
		browser.setRetainInstance(true);

	}

	@Override
	public Fragment getItem(int i) {

		switch (i) {

		case 0:
			return browser;
		case 1:
			return playlist;
		case 2:
			return new Queue();

		}

		return null;

	}

	@Override
	public int getCount() {

		return 3; // Number of tabs.

	}

	@Override
	public CharSequence getPageTitle(int position) {

		switch (position) {

		case 0:
			return context.getString(R.string.Folders);
		case 1:
			return context.getString(R.string.Playlist);
		case 2:
			return context.getString(R.string.Queue);

		}

		return null;
	}

	public Playlist getPlaylistFragment() {
		return playlist;
	}

	public Browser getBrowserFragment() {
		return browser;
	}

}