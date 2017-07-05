package com.lifeistech.android.storybook;

import android.net.Uri;

import io.realm.RealmObject;

public class ImageData extends RealmObject {


    private String image;
    private String text; //写真一枚一枚に対する説明文
    private int index; //bookの通し番号
    private int room;

    public void setRoom(int room) {
        this.room = room;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String uri) {
        this.image = uri;
    }
}

