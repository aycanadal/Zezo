package com.zezo.music.tabs;

import com.zezo.music.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {

	private final Context context;
	
	private final PlaylistFragment playlistFragment;

	public TabPagerAdapter(FragmentManager fragmentManager, Context context) {

		super(fragmentManager);
		this.context = context;
		
		playlistFragment = new PlaylistFragment();
		playlistFragment.setRetainInstance(true);

	}

	@Override
	public Fragment getItem(int i) {

		switch (i) {

		case 0:
			return new Browser();
		case 1:
			return playlistFragment;
		case 2:
			return new PlayQueue();
			
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
			return context.getString(R.string.Browser);
		case 1:
			return context.getString(R.string.Playlist);
		case 2:
			return context.getString(R.string.Queue);
			
		}

		return null;
	}

	public PlaylistFragment getPlaylistFragment() {
		return playlistFragment;
	}

}