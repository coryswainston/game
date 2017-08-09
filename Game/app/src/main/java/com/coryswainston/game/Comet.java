package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * For killin llamas
 *
 * @author Cory Swainston
 */

public class Comet {
    private Bitmap[] bitmaps;
    private int x;
    private int y;
    private int dx;
    private int dy;
    private int frameCtr;
    private int bitmapIdx;
    public boolean alive;

    public static final int COMET_WIDTH = 60;
    public static final int COMET_HEIGHT = 90;

    public Comet(Context context, int x){
        this.x = x;
        y = 0;
        dy = 30;
        dx = (int)(Math.random() * 20) - 10;
        frameCtr = 0;
        alive = true;

        bitmaps = new Bitmap[3];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet2);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet2);
    }

    public void update(){
        x += dx;
        y += dy;
        frameCtr++;
        if (frameCtr == 5) {
            toggleBitmap();
            frameCtr = 0;
        }
    }

    public Bitmap getBitmap(){
        return bitmaps[bitmapIdx];
    }

    public int getX() {
        return x;
    }

    public void setX(int x) { this.x = x; }

    public int getY() {
        return y;
    }

    public void setY(int y) { this.y = y; }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) { this.dx = dx; }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) { this.dy = dy; }

    private void toggleBitmap(){
        if (bitmapIdx == 2){
            alive = false;
            return;
        }
        bitmapIdx = (bitmapIdx == 1 ? 0 : 1);
    }

    public void explode(){
        bitmapIdx = 2;
        dy = 0;
    }
}
