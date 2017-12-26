package com.coryswainston.game.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.coryswainston.game.views.MenuView;

public class MainActivity extends AppCompatActivity {

    public static final String HANKEN_BOOK_FONT = "Hanken-Book.ttf";

    private MenuView menuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
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
