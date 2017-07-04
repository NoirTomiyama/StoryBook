package com.lifeistech.android.storybook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


public class PictureActivity extends AppCompatActivity {

    //ImageButtonの宣言
    ImageButton ib0;
    ImageButton ib1;
    ImageButton ib2;
    ImageButton ib3;
    ImageButton ib4;

    /*
    ImageButton ib5;
    ImageButton ib6;
    ImageButton ib7;
    ImageButton ib8;
    ImageButton ib9;
    ImageButton ib10;
    ImageButton ib11;
    ImageButton ib12;
    ImageButton ib13;
    ImageButton ib14;
    */

    int nowPageNumber;//何ページ目か

    int allPageNumber;//全てのページ数

    //ImageButton[]の配列
    ImageButton[] buttons;

    //realm導入
    Realm realm;
    //Book型の変数の宣言
    Book book;

    //部屋番号
    int room;

    //RealmListの導入，要素はImageData型
    RealmList<ImageData> imageDataRealmList;

    //idリスト配列
    int[] idLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        idLists = new int[]{
                R.id.ib0,R.id.ib1,R.id.ib2,R.id.ib3,R.id.ib4
        };

        buttons = new ImageButton[]{
                ib0, ib1, ib2, ib3, ib4
        };

        for(int i = 0 ; i < idLists.length ; i++){
            buttons[i] = (ImageButton)findViewById(idLists[i]);
        }

        //ページのインテント
        Intent intent = getIntent();
        //int型room変数に、intentで"room"に入っている数値を取得する．
        room = intent.getIntExtra("room", -1);
        Log.d("room:", String.valueOf(room));

        Realm.init(this);
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        imageDataRealmList = new RealmList<ImageData>();

        //FindFirstで一つだけ取り出そう!!
        book = realm.where(Book.class).equalTo("images.room",room).findFirst();
        System.out.println("book = " + book);

        //画像から、必要なページ数を計算

        if(book == null){

            //最初に開いた時にはデータがないから初期化する

            //book が nullだった場合
            book = realm.createObject(Book.class);
            book.room = room;
            book.images = new RealmList<>();

        }else{
            try {
                Log.d("data:", String.valueOf(book));
                Log.d("size:", String.valueOf(book.images.size()));
                //RealmResults<ImageData> results=book.images.where(ImageData.class);
                for(int i = 0; i < book.images.size(); i++){

                    System.out.println("i = " + i);
                    System.out.println("book.images.get(i).room = " + book.images.get(i).room);

                    if(book.images.get(i).room == 0){

                        Bitmap bmp = null;
                        String ims = book.images.get(i).image;
                        byte[] im = Base64.decode(ims,0);
                        bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                        buttons[i].setImageBitmap(bmp);
                        System.out.println("(if文内) i = " + i);

                        nowPageNumber = (book.images.size()-1)/5 + 1;

                    }

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

    @Override
    public void onDestroy(){
        super.onDestroy();
        realm.close();
    }

    public void save (View v){
        realm.commitTransaction();
    }

    public void nextPage (View v){

    }

    public void addImage (View v) {
//        Intent library = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        library.addCategory(Intent.CATEGORY_OPENABLE);
//        library.setType("image/*");
//        Log.d("id", String.valueOf(v.getTag()));
//        Log.d("id", String.valueOf(v.getTag().getClass()));
//        int num = Integer.parseInt(String.valueOf(v.getTag()));
//        startActivityForResult(library, num);
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        int REQUEST_CODE_GALLERY = Integer.parseInt(String.valueOf(v.getTag()));

        startActivityForResult(intent,REQUEST_CODE_GALLERY);
    }

    //これはおまじない
//    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
//        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri,"r");
//        FileDescriptor fd = pfd.getFileDescriptor();
//        Bitmap image = BitmapFactory.decodeFileDescriptor(fd);
//        pfd.close();
//        return image;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if(resultCode == Activity.RESULT_OK){
//            Uri uri = null;
//            uri = data.getData();
            try {
                //データフォルダから写真をとってくる処理
                InputStream inputStream = getContentResolver().openInputStream(intent.getData());
                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

//                Bitmap img = getBitmapFromUri(uri);
                System.out.println("(onActivityResult)requestCode =  " + requestCode);
//                Log.d("resultCode", String.valueOf(resultCode));
                int Width = bmp.getWidth();
                int Height = bmp.getHeight();

                //requestコード(タグ付けした整数値によりImageViewに表示させる)
                Bitmap bmpBase = Bitmap.createScaledBitmap(bmp,Width/2,Height/2,false);
                buttons[requestCode].setImageBitmap(bmpBase);




                ImageData imageData = realm.createObject(ImageData.class);
                imageData.room = nowPageNumber;
                imageData.id = requestCode;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bmpBase.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                final byte[] mImageData =  byteArrayOutputStream.toByteArray();
                imageData.image = Base64.encodeToString(mImageData,Base64.NO_WRAP);

                if(imageDataRealmList.size() > requestCode) {
                    book.images.set(requestCode,imageData);
                }
                else{
                  book.images.add(imageData);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
