package com.coryswainston.game.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.coryswainston.game.R;
import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.views.GameView;
import com.coryswainston.game.views.TutorialView;

import static com.coryswainston.game.Game.CONTINUE;
import static com.coryswainston.game.Game.LEVEL;
import static com.coryswainston.game.Game.SCORE;

public class TutorialActivity extends AppCompatActivity {

    private TutorialView tutorialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tutorialView = new TutorialView(this);
        tutorialView.setFinishListener(new ViewListener() {
            @Override
            public void onAction(Intent data) {
                Log.d("TutorialActivity", "Listener triggered. Exiting tutorial");
                onTutorialFinished(data);
            }
        });

        setContentView(tutorialView);
    }

    @Override
    protected void onPause(){
        super.onPause();
        tutorialView.pause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putAll(tutorialView.getStateVars());
    }

    @Override
    protected void onResume(){
        super.onResume();
        tutorialView.resumePaused();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void onTutorialFinished(Intent data) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}
