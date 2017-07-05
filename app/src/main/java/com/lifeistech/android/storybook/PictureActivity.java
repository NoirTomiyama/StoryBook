package com.lifeistech.android.storybook;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    LinearLayout container;

    TextView textView;

    //SquareViewの宣言
    SquareView ib0; SquareView ib5; SquareView ib10;
    SquareView ib1; SquareView ib6; SquareView ib11;
    SquareView ib2; SquareView ib7; SquareView ib12;
    SquareView ib3; SquareView ib8; SquareView ib13;
    SquareView ib4; SquareView ib9; SquareView ib14;

    int nowPageNumber;//何ページ目か

//    final int ALL_PAGE_NUMBER = 3;//全てのページ数

    //SquareView[]の配列
    SquareView[] buttons;

    //realm導入
    Realm realm;
    //Book型の変数の宣言
    Book book;

    //部屋番号(カード番号)
    int room;
    //通し番号(クリックしたところを保持する)
    int index;

    //RealmListの導入，要素はImageData型
    RealmList<ImageData> imageDataRealmList;

    Drawable drawable;

    float scale;
    int margins;

    Bitmap bmp1;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        container = (LinearLayout)findViewById(R.id.container);
        textView = (TextView)findViewById(R.id.nowPage);

        nowPageNumber= 0;
        textView.setText(String.valueOf(nowPageNumber));
        index = 0;

        buttons = new SquareView[]{
                ib0, ib1, ib2, ib3, ib4,
                ib5, ib6, ib7, ib8, ib9,
                ib10, ib11, ib12, ib13, ib14
        };

        //SquareViewの初期化
        for(int i = 0 ; i < buttons.length ; i++){
            buttons[i] = new SquareView(this);
            buttons[i].setId(i);
        }

        //ページのインテント
        Intent intent = getIntent();
        //int型room変数に、intentで"room"に入っている数値を取得する．
        room = intent.getIntExtra("room", -1);
        Log.d("room:", String.valueOf(room));

        Resources r = getResources();
        bmp1 = BitmapFactory.decodeResource(r, R.drawable.plus);

        drawable = getResources().getDrawable(R.drawable.border1);

        // dp単位を取得
        scale = getResources().getDisplayMetrics().density;
        margins = (int)(8 * scale);

        //初期時には5枚のみ表示
        for(int i = 0 ; i < 5 ; i++){
            buttons[i].setImageBitmap(bmp1);
//            buttons[i].setBackground(drawable);
            buttons[i].setBackgroundColor(Color.parseColor("#00000000"));
            buttons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            buttons[i].setPadding(margins,margins,margins,margins);
            buttons[i].setOnClickListener(onClickListener);
            container.addView(buttons[i]);
        }

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

                    Bitmap bmp = null;
                    String image = book.getImages().get(i).getImage();
                    byte[] im = Base64.decode(image,0);
                    bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                    buttons[i].setImageBitmap(bmp);

//                    nowPageNumber = (book.getImages().size()-1)/5 + 1;

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

    public void nextPage (View v){
        switch (String.valueOf(v.getTag())){
            case "up":
                upPage();
                break;
            case "down":
                downPage();
                break;
        }
    }

    private void downPage() {
        if(nowPageNumber != 2){
            container.removeAllViews();
            // TODO
            for(int i = 5*nowPageNumber + 5; i < 5 * (nowPageNumber+1) + 5;i++){
                buttons[i].setImageBitmap(bmp1);
                buttons[i].setBackgroundColor(Color.parseColor("#00000000"));
                buttons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                buttons[i].setPadding(margins,margins,margins,margins);
                buttons[i].setOnClickListener(onClickListener);
                container.addView(buttons[i]);
            }

            if( 5*nowPageNumber + 5 < book.getImages().size()){
                for(int i = 5*nowPageNumber + 5 ; i < book.getImages().size(); i++){

                    System.out.println("i = " + i);
                    System.out.println("book.images.index = " + book.getImages().get(i).getIndex());

                    Bitmap bmp = null;
                    String image = book.getImages().get(i).getImage();
                    byte[] im = Base64.decode(image,0);
                    bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                    buttons[i].setImageBitmap(bmp);
                }
            }
            nowPageNumber++;
        }
        textView.setText(String.valueOf(nowPageNumber));
    }

    private void upPage() {
        if(nowPageNumber != 0){
            container.removeAllViews();
            // TODO
            for(int i = nowPageNumber*5 - 5; i < nowPageNumber*5;i++){
                buttons[i].setImageBitmap(bmp1);
                buttons[i].setBackgroundColor(Color.parseColor("#00000000"));
                buttons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                buttons[i].setPadding(margins,margins,margins,margins);
                buttons[i].setOnClickListener(onClickListener);
                container.addView(buttons[i]);
            }

            if(nowPageNumber*5 - 5 < book.getImages().size()){
                for(int i = nowPageNumber*5-5; i < book.getImages().size(); i++){

                    System.out.println("i = " + i);
                    System.out.println("book.images.index = " + book.getImages().get(i).getIndex());

                    Bitmap bmp = null;
                    String image = book.getImages().get(i).getImage();
                    byte[] im = Base64.decode(image,0);
                    bmp = BitmapFactory.decodeByteArray(im,0,im.length);
                    buttons[i].setImageBitmap(bmp);
                }
            }
            nowPageNumber--;
        }
        textView.setText(String.valueOf(nowPageNumber));
    }

    //クリックイベント管理(addImageメソッド)
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");

            index = Integer.parseInt(String.valueOf(v.getId()));

            startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
        }
    };

    public void addImage (View v) {
//        Intent intent = new Intent(Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        int REQUEST_CODE_GALLERY = Integer.parseInt(String.valueOf(v.getTag()));

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
//        switch (v.getId()){
//            case 0:
//                index = 0;
//                break;
//            case 1:
//                index = 1;
//                break;
//            case 2:
//                index = 2;
//                break;
//            case 3:
//                index = 3;
//                break;
//            case 4:
//                index = 4;
//                break;
//        }
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
                    byte[] image =  byteArrayOutputStream.toByteArray();
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
