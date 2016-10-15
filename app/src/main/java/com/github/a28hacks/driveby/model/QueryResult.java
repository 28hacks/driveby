package com.github.a28hacks.driveby.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QueryResult {

    @SerializedName("geosearch")
    List<GeoItem> items;

    public List<GeoItem> getItems() {
        return items;
    }

    public void setItems(List<GeoItem> items) {
        this.items = items;
    }
}
