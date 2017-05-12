package com.lifeistech.android.storybook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class PictureActivity extends AppCompatActivity {

    //ImageButtonの宣言(いずれは12個に)
    ImageButton ib0;
    ImageButton ib1;
    ImageButton ib2;
    ImageButton ib3;
    ImageButton ib4;

    //ImageButton[]の配列
    ImageButton[] buttons;

    //realm導入
    Realm realm;

    //RealmListの導入，要素はImageData型
    RealmList<ImageData> imageDataRealmList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        ib0 = (ImageButton)findViewById(R.id.ib0);
        ib1 = (ImageButton)findViewById(R.id.ib1);
        ib2 = (ImageButton)findViewById(R.id.ib2);
        ib3 = (ImageButton)findViewById(R.id.ib3);
        ib4 = (ImageButton)findViewById(R.id.ib4);

        buttons = new ImageButton[]{ib0,
                                    ib1,
                                    ib2,
                                    ib3,
                                    ib4};

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        imageDataRealmList = new RealmList<ImageData>();
        RealmResults<Book> realmResults = realm.where(Book.class).findAll();

        if(realmResults != null){
            try {
                Log.d("realmResults", String.valueOf(realmResults));
                Book book = realmResults.get(0);
                Log.d("data", String.valueOf(book));
                Log.d("size", String.valueOf(book.images.size()));
                for(int i = 0; i < book.images.size(); i++){
                    Bitmap bmp = null;
                    String ims = book.images.get(i).image;
                    byte[] im = Base64.decode(ims,0);
                    bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                    buttons[i].setImageBitmap(bmp);
                }
            }catch(Exception e){
                Log.d("e", String.valueOf(e));
            }

        }

    }

    public void back(View v) {
        realm.close();
        finish();
    }

    public void save (View v){
        realm.beginTransaction();
        Book book = realm.createObject(Book.class);
        book.room = 0;
        book.images = imageDataRealmList;
        Log.d("Book", String.valueOf(book));
        realm.commitTransaction();

    }

    public void addImage (View v) {
        Intent library = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        library.addCategory(Intent.CATEGORY_OPENABLE);
        library.setType("image/*");
        Log.d("id", String.valueOf(v.getTag()));
        Log.d("id", String.valueOf(v.getTag().getClass()));
        int num = Integer.parseInt(String.valueOf(v.getTag()));
        startActivityForResult(library, num);
    }

    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fd = pfd.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fd);
        pfd.close();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK){
            Uri uri = null;
            uri =data.getData();
            try {
                Bitmap img = getBitmapFromUri(uri);
                Log.d("resultCode", String.valueOf(resultCode));
                int Width = img.getWidth();
                int Hight = img.getHeight();

                Bitmap bmpBase = Bitmap.createScaledBitmap(img,Width/2,Hight/2,false);
                buttons[requestCode].setImageBitmap(bmpBase);
                realm.beginTransaction();
                ImageData imgdt = realm.createObject(ImageData.class);
                imgdt.room = 0;
                imgdt.id = resultCode;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmpBase.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] mImageDeta =  baos.toByteArray();
                imgdt.image = Base64.encodeToString(mImageDeta,Base64.NO_WRAP);
                imageDataRealmList.add(imgdt);
                realm.commitTransaction();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
