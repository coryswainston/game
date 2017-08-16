package com.coryswainston.game;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;
    public MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        gameView = new GameView(this);
        setContentView(gameView);
        mediaPlayer = MediaPlayer.create(this, R.raw.pixelland);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(0.6f, 0.6f);
        mediaPlayer.start();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameView.pause();
        mediaPlayer.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameView.resume();
        mediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
