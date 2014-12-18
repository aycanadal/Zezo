package com.zezo.zezomusicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class MusicController extends MediaController implements
		MediaPlayerControl {

	private boolean musicBound = false;
	private MusicService musicService;

	// public MusicController(Context context, AttributeSet attributeSet){
	//
	// super(context, attributeSet);
	//
	// }

	public MusicController(Context context) {

		super(context);

	}

	@Override
	public void hide() {

	}

	public void hideSuper() {

		super.hide();

	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getAudioSessionId() {
		return 0;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (musicService != null && isMusicBound() && musicService.isPng())
			return musicService.getPosition();
		else if (musicService != null && isMusicBound())
			return musicService.getPausePosition();
		else
			return 0;
	}

	@Override
	public int getDuration() {

		if (musicService != null && isMusicBound() && musicService.isPng())
			return musicService.getDuration();

		else if (musicService != null && isMusicBound())
			return musicService.getPauseDuration();

		else
			return 0;

	}

	public void init(MusicService musicService) {

		this.musicService = musicService;

		setPrevNextListeners(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});

		setMediaPlayer(this);
		setEnabled(true);

	}

	public boolean isMusicBound() {
		return musicBound;
	}

	@Override
	public boolean isPlaying() {
		if (musicService != null & isMusicBound())
			return musicService.isPng();
		return false;
	}

	@Override
	public void pause() {

		if (musicService != null && musicService.isPng())
			musicService.pause();

		show(0);
		setVisibility(View.VISIBLE);
	}

	private void playNext() {
		musicService.playNext();
		// show(0);
	}

	private void playPrev() {
		musicService.playPrevious();
		// show(0);
	}

	@Override
	public void seekTo(int pos) {

		musicService.seek(pos);

		if (!musicService.isPng())
			musicService.setPausePosition(pos);

	}

	public void setMusicBound(boolean musicBound) {
		this.musicBound = musicBound;
	}

	@Override
	public void start() {

		musicService.play();
		// show(0);

	}

}