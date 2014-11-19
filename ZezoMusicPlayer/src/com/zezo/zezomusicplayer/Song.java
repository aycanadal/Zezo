package com.zezo.zezomusicplayer;

public class Song {

	private String artist;
	private long id;
	private String title;

	public Song(long songID, String songTitle, String songArtist) {
		id = songID;
		title = songTitle;
		artist = songArtist;
	}

	public String getArtist() {
		return artist;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
}
