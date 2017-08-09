package com.coryswainston.game;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String HANKEN_BOOK_FONT = "Hanken-Book.ttf";

    private MenuView menuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        menuView = new MenuView(this);
        setContentView(menuView);
    }

    @Override
    protected void onPause(){
        super.onPause();
         menuView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        menuView.resume();
    }
}
