package com.zezo.music.tabs.playlist;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zezo.music.MusicController;
import com.zezo.music.MusicService;
import com.zezo.music.R;

public class PlaylistBottomPaneFragment extends Fragment {

    private FrameLayout controllerFrame;
    private MusicController musicController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nowplaying, container, false);
        controllerFrame = (FrameLayout) view.findViewById(R.id.controllerFrame);
        musicController = new MusicController(getActivity());
        musicController.setAnchorView(controllerFrame);
        return view;
    }

    public void initController(MusicService musicService) {

        musicController.init(musicService);
        musicController.setMusicBound(true);
        musicController.show(0);

    }

    public void unbindController() {

        musicController.setMusicBound(false);

    }

    public void hide() {

        musicController.setVisibility(View.GONE);
        musicController.hideSuper();
        controllerFrame.setVisibility(View.GONE);
        getChildFragmentManager().beginTransaction().hide(this).commit();

    }

    public void show() {

        musicController.show(0);
        musicController.setVisibility(View.VISIBLE);
        controllerFrame.setVisibility(View.VISIBLE);
        getChildFragmentManager().beginTransaction().show(this).commit();

    }

}