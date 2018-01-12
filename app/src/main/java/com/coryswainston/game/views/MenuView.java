package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.coryswainston.game.activities.GameActivity;
import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.objects.Llama;

/**
 * The opening menu of the game
 */

public class MenuView extends SurfaceView implements Runnable{
    private Context context;
    private Thread thread = null;
    private ViewListener viewListener;
    volatile boolean running = false;

    public MenuView(Context context){
        super(context);
        this.context = context;
    }

    public void draw(){
        SurfaceHolder surfaceHolder = getHolder();
        if(surfaceHolder.getSurface().isValid()){
            Canvas canvas = surfaceHolder.lockCanvas();
            Paint paint = new Paint();

            Point bounds = new Point();
            ((Activity)context).getWindowManager().getDefaultDisplay().getSize(bounds);

            Llama llama = new Llama(context, bounds.x / 6, bounds.x / 6);
            llama.setY(bounds.y / 2.3f);
            llama.setX(bounds.x * 3 / 4);

            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(llama.getBitmap(), llama.getX(), llama.getY(), paint);
            paint.setColor(Color.BLUE);
            paint.setTextSize(300);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "Hanken-Book.ttf"));
            canvas.drawText("LLAMA", bounds.x / 8, bounds.y / 2.3f, paint);
            canvas.drawText("LLAND", bounds.x / 8, bounds.y / 2.3f + 350, paint);
            paint.setTextSize(80);
            paint.setColor(Color.rgb(0, 100, 0));
            canvas.drawText("Tap to play", bounds.x / 2, bounds.y /2 + 680, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void update() {
        // empty
    }

    private void control(long frameTime){
        long leftoverMillis = 33 - frameTime;
        if (leftoverMillis < 10){
            leftoverMillis = 10;
        }
        try {
            thread.sleep(leftoverMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent e){
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN){
            getViewListener().onAction(new Intent());
        }
        return super.onTouchEvent(e);
    }


    public void run(){
        while(running){
            long startMillis = System.currentTimeMillis();
            update();
            draw();

            long frameTime = System.currentTimeMillis() - startMillis;
            control(frameTime);
        }
    }

    public void pause() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void setViewListener(ViewListener listener) {
        this.viewListener = listener;
    }

    public ViewListener getViewListener() {
        if (viewListener == null) {
            throw new IllegalStateException("ViewListener is null.");
        } else {
            return viewListener;
        }
    }
}
