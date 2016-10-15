package com.github.a28hacks.driveby.model.database;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GeoItem extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    private double latitude;
    private double longitude;
    private RealmList<InfoChunk> infoChunks;
    private String type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public RealmList<InfoChunk> getInfoChunks() {
        return infoChunks;
    }

    public void setInfoChunks(RealmList<InfoChunk> infoChunks) {
        this.infoChunks = infoChunks;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
