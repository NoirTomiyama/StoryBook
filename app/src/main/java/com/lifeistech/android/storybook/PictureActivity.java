package com.lifeistech.android.storybook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;


public class PictureActivity extends AppCompatActivity {

    private static final int RESULT_PICK_IMAGEFILE = 1000;

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

    //部屋番号(カード番号)
    int room;

    //通し番号
    int index;

    //RealmListの導入，要素はImageData型
    RealmList<ImageData> imageDataRealmList;

    //idリスト配列
    int[] idLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        nowPageNumber= 0;
        index = 0;

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
        imageDataRealmList = new RealmList<ImageData>();

        //FindFirstで一つだけ取り出そう!!
        book = realm.where(Book.class).equalTo("images.room",room).findFirst();
        System.out.println("book = " + book);

        //画像から、必要なページ数を計算

        if(book == null){

            //最初に開いた時にはデータがないから初期化する
            //book が nullだった場合，bookを作成する

            realm.beginTransaction();
            book = realm.createObject(Book.class);
            book.setRoom(room);
            book.setImages(new RealmList<ImageData>());
            realm.commitTransaction();


        }else{
            try {
                Log.d("data:", String.valueOf(book));
                Log.d("size:", String.valueOf(book.getImages().size()));
                //RealmResults<ImageData> results=book.images.where(ImageData.class);

                for(int i = 0; i < book.getImages().size(); i++){

                    System.out.println("i = " + i);
                    System.out.println("book.images.index = " + book.getImages().get(i).getIndex());

//                    Uri uri = null;
//                    uri = Uri.parse(book.getImages().get(i).getUri());
//                    Log.d("Uri:",uri.toString());

//                    try {

                        Bitmap bmp = null;
                        String image = book.getImages().get(i).getImage();
                        byte[] im = Base64.decode(image,0);
                        bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                        buttons[i].setImageBitmap(bmp);

//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//                    if(book.images.get(i).room == 0){
//
//                        Bitmap bmp = null;
//                        String ims = book.images.get(i).image;
//                        byte[] im = Base64.decode(ims,0);
//                        bmp = BitmapFactory.decodeByteArray(im,0,im.length);
//                        buttons[i].setImageBitmap(bmp);
//                        System.out.println("(if文内) i = " + i);
//
                       nowPageNumber = (book.getImages().size()-1)/5 + 1;
//                    }
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
        finish();
    }

    public void save (View v){
    }

    public void nextPage (View v){

    }

    public void addImage (View v) {

//        Intent intent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        int REQUEST_CODE_GALLERY = Integer.parseInt(String.valueOf(v.getTag()));

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        switch (v.getId()){
            case R.id.ib0:
                index = 0;
                break;
            case R.id.ib1:
                index = 1;
                break;
            case R.id.ib2:
                index = 2;
                break;
            case R.id.ib3:
                index = 3;
                break;
            case R.id.ib4:
                index = 4;
                break;
        }

        startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
    }

    //これはおまじない
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }


    //もともとrequestCodeをindexで使用していた
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData){
        if(requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;

            if (resultData != null){
                uri = resultData.getData();
                Log.d("Uri:",uri.toString());

                try {

                    Bitmap bmp = getBitmapFromUri(uri);

                    int width = bmp.getWidth();
                    int height = bmp.getHeight();

                    Bitmap bmpBase = Bitmap.createScaledBitmap(bmp,width/2,height/2,false);
                    buttons[index].setImageBitmap(bmpBase);

                    realm.beginTransaction();
                    //データの永続化
                    ImageData imageData = realm.createObject(ImageData.class);

                    //TODO EditTextから読み取ったString型のTextもここで保存
                    imageData.setIndex(index);
                    imageData.setRoom(room);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bmpBase.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                    final byte[] image =  byteArrayOutputStream.toByteArray();
                    imageData.setImage(Base64.encodeToString(image,Base64.NO_WRAP));

                    final ImageData managedImageData = realm.copyToRealm(imageData);
                    book.getImages().add(managedImageData);

                    realm.commitTransaction();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            try {
//
//                //データフォルダから写真をとってくる処理
////                InputStream inputStream = getContentResolver().openInputStream(intent.getData());
////                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
////                inputStream.close();
//
////                Bitmap img = getBitmapFromUri(uri);
////                System.out.println("(onActivityResult)requestCode =  " + requestCode);
////                Log.d("resultCode", String.valueOf(resultCode));
//
////                int Width = bmp.getWidth();
////                int Height = bmp.getHeight();
////
////                //requestコード(タグ付けした整数値によりImageViewに表示させる)
////                Bitmap bmpBase = Bitmap.createScaledBitmap(bmp,Width/2,Height/2,false);
////                buttons[index].setImageBitmap(bmpBase);
//
////                ImageData imageData = realm.createObject(ImageData.class);
////                imageData.room = nowPageNumber;
////                imageData.id = requestCode;
//
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                bmpBase.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
//                final byte[] mImageData =  byteArrayOutputStream.toByteArray();
//                imageData.image = Base64.encodeToString(mImageData,Base64.NO_WRAP);
//
//                if(imageDataRealmList.size() > requestCode) {
//                    book.images.set(requestCode,imageData);
//                }
//                else{
//                  book.images.add(imageData);
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
