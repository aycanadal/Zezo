package com.zezo.music;

import com.zezo.music.domain.Song;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.nowplaying, container, false);

		LinearLayout nowPlayingFrame = (LinearLayout) view.findViewById(R.id.nowPlaying);

		nowPlayingFrame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nowPlayingClickListener.onNowPlayingClicked();
			}
		});

		currentTitleView = (TextView) view.findViewById(R.id.currentTitle);
		currentArtistView = (TextView) view.findViewById(R.id.currentArtist);
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

	public void setCurrentSong(Song song) {

		currentArtistView.setText(song.getArtist());
		currentTitleView.setText(song.getTitle());

	}

	public void hide() {

		musicController.setVisibility(View.GONE);
		musicController.hideSuper();
		controllerFrame.setVisibility(View.GONE);
		getActivity().getSupportFragmentManager().beginTransaction().hide(this).commit();

	}

	public void show() {

		musicController.show(0);
		musicController.setVisibility(View.VISIBLE);
		controllerFrame.setVisibility(View.VISIBLE);
		getActivity().getSupportFragmentManager().beginTransaction().show(this).commit();

	}

	public void updateController() {

		musicController.show(0);

	}

}
