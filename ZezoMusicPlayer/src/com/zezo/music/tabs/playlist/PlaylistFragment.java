package com.zezo.music.tabs.playlist;

import java.util.ArrayList;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.NowPlayingFragment.NowPlayingClickListener;
import com.zezo.music.R;
import com.zezo.music.SearchFragment;
import com.zezo.music.SearchFragment.SearchListener;
import com.zezo.music.domain.Song;
import com.zezo.music.tabs.playlist.PlaylistAdapter.SongClickListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

public class PlaylistFragment extends Fragment implements SongClickListener, NowPlayingClickListener, SearchListener {

	private ListView songListView;
	private PlaylistAdapter playlistAdapter;
	private SearchFragment searchFragment;
	private Menu optionsMenu;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		playlistAdapter = new PlaylistAdapter(getActivity(), ((MusicPlayerActivity)activity).getPlaylist(), this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		setHasOptionsMenu(true);

		View playlistView = inflater.inflate(R.layout.playlist, container, false);

		songListView = (ListView) playlistView.findViewById(R.id.song_list);
		songListView.setAdapter(playlistAdapter);
		registerForContextMenu(songListView);

		searchFragment = (SearchFragment) getChildFragmentManager().findFragmentById(R.id.search);
		getChildFragmentManager().beginTransaction().hide(searchFragment).commit();

		return playlistView;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		optionsMenu = menu;
		optionsMenu.clear();
		inflater.inflate(R.menu.playlist, optionsMenu);
		super.onCreateOptionsMenu(optionsMenu, inflater);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_search:

			if (searchFragment.isVisible()) {

				hideSearch();

			} else {

				showSearch();

			}

			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	public void setCurrentSong(Song song) {

		playlistAdapter.setItemChecked(song.getId());
		songListView.setItemChecked(playlistAdapter.getFilteredSongs().indexOf(song), true);

	}

	public void remove(Song song) {

		playlistAdapter.getSongs().remove(song);
		playlistAdapter.getFilteredSongs().remove(song);
		playlistAdapter.notifyDataSetChanged();

	}

	public void loadPlaylist(ArrayList<Song> songs) {

		playlistAdapter = new PlaylistAdapter(getActivity(), songs, this);
		songListView.setAdapter(playlistAdapter);

	}

	public void scrollToCurrent() {

		songListView.requestFocusFromTouch();
		songListView.setSelection(songListView.getCheckedItemPosition());

	}

	@Override
	public void onSongClicked(Song song) {

		Activity activity = getActivity();

		if (activity instanceof MusicPlayerActivity)
			((MusicPlayerActivity) activity).play(song);

	}

	private void showSearch() {

		((MusicPlayerActivity) getActivity()).hideNowPlaying();

		searchFragment.show(getChildFragmentManager(),
				(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

	}

	private void hideSearch() {

		searchFragment.hide(getChildFragmentManager(),
				(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));

	}

	@Override
	public void onSearchTextChanged(CharSequence cs) {

		playlistAdapter.getFilter().filter(cs);

	}

	public void setShuffle(boolean shuffle) {

		Drawable shuffleIcon;
		MenuItem item = optionsMenu.findItem(R.id.action_shuffle);

		if (shuffle) {

			shuffleIcon = getResources().getDrawable(R.drawable.shufflegrey40);
			Toast.makeText(getActivity(), "Shuffle is now off.", Toast.LENGTH_SHORT).show();

		} else {

			shuffleIcon = getResources().getDrawable(R.drawable.shufflewhite40);
			Toast.makeText(getActivity(), "Shuffle is now on.", Toast.LENGTH_SHORT).show();

		}

		item.setIcon(shuffleIcon);

	}

	@Override
	public void onNowPlayingClicked() {

		scrollToCurrent();
		
	}

}