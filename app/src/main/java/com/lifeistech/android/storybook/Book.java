package com.lifeistech.android.storybook;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Book extends RealmObject {

    private int room; //roomでBookの種類を判別

    //private String text; //Book一つ一つに存在するテキスト
    private RealmList<ImageData> images;

    public void setRoom(int room) {
        this.room = room;
    }

    public RealmList<ImageData> getImages() {
        return images;
    }

    public void setImages(RealmList<ImageData> images) {
        this.images = images;
    }
}
