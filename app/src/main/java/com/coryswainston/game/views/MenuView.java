package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.coryswainston.game.activities.GameActivity;
import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.helpers.DrawingHelper;
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

    private int flashAlpha;
    private boolean flashSwitch;

    public MenuView(Context context){
        super(context);
        this.context = context;

        flashAlpha = 255;
        flashSwitch = false;
    }

    public void draw() {
        Point bounds = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(bounds);

        DrawingHelper drawingHelper = new DrawingHelper(context, getHolder());
        if (drawingHelper.readyToDraw()) {
            Llama llama = new Llama(context, bounds.x / 6, bounds.x / 6);
            llama.setY(bounds.y / 2.3f);
            llama.setX(bounds.x * 3 / 4);

            drawingHelper.fillBackground(Color.WHITE);
            drawingHelper.draw(llama);
            drawingHelper.drawRegularText("LLAMA", bounds.y / 4, bounds.y / 6,
                                        bounds.y * 3 / 8, DrawingHelper.BLUE);
            drawingHelper.drawRegularText("LLAND", bounds.y / 4, bounds.y / 6,
                                        bounds.y * 11 / 16, DrawingHelper.BLUE);
            drawingHelper.drawRegularText("Tap to play", bounds.y / 12, bounds.y / 6,
                                        bounds.y * 7/ 8, Color.argb(flashAlpha, 0, 100, 0));

            drawingHelper.finish();
        }
    }

    public void update() {
        flashAlpha += flashSwitch ? 10 : -15;
        if (flashAlpha == 105) {
            flashSwitch = true;
        }
        if (flashAlpha == 255) {
            flashSwitch = false;
        }
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
