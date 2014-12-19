package com.zezo.zezomusicplayer;

import java.util.ArrayList;
import java.util.List;

import android.widget.Filter;

public class SongFilter extends Filter {

	private SongAdapter songAdapter;

	public SongFilter(SongAdapter songAdapter) {

		this.songAdapter = songAdapter;

	}

	@Override
	protected FilterResults performFiltering(CharSequence charSequence) {

		FilterResults results = new FilterResults();

		if (charSequence == null || charSequence.length() == 0) {

			results.values = songAdapter.getSongs();
			results.count = songAdapter.getSongs().size();

		} else {

			List<Song> filteredSongs = new ArrayList<Song>();

			for (Song song : songAdapter.getSongs()) {

				String songTitle = song.getTitle();
				String songArtist = song.getArtist();

				String songInfo = songArtist + songTitle;

				String[] words = charSequence.toString().split(" ");

				boolean isMatch = true;

				for (String word : words) {

					if (!songInfo.toUpperCase().contains(word.toUpperCase())) {
						isMatch = false;
						break;
					}

				}

				if (isMatch)
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

		songAdapter.setFilteredSongs((ArrayList<Song>) filterResults.values);
		songAdapter.notifyDataSetChanged();

	}

}
