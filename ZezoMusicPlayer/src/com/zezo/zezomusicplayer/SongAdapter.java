package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.List;

import com.zezo.dragndroplistview.DropListener;
import com.zezo.dragndroplistview.RemoveListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter implements Filterable,
		RemoveListener, DropListener {

	private ArrayList<Song> songs;
	private ArrayList<Song> filteredSongs;
	private LayoutInflater songInf;

	public SongAdapter(Context c, ArrayList<Song> theSongs) {
		songs = theSongs;
		filteredSongs = theSongs;
		songInf = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {

		return filteredSongs.size();
	}

	@Override
	public Song getItem(int arg0) {
		return filteredSongs.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return filteredSongs.get(arg0).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// map to song layout
		LinearLayout songLayout = (LinearLayout) songInf.inflate(R.layout.song,
				parent, false);

		// get title and artist views
		TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
		TextView artistView = (TextView) songLayout
				.findViewById(R.id.song_artist);

		// get song using position
		Song currSong = filteredSongs.get(position);

		// get title and artist strings
		songView.setText(currSong.getTitle());
		artistView.setText(currSong.getArtist());

		// set position as tag
		songLayout.setTag(position);

		return songLayout;

	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence charSequence) {
				FilterResults results = new FilterResults();

				if (charSequence == null || charSequence.length() == 0) {

					results.values = songs;
					results.count = songs.size();

				} else {

					List<Song> filteredSongs = new ArrayList<Song>();

					for (Song song : songs) {

						String songTitle = song.getTitle();

						if (songTitle.toUpperCase().contains(
								charSequence.toString().toUpperCase()))
							filteredSongs.add(song);

					}

					results.values = filteredSongs;
					results.count = filteredSongs.size();

				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence charSequence,
					FilterResults filterResults) {

				filteredSongs = (ArrayList<Song>) filterResults.values;
				notifyDataSetChanged();

			}
		};
	}

	public void onRemove(int songIndex) {

		if (songIndex < 0 || songIndex > songs.size())
			return;

		songs.remove(songIndex);

	}

	public void onDrop(int from, int to) {

		Song temp = songs.get(from);
		songs.remove(from);
		songs.add(to, temp);

	}
}
