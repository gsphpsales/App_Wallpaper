package com.bytecod.wallpaperhd.models;

import com.google.firebase.database.Exclude;

public class Wallpaper {

    @Exclude
    public String id;

    public String title, desc, url, urlSource;

    @Exclude
    public String category;

    @Exclude
    public boolean isFavourite = false;

    public Wallpaper(String id, String title, String desc, String url, String urlSource, String category) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.url = url;
        this.urlSource = urlSource;
        this.category = category;
    }

}
