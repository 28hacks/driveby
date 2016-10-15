package com.github.a28hacks.driveby.model;


import com.google.gson.annotations.SerializedName;

public class GeoItem {

    @SerializedName("pageid")
    private long pageId;

    private String title;

    private double lat;

    private double lon;

    @SerializedName("dist")
    private float distance;

    public long getPageId() {
        return pageId;
    }

    public void setPageId(long pageId) {
        this.pageId = pageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
