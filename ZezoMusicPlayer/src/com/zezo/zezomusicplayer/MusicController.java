package com.zezo.zezomusicplayer;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class MusicController extends MediaController implements
		MediaPlayerControl {

	private boolean musicBound = false;
	private MusicService musicService;

	public MusicController(Context context) {

		super(context);

	}
	
	@Override
	public void hide() {
		
		// Do nothing.
				
	};

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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (musicService != null && isMusicBound() && musicService.isPng())
			// if (musicSrv != null && musicBound)
			return musicService.getPosition();
		else if (musicService != null && isMusicBound())
			return musicService.getPausePosition();
		else
			return 0;
	}

	@Override
	public int getDuration() {

		// Log.d(musicSrv.);

		if (musicService != null && isMusicBound() && musicService.isPng())
			// if (musicSrv != null && musicBound)
			return musicService.getDuration();
		else if (musicService != null && isMusicBound()) {
			int pauseDuration = musicService.getPauseDuration();
			return musicService.getPauseDuration();
		} else
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

		// initController();
		//show(0);
	}

	private void playNext() {
		musicService.playNext();
		//show(0);
	}

	private void playPrev() {
		musicService.playPrevious();
		//show(0);
	}

	@Override
	public void seekTo(int pos) {
		musicService.seek(pos);
	}

	public void setMusicBound(boolean musicBound) {
		this.musicBound = musicBound;
	}

	@Override
	public void start() {

		musicService.play();

	}

}