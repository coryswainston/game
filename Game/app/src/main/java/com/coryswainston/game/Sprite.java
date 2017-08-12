package com.coryswainston.game;

import android.graphics.Bitmap;

/**
 * @author Cory Swainston
 */

public abstract class Sprite {

    protected Bitmap[] bitmaps;
    protected int x;
    protected int y;
    protected int dx;
    protected int dy;
    protected int bitmapIdx;
    protected int height;
    protected int width;

    public abstract void update();

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

    public void addDx(int ddx) { dx += ddx; }

    public void addDy(int ddy) { dy += ddy; }

    public void setSize(int width, int height) {
        for (int i = 0; i < bitmaps.length; i++){
            bitmaps[i] = Bitmap.createScaledBitmap(bitmaps[i], width, height, false);
        }
        this.width = width;
        this.height = height;
    }

    public int getWidth(){return width;}

    public int getHeight(){return height;}
}
