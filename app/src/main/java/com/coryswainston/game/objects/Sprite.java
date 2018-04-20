package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * @author Cory Swainston
 */

public abstract class Sprite {

    protected Drawable[] drawables;
    protected float x;
    protected float y;
    protected float dx;
    protected float dy;
    protected int drawableIdx;
    protected int height;
    protected int width;
    protected boolean alive;
    protected Context context;
    protected int rotation;

    public abstract void update();

    public Drawable getDrawable(){
        return drawables[drawableIdx];
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
        this.width = width;
        this.height = height;
    }

    public int getWidth(){return width;}

    public int getHeight(){return height;}

    public void kill(){alive = false;}

    public boolean isAlive(){return alive;}

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
}
