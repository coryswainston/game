package com.coryswainston.game;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Cory Swainston
 */

public class GameView extends SurfaceView implements Runnable {

    volatile boolean playing;
    private Thread gameThread = null;
    private Paint paint;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Llama llama;
    private Point bounds;
    private GestureDetector mGestureDetector;
    private ArrayList<Comet> comets;

    private float[] initialValues;

    public GameView(Context context){
        super(context);

        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);

        llama = new Llama(context);
        llama.setX(10);
        llama.setY(bounds.y - 190 - llama.getBitmap().getHeight());
        llama.setFloor(llama.getY());
        surfaceHolder = getHolder();
        paint = new Paint();
        playing = true;
        initialValues = new float[2];
        comets = new ArrayList<>();

        CustomGestureDetector gd = new CustomGestureDetector();
        mGestureDetector = new GestureDetector(context, gd);
    }

    public void run(){
        while (playing){
            update();
            draw();
            control();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){

        final float THRESHOLD = 200;

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                llama.setDx(e.getX() > llama.getX() ? 10 : -10);

                initialValues[0] = e.getX();
                initialValues[1] = e.getY();

                return true;
            case MotionEvent.ACTION_UP:
                float endX = e.getX();
                float endY = e.getY();

                float xDiff = endX - initialValues[0];
                float yDiff = endY - initialValues[1];
                Log.d("ACTION UP", "xDiff = " + xDiff + ", yDiff = " + yDiff);

                if (Math.abs(xDiff) < THRESHOLD && Math.abs(yDiff) < THRESHOLD) {
                    llama.setDx(0);
                    Log.d(">", "Didn't pass threshold");
                } else if (Math.abs(xDiff) > THRESHOLD) {
                    Log.d(">", "x threshold passed");
                    llama.setDx(xDiff > 0 ? 30 : -30);
                } else {
                    Log.d(">", "y threshold passed");
                    llama.jump();
                }

                break;
        }

        return super.onTouchEvent(e);
    }

    private void update(){
        llama.update();
        Comet newComet = null;
        Random random = new Random();
        if (random.nextInt(50) == 1){
            newComet = new Comet(getContext(), random.nextInt(bounds.x - newComet.getBitmap().getWidth()));
        }
        if (newComet != null){
            comets.add(newComet);
        }
        for (Comet comet : comets){
            comet.update();
        }
    }

    private void draw(){
        if(surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);

            paint.setColor(Color.BLACK);
            canvas.drawRect(0, bounds.y - 200, bounds.x, bounds.y, paint);

            canvas.drawBitmap(llama.getBitmap(), llama.getX(), llama.getY(), paint);
            for (Comet comet : comets) {
                canvas.drawBitmap(comet.getBitmap(), comet.getX(), comet.getY(), paint);
            }

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    private void control(){
        try {
            gameThread.sleep(17);
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

    private class CustomGestureDetector implements GestureDetector.OnGestureListener,
            GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("Gesture ", " onDown");
            if (e.getAxisValue(MotionEvent.AXIS_X) < llama.getX()){
                llama.setDx(-10);
            } else {
                llama.setDx(10);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("Gesture ", " onSingleTapConfirmed");
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d("Gesture ", " onSingleTapUp");
            llama.setDx(0);
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d("Gesture ", " onShowPress");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d("Gesture ", " onDoubleTap");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d("Gesture ", " onDoubleTapEvent");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d("Gesture ", " onLongPress");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            Log.d("Gesture ", " onScroll");
            if (e1.getY() < e2.getY()){
                Log.d("Gesture ", " Scroll Down");
            }
            if(e1.getY() > e2.getY()){
                Log.d("Gesture ", " Scroll Up");
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() < e2.getX()) {
                Log.d("Gesture ", "Left to Right swipe: "+ e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityX) + " pixels/second");
            }
            if (e1.getX() > e2.getX()) {
                Log.d("Gesture ", "Right to Left swipe: "+ e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityX) + " pixels/second");
            }
            if (e1.getY() < e2.getY()) {
                Log.d("Gesture ", "Up to Down swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityY) + " pixels/second");
            }
            if (e1.getY() > e2.getY()) {
                Log.d("Gesture ", "Down to Up swipe: " + e1.getX() + " - " + e2.getX());
                Log.d("Speed ", String.valueOf(velocityY) + " pixels/second");
            }
            return true;

        }
    }
}
