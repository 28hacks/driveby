package com.github.a28hacks.driveby.model.wiki_api;


import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class QueryResult {

    @SerializedName("geosearch")
    List<GeoSearchResult> items;

    Map<String, GeoSearchResult> pages;

    public List<GeoSearchResult> getItems() {
        return items;
    }

    public void setItems(List<GeoSearchResult> items) {
        this.items = items;
    }

    public Map<String, GeoSearchResult> getPages() {
        return pages;
    }

    public void setPages(Map<String, GeoSearchResult> pages) {
        this.pages = pages;
    }
}
