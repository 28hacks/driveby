package com.github.a28hacks.driveby.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class QueryResult {

    @SerializedName("geosearch")
    List<GeoItem> items;

    Map<String, GeoItem> pages;

    public List<GeoItem> getItems() {
        return items;
    }

    public void setItems(List<GeoItem> items) {
        this.items = items;
    }

    public Map<String, GeoItem> getPages() {
        return pages;
    }

    public void setPages(Map<String, GeoItem> pages) {
        this.pages = pages;
    }
}
