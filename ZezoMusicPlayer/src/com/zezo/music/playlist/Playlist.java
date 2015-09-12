package com.zezo.music.playlist;

import java.util.ArrayList;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.PlaylistAdapter;
import com.zezo.music.PlaylistAdapter.SongClickListener;
import com.zezo.music.R;
import com.zezo.music.SearchFragment.SearchListener;
import com.zezo.music.domain.Song;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Playlist extends Fragment implements SongClickListener, SearchListener {

	private ListView songListView;
	private PlaylistAdapter songAdapter;
	private ArrayList<Song> initialPlaylist;

	public void onAttach(Activity activity) {

		super.onAttach(activity);
		songAdapter = new PlaylistAdapter(getActivity(), initialPlaylist, this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View playlistView = inflater.inflate(R.layout.playlist, container, false);

		songListView = (ListView) playlistView.findViewById(R.id.song_list);
		songListView.setAdapter(songAdapter);
		registerForContextMenu(songListView);

		return playlistView;

	}
	
	public void setCurrentSong(Song song) {

		songAdapter.setItemChecked(song.getId());
		songListView.setItemChecked(songAdapter.getFilteredSongs().indexOf(song), true);

	}

	public void remove(Song song) {

		songAdapter.getSongs().remove(song);
		songAdapter.getFilteredSongs().remove(song);
		songAdapter.notifyDataSetChanged();

	}

	public void setPlaylist(ArrayList<Song> songs) {

		initialPlaylist = songs;

	}

	public void loadPlaylist(ArrayList<Song> songs) {

		songAdapter = new PlaylistAdapter(getActivity(), songs, this);
		songListView.setAdapter(songAdapter);

	}

	public void onSearchTextChanged(CharSequence cs) {

		songAdapter.getFilter().filter(cs);

	}

	public void scrollToCurrent() {

			songListView.setSelection(songListView.getCheckedItemPosition());

	}

	@Override
	public void songClicked(Song song) {

		Activity activity = getActivity();

		if (activity instanceof MusicPlayerActivity)
			((MusicPlayerActivity) activity).play(song);

		
	}

}