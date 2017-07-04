package com.lifeistech.android.storybook;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Book extends RealmObject {

    int room;
    String text;
    RealmList<ImageData> images;

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public RealmList<ImageData> getImages() {
        return images;
    }

    public void setImages(RealmList<ImageData> images) {
        this.images = images;
    }
}
