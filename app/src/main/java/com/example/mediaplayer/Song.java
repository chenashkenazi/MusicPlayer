package com.example.mediaplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.Serializable;
import java.text.BreakIterator;

public class Song implements Serializable {

    private String name;
    private String artist;
    private String link;
    private String image;

    public Song(String name, String artist, String link, String image) {
        this.name = name;
        this.artist = artist;
        this.link = link;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }
}
