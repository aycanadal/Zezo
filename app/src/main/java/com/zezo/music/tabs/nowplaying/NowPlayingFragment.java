package com.zezo.music.tabs.nowplaying;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zezo.music.MusicController;
import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.MusicService;
import com.zezo.music.R;
import com.zezo.music.domain.Song;
import com.zezo.music.tabs.playlist.MediaControllerFragment;

public class NowPlayingFragment extends Fragment {

    private TextView currentArtistView;
    private TextView currentTitleView;
    private MediaControllerFragment mediaControllerFragment;

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
        mediaControllerFragment = (MediaControllerFragment) getChildFragmentManager().findFragmentById(R.id.playlistBottomPane);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();
        inflater.inflate(R.menu.nowplaying, menu);
        super.onCreateOptionsMenu(menu, inflater);
        ((MusicPlayerActivity) getActivity()).updateShuffleIcon();

    }

    public void initController(MusicService musicService) {

        mediaControllerFragment.initController(musicService);

    }

    public void unbindController() {

        mediaControllerFragment.unbindController();

    }

    public void setInfo(Song song) {

        currentArtistView.setText(song.getArtist());
        currentTitleView.setText(song.getTitle());

    }

    public void hide() {

        mediaControllerFragment.hide();

    }

    @Override
    public void onPause() {

        Log.d("NowPlaying Lifecycle", "onPause");
        super.onPause();
        hide();
    }

    public void show() {

        mediaControllerFragment.show();
    }

}
