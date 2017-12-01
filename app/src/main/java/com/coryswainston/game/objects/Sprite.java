package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * @author Cory Swainston
 */

public abstract class Sprite {

    protected Bitmap[] bitmaps;
    protected float x;
    protected float y;
    protected float dx;
    protected float dy;
    protected int bitmapIdx;
    protected int height;
    protected int width;
    protected boolean alive;
    protected Context context;

    public abstract void update();

    public Bitmap getBitmap(){
        return bitmaps[bitmapIdx];
    }

    public int getX() {
        return (int)x;
    }

    public void setX(float x) { this.x = x; }

    public int getY() {
        return (int)y;
    }

    public void setY(float y) { this.y = y; }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) { this.dx = dx; }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) { this.dy = dy; }

    public void addDx(float ddx) { dx += ddx; }

    public void addDy(float ddy) { dy += ddy; }

    public void setSize(int width, int height) {
        for (int i = 0; i < bitmaps.length; i++){
            bitmaps[i] = Bitmap.createScaledBitmap(bitmaps[i], width, height, false);
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth(){return width;}

    public int getHeight(){return height;}

    public void kill(){alive = false;}

    public boolean isAlive(){return alive;}
}
