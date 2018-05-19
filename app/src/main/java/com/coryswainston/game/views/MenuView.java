package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.coryswainston.game.R;
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
    private Point bounds;

    private Bitmap background;

    private Llama llama;

    public MenuView(Context context){
        super(context);
        this.context = context;

        bounds = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(bounds);

        flashAlpha = 255;
        flashSwitch = false;

        background = BitmapFactory.decodeResource(getResources(), R.drawable.menu_background);
        background = Bitmap.createScaledBitmap(background, bounds.y * 4 / 3, bounds.y, false);

        llama = new Llama(context, bounds.x / 6);
        llama.setY(bounds.y * 3 /4);
        llama.setFloor(llama.getY());
        llama.setX(bounds.x * 3 / 4);
        llama.setDx(10);

    }

    public void draw() {
        DrawingHelper drawingHelper = new DrawingHelper(context, getHolder());
        if (drawingHelper.readyToDraw()) {

            drawingHelper.fillBackground(DrawingHelper.SKY_BLUE);
//            drawingHelper.drawWatermark(background);

            drawingHelper.drawMenuText("Llama", bounds.x / 5, bounds.x / 2,
                                        bounds.y / 3, DrawingHelper.WHITE);
            drawingHelper.drawMenuText("Lland", bounds.x / 5, bounds.x / 2,
                                        bounds.y / 2, DrawingHelper.WHITE);
            drawingHelper.drawMenuText("Tap to play", bounds.x / 14, bounds.x / 2,
                                        bounds.y * 3 / 5, Color.argb(flashAlpha, 0, 100, 0));

            drawingHelper.drawRectangle(0, bounds.y * 3 / 4 + llama.getHeight(), bounds.x, bounds.y, DrawingHelper.DARK_GREEN);


//            int width = llama.getWidth() / 12;
//            int shadowLeft = llama.getX() + width * (llama.facingRight() ? 1 : 2);
//            int shadowRight = llama.getX() + llama.getWidth() - width * (llama.facingRight() ? 2 : 1);
//            int shadowTop = llama.getFloor() + llama.getHeight() - width;
//            int shadowBottom = llama.getFloor() + llama.getHeight() + width;
//            drawingHelper.drawOval(shadowLeft, shadowTop, shadowRight, shadowBottom, DrawingHelper.GREY);

            drawingHelper.draw(llama);

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
        llama.update();
        if (llama.getX() < 0 || llama.getX() + llama.getWidth() > bounds.x) {
            llama.setDx(-llama.getDx());
            if (llama.getX() < 0) {
                llama.turnRight();
            } else {
                llama.turnLeft();
            }
        }

        if (llama.getX() < 700 && llama.getX() > 650) {
            llama.jump();
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
