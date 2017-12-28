package com.coryswainston.game.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.views.MenuView;

public class MainActivity extends AppCompatActivity {

    public static final String HANKEN_BOOK_FONT = "Hanken-Book.ttf";

    private MenuView menuView;
    private int level = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        menuView = new MenuView(this);
        menuView.setViewListener(new ViewListener() {
            @Override
            public void onAction(Intent data) {
                Log.d("MainActivity", "Starting first level");
                startLevel(data);
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "returned with result '" + resultCode + "' and data '" +
                data.toString() + "'");
        if (requestCode == resultCode) {
            startLevel(data);
        }
    }

    private void startLevel(Intent data) {
        Log.d("MainActivity", "Starting new level.");
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("score", data.getIntExtra("score", 0));
        intent.putExtra("level", ++level);
        startActivityForResult(intent, 1);
    }
}
