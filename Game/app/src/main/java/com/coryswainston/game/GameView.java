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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import junit.framework.Assert;

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

    public GameView(Context context){
        super(context);

        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);

        llama = new Llama(context);
        llama.setX(10);
        llama.setY(bounds.y - 580);
        surfaceHolder = getHolder();
        paint = new Paint();
        playing = true;
    }

    public void run(){
        while (playing){
            update();
            draw();
            control();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch(motionEvent.getAction() & motionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                llama.setDx(0);
                break;
            case MotionEvent.ACTION_DOWN:
                if (motionEvent.getAxisValue(MotionEvent.AXIS_X) <= llama.getX()) {
                    llama.setDx(-10);
                } else {
                    llama.setDx(10);
                }

                break;
        }
        return true;
    }

    private void update(){
        llama.update();
    }

    private void draw(){
        if(surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);

            paint.setColor(Color.BLACK);
            canvas.drawRect(0, bounds.y - 200, bounds.x, bounds.y, paint);

            canvas.drawBitmap(llama.getBitmap(), llama.getX(), llama.getY(), paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
            AnimationDrawable a;

            if (Build.VERSION.SDK_INT >= 22) {
                a = (AnimationDrawable) getContext().getResources().getDrawable(R.drawable.char_animation, null);
            } else {
                a = (AnimationDrawable) getContext().getResources().getDrawable(R.drawable.char_animation);
            }
            Assert.assertNotNull(a);
            a.setBounds(0, 0, 100, 100);
            a.setCallback(new Drawable.Callback() {
                @Override
                public void invalidateDrawable(@NonNull Drawable who) {

                }

                @Override
                public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {

                }

                @Override
                public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {

                }
            });
            a.draw(canvas);
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

}
