package dev.olog.msc.api.last.fm.track.info;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Track {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("mbid")
    @Expose
    public String mbid;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("duration")
    @Expose
    public String duration;
    @SerializedName("streamable")
    @Expose
    public Streamable streamable;
    @SerializedName("listeners")
    @Expose
    public String listeners;
    @SerializedName("playcount")
    @Expose
    public String playcount;
    @SerializedName("artist")
    @Expose
    public Artist artist;
    @SerializedName("album")
    @Expose
    public Album album;
    @SerializedName("toptags")
    @Expose
    public Toptags toptags;
    @SerializedName("wiki")
    @Expose
    public Wiki wiki;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Track() {
    }

    /**
     * 
     * @param listeners
     * @param duration
     * @param mbid
     * @param toptags
     * @param album
     * @param name
     * @param streamable
     * @param playcount
     * @param artist
     * @param wiki
     * @param url
     */
    public Track(String name, String mbid, String url, String duration, Streamable streamable, String listeners, String playcount, Artist artist, Album album, Toptags toptags, Wiki wiki) {
        super();
        this.name = name;
        this.mbid = mbid;
        this.url = url;
        this.duration = duration;
        this.streamable = streamable;
        this.listeners = listeners;
        this.playcount = playcount;
        this.artist = artist;
        this.album = album;
        this.toptags = toptags;
        this.wiki = wiki;
    }

    @Override
    public String toString() {
        return "Track{" +
                "name='" + name + '\'' +
                ", mbid='" + mbid + '\'' +
                ", url='" + url + '\'' +
                ", duration='" + duration + '\'' +
                ", streamable=" + streamable +
                ", listeners='" + listeners + '\'' +
                ", playcount='" + playcount + '\'' +
                ", artist=" + artist +
                ", album=" + album +
                ", toptags=" + toptags +
                ", wiki=" + wiki +
                '}';
    }
}