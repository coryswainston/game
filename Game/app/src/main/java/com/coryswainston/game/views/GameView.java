package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.helpers.DrawingHelper;
import com.coryswainston.game.helpers.GestureHelper;
import com.coryswainston.game.objects.Sheep;
import com.coryswainston.game.objects.Cloud;
import com.coryswainston.game.objects.Comet;
import com.coryswainston.game.objects.Llama;
import com.coryswainston.game.objects.Sprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * View that handles the game
 *
 * @author Cory Swainston
 */

public class GameView extends SurfaceView implements Runnable {

    private final long TARGET_MILLIS = 33;
    private final int INITIAL_FREQUENCY = 50;
    private final String HIGH_SCORE = "high_score";
    private final String LLAMA_PREFS = "llama_prefs";

    volatile boolean playing = true;
    private Thread gameThread = null;
    private SurfaceHolder surfaceHolder = getHolder();
    GestureHelper gestureHelper = new GestureHelper();
    private Point bounds;
    private int yFloor;

    private Context context;
    private SoundPool soundPool;
    private SharedPreferences sharedPreferences;

    private Llama llama;
    private List<Comet> comets = new ArrayList<>();
    private List<Sheep> sheeps = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>(3);
    private int cometFrequency = INITIAL_FREQUENCY;
    private int sheepFrequency = INITIAL_FREQUENCY * 2;

    int points = 0;
    int highScore;
    boolean newHigh;

    private int gatheredSheep;

    /**
     * Constructor, initializes everything for the game
     *
     * @param context the activity we're in
     */
    public GameView(Context context){
        super(context);
        this.context = context;

        setUpBoundaries();
        createLlama();
        setUpClouds();
        getHighScore();
    }

    private void createLlama() {
        llama = new Llama(context, bounds.y / 6, bounds.y / 6);
        llama.setX(10);
        llama.setY(yFloor - llama.getHeight());
        llama.setFloor(llama.getY());
        llama.setCeiling(bounds.y / 5);
    }

    private void setUpBoundaries() {
        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);
        yFloor = bounds.y - bounds.y / 7;
        surfaceHolder.setFixedSize(bounds.x, bounds.y);
    }

    private void setUpClouds() {
        for(int i = 0; i < 3; i++){
            Cloud cloud = new Cloud(context);
            cloud.setSize(bounds.y / 4, bounds.y / 6);
            cloud.setDx((float)(.5 - 0.1 * i));
            clouds.add(i, cloud);
        }
        clouds.get(0).setX(bounds.x / 2);
        clouds.get(1).setX(bounds.x * 7 / 8);
        clouds.get(2).setX(bounds.x / 6);
        clouds.get(0).setY(bounds.y / 2);
        clouds.get(1).setY(bounds.y / 4);
        clouds.get(2).setY(0);
    }

    private void getHighScore() {
        sharedPreferences = context.getSharedPreferences(LLAMA_PREFS, 0);
        highScore = sharedPreferences.getInt(HIGH_SCORE, 0);
        newHigh = false;
    }

    /**
     * The game loop
     */
    public void run(){
        while (playing){
            long startMillis = System.currentTimeMillis();
            update();
            draw();

            long frameTime = System.currentTimeMillis() - startMillis;
            control(frameTime);
        }
    }

    /**
     * Handles user input through touch
     *
     * @param e the motion event
     * @return whether event is consumed
     */
    @Override
    public boolean onTouchEvent(MotionEvent e){

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: // when finger hits the screen
                if (gestureHelper.isFirstTouch()) {
                    gestureHelper.down(e);
                    return true;
                }
                return true;
            case MotionEvent.ACTION_UP: // when finger releases
                if(!playing){
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }

                gestureHelper.up(e);

                if (gestureHelper.noSwipe()) {
                    llama.setDx(0);
                } else if (gestureHelper.isRightSwipe()){
                    llama.turnRight();
                    llama.setDx(30);
                } else if (gestureHelper.isLeftSwipe()) {
                    llama.turnLeft();
                    llama.setDx(-30);
                } else if (gestureHelper.isSwipeUp()){
                    llama.jump();
                } else {
                    Log.d("Gesture", "Swiped down foo");
                    llama.duck();
                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * Move everything along
     */
    private void update(){
        llama.update();
        updateComets();
        detectCollisions();
        updateClouds();
        updateSheep();
        checkBounds();
        updateHighScore();
    }

    private void updateHighScore() {
        if (!playing && points > highScore){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, points);
            editor.apply();
            newHigh = true;
        }
    }

    private void updateClouds(){
        for (Cloud cloud : clouds){
            if (cloud.getX() < bounds.x){
                cloud.update();
            } else {
                cloud.setX(0 - cloud.getWidth());
            }
        }
    }

    private void updateSheep(){
        Sheep newSheep = null;
        Random random = new Random();
        if (random.nextInt(sheepFrequency) == 1 && sheeps.size() < 5){
            newSheep = new Sheep(getContext());
        }
        if (newSheep != null){
            newSheep.setSize(bounds.y / 6, bounds.y / 9);
            newSheep.setX(random.nextInt(2) == 1 ? bounds.x : -newSheep.getWidth());
            newSheep.setY(yFloor - newSheep.getHeight());
            newSheep.setDx(newSheep.getX() > 0 ? -5 : 5);
            if (newSheep.getDx() < 0){
                newSheep.turnLeft();
            }
            sheeps.add(newSheep);
        }
        for (Sheep sheep : sheeps){
            sheep.update();
        }
    }

    private void updateComets(){
        Comet newComet = null;
        Random random = new Random();
        if (random.nextInt(cometFrequency) == 1 && comets.size() < 3){
            newComet = new Comet(getContext(), random.nextInt(bounds.x));
        }
        if (newComet != null){
            newComet.setSize(bounds.y /10, bounds.y / 7);
            newComet.setDy(bounds.y / 50);
            newComet.setDx(random.nextInt(6) - 3);
            comets.add(newComet);
        }
        for (Iterator<Comet> it = comets.iterator(); it.hasNext();){
            Comet comet = it.next();
            if ((comet.getY() >= yFloor - comet.getHeight()) && !comet.isExploded()){
                comet.explode();
            }
            comet.update();
            if (!comet.isAlive()){
                it.remove();
                points += 10;
            }
        }
    }

    private void detectCollisions(){
        Point llamaCenter = new Point(llama.getX() + llama.getWidth() / 2, llama.getY() + llama.getHeight() / 3);
        for(Comet comet : comets){
            Point cometCenter = new Point(comet.getX() + comet.getWidth() / 2, comet.getY() + comet.getHeight());
            if (Math.abs(cometCenter.x - llamaCenter.x) < llama.getWidth() / 2.1 + comet.getWidth() / 2 &&
                    cometCenter.y - llamaCenter.y >= 0){
                comet.explode();
                playing = false;
                llama.kill();
            }
        }
        for(Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            Point sheepCenter = new Point(sheep.getX() + sheep.getWidth() / 2, sheep.getY());
            if (Math.abs(llamaCenter.x - sheepCenter.x) < 100 && llama.isDucking()) {
                it.remove();
                llama.addToPile(sheep);
            }
        }
    }

    private void checkBounds(){
        for (Sheep sheep : sheeps){
            if (sheep.getX() > bounds.x - sheep.getWidth() && sheep.getDx() > 0){
                sheep.turnLeft();
                sheep.setDx(-sheep.getDx());
            } else if (sheep.getX() < 0 && sheep.getDx() < 0){
                sheep.turnRight();
                sheep.setDx(-sheep.getDx());
            }
        }
        if (llama.getX() > bounds.x - llama.getWidth() || llama.getX() < 0){
            llama.setDx(0);
        }
    }

    /**
     * You know what this one does
     */
    private void draw(){
        if(readyToDraw()) {
            DrawingHelper drawingHelper = new DrawingHelper(context, surfaceHolder);

            drawingHelper.fillBackground(Color.rgb(180, 230, 255));

            drawingHelper.draw(clouds);
            drawingHelper.drawRectangle(0, yFloor, bounds.x, bounds.y, Color.rgb(0, 100, 0)); // the ground
            drawingHelper.draw(llama);
            drawingHelper.draw(llama.getSheepPile(), comets, sheeps);

            int fontSize = bounds.y / 20;
            drawingHelper.drawScore(points, fontSize, 40, 70);

            if (!playing){
                drawingHelper.throwShade();
                int xTextPosition = bounds.x / 2;
                int yTextPosition = bounds.y / 3;
                fontSize = bounds.y / 4;
                drawingHelper.drawBoldText(llama.isAlive() ? "PAUSED" : "GAME OVER", fontSize,
                        xTextPosition, yTextPosition, Color.rgb(180, 0, 0));
                drawingHelper.drawBoldText((newHigh ? "NEW HIGH SCORE: " : "SCORE: ") + points,
                        newHigh ? fontSize / 2 : fontSize, xTextPosition, yTextPosition + fontSize + 50,
                        Color.YELLOW);
                drawingHelper.drawBoldText("Tap to continue", bounds.y / 12, xTextPosition,
                        bounds.y * 3/4, Color.BLUE);
            }

            drawingHelper.finish();
        }
    }

    private boolean readyToDraw() {
        return surfaceHolder.getSurface().isValid();
    }

    private void control(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
        if (leftoverMillis < 5){
            leftoverMillis = 5;
        }
        try {
            gameThread.sleep(leftoverMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
