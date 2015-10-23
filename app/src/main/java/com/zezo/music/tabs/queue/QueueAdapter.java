package com.zezo.music.tabs.queue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zezo.music.R;
import com.zezo.music.domain.Song;

import java.util.ArrayList;

public class QueueAdapter extends BaseAdapter {

    private ArrayList<Song> queue = new ArrayList<Song>();
    private LayoutInflater songInflater;

    public QueueAdapter(Context c) {

        songInflater = LayoutInflater.from(c);

    }

    @Override
    public int getCount() {

        return queue.size();

    }

    @Override
    public Object getItem(int position) {

        return queue.get(position);

    }

    @Override
    public long getItemId(int position) {

        return queue.get(position).getId();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Song song = queue.get(position);

        LinearLayout songLayout = (LinearLayout) songInflater.inflate(R.layout.song, parent, false);
        TextView songView = (TextView) songLayout.findViewById(R.id.song_title);
        TextView artistView = (TextView) songLayout.findViewById(R.id.song_artist);
        TextView durationView = (TextView) songLayout.findViewById(R.id.songDuration);
        songView.setText(song.getTitle());
        artistView.setText(song.getArtist());
        durationView.setText(song.getDuration());
        songLayout.setTag(song.getId());
        return songLayout;

    }

    public void addToQueue(Song song) {

        queue.add(song);
        notifyDataSetChanged();

    }

    public void removeFromQueue(Song song) {

        queue.remove(song);
        notifyDataSetChanged();

    }

    public ArrayList<Song> getQueue() {
        return queue;
    }

    public void setQueue(ArrayList<Song> queue) {
        this.queue = queue;
    }

}
