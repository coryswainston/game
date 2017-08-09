package com.coryswainston.game;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.solver.SolverVariable;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
    private Context context;

    private Llama llama;
    private ArrayList<Comet> comets;

    private int cometFrequency;
    private float[] initialValues;

    private final long TARGET_MILLIS = 33;

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

        // make a llama
        llama = new Llama(context);
        llama.setX(10);
        llama.setY(bounds.y - 190 - llama.getBitmap().getHeight());
        llama.setFloor(llama.getY());

        //set up comet array
        comets = new ArrayList<>();
        cometFrequency = 50;

        // set everything up for drawing
        surfaceHolder = getHolder();
        paint = new Paint();

        // initialize other members
        playing = true;
        initialValues = new float[2];
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
                llama.setDx(e.getX() > llama.getX() ? 10 : -10);

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
                    llama.setDx(xDiff > 0 ? 30 : -30);
                } else {
                    // if we swipe up, the llama jumps
                    Log.d(">", "y threshold passed");
                    llama.jump();
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
    }

    private void updateComets(){
        Comet newComet = null;
        Random random = new Random();
        if (random.nextInt(cometFrequency) == 1){
            newComet = new Comet(getContext(), random.nextInt(bounds.x - Comet.COMET_WIDTH));
        }
        if (newComet != null){
            comets.add(newComet);
        }
        for (Iterator<Comet> it = comets.iterator(); it.hasNext();){
            Comet comet = it.next();
            if (comet.getY() >= bounds.y - Comet.COMET_HEIGHT - 200){
                comet.explode();
            }
            if (!comet.alive){
                it.remove();
            }
            comet.update();
        }
    }

    private void detectCollisions(){
        final int THRESHOLD = 200;
        for(Comet comet : comets){
            if (Math.abs(comet.getX() - llama.getX()) < THRESHOLD &&
                    Math.abs(comet.getY() - llama.getY()) < THRESHOLD){
                comet.explode();
                playing = false;
            }
        }
    }

    /**
     * You know what this one does
     */
    private void draw(){
        if(surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.rgb(180, 230, 255));

            // draw the ground
            paint.setColor(Color.rgb(0, 100, 0));
            canvas.drawRect(0, bounds.y - 200, bounds.x, bounds.y, paint);

            // draw llama and comets
            Bitmap llamaBitmap = llama.getBitmap();
            if (llama.getDx() < 0){

            }
            canvas.drawBitmap(llama.getBitmap(), llama.getX(), llama.getY(), paint);
            for (Comet comet : comets) {
                canvas.drawBitmap(comet.getBitmap(), comet.getX(), comet.getY(), paint);
            }

            if (!playing){
                canvas.drawColor(Color.argb(150, 255, 255, 255));
                paint.setColor(Color.rgb(180, 0, 0));
                paint.setTextSize(250);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStyle(Paint.Style.FILL);
                paint.setFakeBoldText(true);
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), MainActivity.HANKEN_BOOK_FONT);
                paint.setTypeface(typeface);
                canvas.drawText("GAME OVER", bounds.x / 2, bounds.y / 2, paint);
                paint.setTextSize(80);
                paint.setColor(Color.BLUE);
                canvas.drawText("Tap to continue", bounds.x / 2, bounds.y / 2 + 100, paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void control(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
        if (leftoverMillis < 10){
            leftoverMillis = 10;
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
