package com.lifeistech.android.storybook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
    }

    public void goToPict (View v) {
        Intent intent = new Intent (this, PictureActivity.class);

        switch (v.getId()){
            case R.id.imageButton6:
                intent.putExtra("room",0);
                break;
            case R.id.imageButton2:
                intent.putExtra("room",1);
                break;
            case R.id.imageButton3:
                intent.putExtra("room",2);
                break;
            case R.id.imageButton4:
                intent.putExtra("room",3);
                break;
        }
        startActivity(intent);
    }

    public void back(View v){
        finish();
    }

}
