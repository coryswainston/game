package com.coryswainston.game.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.views.MenuView;

import static com.coryswainston.game.Game.LEVEL;
import static com.coryswainston.game.Game.SCORE;

public class MainActivity extends AppCompatActivity {

    private MenuView menuView;
    private int level = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                (data != null ? data.toString() : "no data...") + "'");
        if (requestCode == resultCode) {
            startLevel(data);
        } else {
            level = 0;
        }
    }

    private void startLevel(Intent data) {
        Log.d("MainActivity", "Starting new level.");
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(SCORE, data.getIntExtra(SCORE, 0));
        intent.putExtra(LEVEL, ++level);
        startActivityForResult(intent, 1);
    }
}
