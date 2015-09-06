package com.zezo.music;

public class Song {

	private String artist;
	private String duration;
	private long id;
	private String title;
	private String data;

	public Song(long id, String title, String artist, String duration, String data) {

		this.id = id;
		this.title = title;
		this.artist = artist;
		this.setDuration(duration);
		this.data = data;

	}

	public String getArtist() {
		return artist;
	}

	public String getDuration() {
		return duration;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

}