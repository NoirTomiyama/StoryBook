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

    public void back(View v){
        finish();
    }

    public void goToPict (View v) {
        Intent intent = new Intent (this, PictureActivity.class);
        startActivity(intent);
    }
}
