package com.github.a28hacks.driveby.model.wiki_api;

import io.realm.RealmObject;

/**
 * Created by dominic on 18.11.17.
 */

public class Thumbnail extends RealmObject{

    private static final String THUMBNAIL_WIDTH_IDENTIFIER = "px";

    private String source;

    private int width;

    private int height;

    public String getSource() {
        return source;
    }

    public String getSourceWithWidth(int width){
        return source.replace(this.width+THUMBNAIL_WIDTH_IDENTIFIER,
                width+THUMBNAIL_WIDTH_IDENTIFIER);
    }

    public void setSource(String source) {
        this.source = source;
    }
}
