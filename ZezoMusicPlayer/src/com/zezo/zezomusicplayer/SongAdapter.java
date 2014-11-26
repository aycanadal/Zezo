package com.zezo.zezomusicplayer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zezo.dragndroplistview.DropListener;
import com.zezo.dragndroplistview.RemoveListener;

public class SongAdapter extends BaseAdapter implements Filterable,
		RemoveListener, DropListener {

	private Filter filter;
	private ArrayList<Song> filteredSongs;
	private LayoutInflater songInflater;
	private ArrayList<Song> songs;

	public SongAdapter(Context c, ArrayList<Song> theSongs) {

		setSongs(theSongs);
		setFilteredSongs(theSongs);
		songInflater = LayoutInflater.from(c);
		filter = new SongFilter(this);

	}

	@Override
	public int getCount() {

		return getFilteredSongs().size();
	}

	@Override
	public Filter getFilter() {

		return filter;
	}

	public ArrayList<Song> getFilteredSongs() {
		return filteredSongs;
	}

	@Override
	public Song getItem(int arg0) {
		return getFilteredSongs().get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return getFilteredSongs().get(arg0).getId();
	}

	public ArrayList<Song> getSongs() {
		return songs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// map to song layout
		LinearLayout songLayout = (LinearLayout) songInflater.inflate(
				R.layout.song, parent, false);

		// get title and artist views
		TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
		TextView artistView = (TextView) songLayout
				.findViewById(R.id.song_artist);

		// get song using position
		Song currentSong = getFilteredSongs().get(position);

		// get title and artist strings
		songView.setText(currentSong.getTitle());
		artistView.setText(currentSong.getArtist());

		// set position as tag
		songLayout.setTag(position);
		
		return songLayout;

	}

	@Override
	public void onDrag(int songIndex) {

		if (songIndex < 0 || songIndex > getSongs().size())
			return;

		getSongs().remove(songIndex);

	}

	@Override
	public void onDrop(int from, int to) {

		Song temp = getSongs().get(from);
		getSongs().remove(from);
		getSongs().add(to, temp);

	}

	public void setFilteredSongs(ArrayList<Song> filteredSongs) {
		this.filteredSongs = filteredSongs;
	}

	public void setSongs(ArrayList<Song> songs) {
		this.songs = songs;
	}

	/*
	 * @Override public boolean isEnabled(int position) { return true; }
	 */
}
