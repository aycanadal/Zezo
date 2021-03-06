package com.zezo.music.tabs.playlist;

import android.widget.Filter;

import com.zezo.music.shared.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSearchFilter extends Filter {

    private PlaylistAdapter songAdapter;

    public PlaylistSearchFilter(PlaylistAdapter songAdapter) {

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
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        songAdapter.setFilteredSongs((ArrayList<Song>) filterResults.values);
        songAdapter.notifyDataSetChanged();

    }

}
