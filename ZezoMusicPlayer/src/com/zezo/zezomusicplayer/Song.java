package com.zezo.zezomusicplayer;

public class Song {

	private String artist;
	private long id;
	private String title;
	private String duration;

	public Song(long id, String title, String artist, String duration) {

		this.id = id;
		this.title = title;
		this.artist = artist;
		this.setDuration(duration);

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

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

}
