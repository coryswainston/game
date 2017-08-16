package com.coryswainston.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.solver.SolverVariable;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * View that handles the game
 *
 * @author Cory Swainston
 */

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Point bounds;
    private int yFloor;
    private Context context;
    private SoundPool soundPool;
    private SharedPreferences sharedPreferences;
    private final String HIGH_SCORE = "high_score";
    private final String LLAMA_PREFS = "llama_prefs";

    private Llama llama;
    private ArrayList<Comet> comets;
    private ArrayList<Sheep> sheeps;
    private Cloud[] clouds;
    int points;
    int highScore;
    boolean newHigh;

    private int cometFrequency;
    private int sheepFrequency;
    private float[] initialValues;

    private final long TARGET_MILLIS = 33;
    private final int INITIAL_FREQUENCY = 50;

    /**
     * Constructor, initializes everything for the game
     *
     * @param context the activity we're in
     */
    public GameView(Context context){
        super(context);
        this.context = context;

        // get the coordinates of the bottom and right of the screen
        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);
        yFloor = bounds.y - bounds.y / 7;

        // make a llama
        llama = new Llama(context);
        llama.setSize(bounds.y / 6, bounds.y / 6);
        llama.setX(10);
        llama.setY(yFloor - llama.getBitmap().getHeight());
        llama.setFloor(llama.getY());
        llama.setCeiling(bounds.y / 5);


        //set up comet array
        comets = new ArrayList<>();
        cometFrequency = INITIAL_FREQUENCY;

        // set up sheep array
        sheeps = new ArrayList<>();
        sheepFrequency = INITIAL_FREQUENCY * 2;

        // set everything up for drawing
        surfaceHolder = getHolder();
        surfaceHolder.setFixedSize(bounds.x, bounds.y);
        paint = new Paint();
        clouds = new Cloud[3];
        for(int i = 0; i < 3; i++){
            Cloud cloud = new Cloud(context);
            cloud.setSize(bounds.y / 4, bounds.y / 6);
            cloud.setDx((float)(.5 - 0.1 * i));
            clouds[i] = cloud;
        }
        clouds[0].setX(bounds.x / 2);
        clouds[1].setX(bounds.x * 7 / 8);
        clouds[2].setX(bounds.x / 6);
        clouds[0].setY(bounds.y / 2);
        clouds[1].setY(bounds.y / 4);
        clouds[2].setY(0);

        // set up soundPool
        if (Build.VERSION.SDK_INT >= 21){
            AudioAttributes audio = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audio).setMaxStreams(10);
            soundPool = builder.build();
        } else {
            soundPool = new SoundPool(AudioManager.STREAM_MUSIC, 10, 1);
        }

        // initialize other members
        playing = true;
        initialValues = new float[2];
        points = 0;
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

        // the amount of movement to register a swipe
        final int THRESHOLD = 200;

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: // when finger hits the screen
                initialValues[0] = e.getX();
                initialValues[1] = e.getY();

                return true;
            case MotionEvent.ACTION_UP: // when finger releases
                if(!playing){
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                }

                float endX = e.getX();
                float endY = e.getY();

                float xDiff = endX - initialValues[0];
                float yDiff = endY - initialValues[1];
                Log.d("ACTION UP", "xDiff = " + xDiff + ", yDiff = " + yDiff);

                if (Math.abs(xDiff) < THRESHOLD && Math.abs(yDiff) < THRESHOLD) {
                    // if we haven't swiped, the llama stops
                    llama.setDx(0);
                    Log.d(">", "Didn't pass threshold");
                } else if (Math.abs(xDiff) > THRESHOLD) {
                    // if we swipe left or right the llama accelerates
                    Log.d(">", "x threshold passed");
                    if (xDiff > 0){
                        llama.turnRight();
                        llama.setDx(30);
                    } else {
                        llama.turnLeft();
                        llama.setDx(-30);
                    }
                } else if (yDiff < 0){
                    // if we swipe up, the llama jumps
                    Log.d(">", "y threshold passed");
                    llama.jump();
                } else {
                    llama.duck();
                }
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

        if (!playing && points > highScore){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, points);
            editor.apply();
            newHigh = true;
        }
    }

    private void updateClouds(){
        for (int i = 0; i < 3; i++){
            if (clouds[i].getX() < bounds.x){
                clouds[i].update();
            } else {
                clouds[i].setX(0 - clouds[i].getWidth());
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
            if (!comet.alive){
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
        for(Sheep sheep : sheeps){
            Point sheepCenter = new Point(sheep.getX() + sheep.getWidth() / 2, sheep.getY());
            if (Math.abs(llamaCenter.x - sheepCenter.x) < 100) {
                sheep.setX(llama.getX());
                sheep.setY(llama.getY() + sheep.getHeight() - llamaCenter.y);

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
        if(surfaceHolder.getSurface().isValid()) {
            Typeface normal = Typeface.createFromAsset(context.getAssets(), MainActivity.HANKEN_BOOK_FONT);
            Typeface bold = Typeface.create(normal, Typeface.BOLD);
            paint.setTypeface(normal);
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.rgb(180, 230, 255));

            // draw the ground and clouds
            for (int i = 0; i < 3; i++){
                canvas.drawBitmap(clouds[i].getBitmap(), clouds[i].getX(), clouds[i].getY(), paint);
            }
            paint.setColor(Color.rgb(0, 100, 0));
            canvas.drawRect(0, yFloor, bounds.x, bounds.y, paint);

            // draw llama and comets
            Bitmap llamaBitmap = llama.getBitmap();
            canvas.drawBitmap(llama.getBitmap(), llama.getX(), llama.getY(), paint);
            for (Comet comet : comets) {
                canvas.drawBitmap(comet.getBitmap(), comet.getX(), comet.getY(), paint);
            }
            for (Sheep sheep : sheeps){
                canvas.drawBitmap(sheep.getBitmap(), sheep.getX(), sheep.getY(), paint);
            }

            // draw score at the top
            paint.setColor(Color.BLACK);
            paint.setTextSize(bounds.y / 20);
            paint.setTypeface(bold);
            canvas.drawText("POINTS: " + points, 40, 70, paint);

            if (!playing){
                canvas.drawColor(Color.argb(150, 255, 255, 255));
                paint.setColor(Color.rgb(180, 0, 0));
                paint.setTextSize(bounds.y / 4);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStyle(Paint.Style.FILL);
                paint.setFakeBoldText(true);
                canvas.drawText(llama.isAlive() ? "PAUSED" : "GAME OVER", bounds.x / 2, bounds.y / 3, paint);
                paint.setColor(Color.YELLOW);
                if (newHigh){
                    paint.setTextSize(paint.getTextSize() / 2);
                }
                canvas.drawText((newHigh ? "NEW HIGH SCORE: " : "SCORE: ") + points,
                        bounds.x / 2, bounds.y / 3 + paint.getTextSize() + 50, paint);
                paint.setTextSize(bounds.y / 12);
                paint.setColor(Color.BLUE);
                canvas.drawText("Tap to continue", bounds.x / 2, bounds.y * 3/4, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void control(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
        if (leftoverMillis < 5){
            leftoverMillis = 5;
        }
        Log.d("MILLISECONDS: ", String.valueOf(leftoverMillis));
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
