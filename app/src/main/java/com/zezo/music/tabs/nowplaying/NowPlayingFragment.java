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

public class NowPlayingFragment extends Fragment {

    private TextView currentArtistView;
    private TextView currentTitleView;
    private FrameLayout controllerFrame;
    private MusicController musicController;
    private NowPlayingClickListener nowPlayingClickListener;

    public interface NowPlayingClickListener {

        public void onNowPlayingClicked();

    }

    public void setNowPlayingClickListener(NowPlayingClickListener nowPlayingClickListener) {

        this.nowPlayingClickListener = nowPlayingClickListener;

    }

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

        LinearLayout nowPlayingFrame = (LinearLayout) view.findViewById(R.id.nowPlaying);

        nowPlayingFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nowPlayingClickListener.onNowPlayingClicked();
            }
        });

        currentTitleView = (TextView) view.findViewById(R.id.currentTitle);
        currentArtistView = (TextView) view.findViewById(R.id.currentArtist);
        controllerFrame = (FrameLayout) view.findViewById(R.id.controllerFrame);
        musicController = new MusicController(getActivity());
        musicController.setAnchorView(controllerFrame);

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

        if (musicController == null)
            return;

        musicController.init(musicService);
        musicController.setMusicBound(true);

    }

    public void unbindController() {

        musicController.setMusicBound(false);

    }

    public void setInfo(Song song) {

        currentArtistView.setText(song.getArtist());
        currentTitleView.setText(song.getTitle());

    }

    public void hide() {

        musicController.setVisibility(View.GONE);
        musicController.hideSuper();
        controllerFrame.setVisibility(View.GONE);

    }

    @Override
    public void onPause() {

        Log.d("NowPlaying Lifecycle", "onPause");
        super.onPause();
        hide();
    }

    public void show() {

        if (musicController == null)
            return;

        musicController.show(0);
        musicController.setVisibility(View.VISIBLE);
        musicController.setFocusable(false);
        musicController.setFocusableInTouchMode(false);
        controllerFrame.setVisibility(View.VISIBLE);

    }

    public void updateController() {

        musicController.show(0);

    }

}
