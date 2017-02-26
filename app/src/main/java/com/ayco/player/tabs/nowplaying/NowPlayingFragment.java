package com.ayco.player.tabs.nowplaying;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ayco.player.MusicPlayerActivity;
import com.ayco.player.R;
import com.ayco.player.shared.Song;

public class NowPlayingFragment extends Fragment {

    private TextView currentArtistView;
    private TextView currentTitleView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d("NowPlaying Lifecycle", "onCreate");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("NowPlaying Lifecycle", "onCreateView");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.nowplaying, container, false);
        currentTitleView = (TextView) view.findViewById(R.id.currentTitle);
        currentArtistView = (TextView) view.findViewById(R.id.currentArtist);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.nowplaying, menu);
        super.onCreateOptionsMenu(menu, inflater);
        ((MusicPlayerActivity) getActivity()).updateShuffleIcon();

    }

    public void setInfo(Song song) {

        currentArtistView.setText(song.getArtist());
        currentTitleView.setText(song.getTitle());

    }

}
