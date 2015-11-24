package com.zezo.music.tabs.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.zezo.music.R;
import com.zezo.music.domain.Song;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter implements Filterable {

    private long checkedItemId;
    private Filter filter;
    private ArrayList<Song> filteredSongs;
    private LayoutInflater songInflater;
    private ArrayList<Song> songs;
    private SongClickListener songClickListener;

    public interface SongClickListener {

        void onSongClicked(Song song);

    }

    public PlaylistAdapter(Context c, ArrayList<Song> songs, SongClickListener songClickListener) {

        setSongs(songs);
        setFilteredSongs(songs);
        songInflater = LayoutInflater.from(c);
        filter = new PlaylistSearchFilter(this);
        this.songClickListener = songClickListener;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Song song = getFilteredSongs().get(position);

        LinearLayout songLayout = (LinearLayout) songInflater.inflate(R.layout.song, parent, false);
        TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLayout.findViewById(R.id.song_artist);
        TextView durationView = (TextView) songLayout.findViewById(R.id.songDuration);
        songView.setText(song.getTitle());
        artistView.setText(song.getArtist());
        durationView.setText(song.getDuration());

        songLayout.setTag(song.getId());

        long itemId = getItemId(position);

        ((ListView) parent).setItemChecked(position, false);

        if (checkedItemId > 0 && checkedItemId == itemId)
            ((ListView) parent).setItemChecked(position, true);

        LinearLayout songClickableArea = (LinearLayout) songLayout.findViewById(R.id.song);
        songClickableArea.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                View item = (View) view.getParent();
                int songIndex = Integer.parseInt(item.getTag().toString());
                Song song = getItem(songIndex);
                songClickListener.onSongClicked(song);

            }

        });

        return songLayout;

    }

    @Override
    public Song getItem(int songId) {

        for (Song song : filteredSongs) {

            if (song.getId() == songId)
                return song;

        }

        return null;

    }

    @Override
    public int getCount() {

        return getFilteredSongs().size();
    }

    @Override
    public Filter getFilter() {

        return filter;

    }

    @Override
    public long getItemId(int position) {

        return getFilteredSongs().get(position).getId();

    }

    public ArrayList<Song> getFilteredSongs() {

        return filteredSongs;

    }

    public ArrayList<Song> getSongs() {

        return songs;

    }

    public void setFilteredSongs(ArrayList<Song> filteredSongs) {

        this.filteredSongs = filteredSongs;

    }

    public void setItemChecked(long id) {

        checkedItemId = id;

    }

    public void setSongs(ArrayList<Song> songs) {

        this.songs = songs;

    }
}
