package com.zezo.music.domain;

import android.content.ContentUris;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.IOException;

public class Song {

    private String artist;
    private String duration;
    private long id;
    private String title;
    private String data;
    private int sampleRate;

    public Song(long id, String title, String artist, String duration, String data) {

        this.id = id;
        this.title = title;
        this.artist = artist;
        this.setDuration(duration);
        this.data = data;

    }

    public int getSampleRate(){

        return sampleRate;

    }

    public Uri getUri(){

        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getId());

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

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
