package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Cory Swainston
 */

public class Llama {

    private Bitmap[] bitmaps;
    private int x;
    private int y;
    private int dx;
    private int dy;
    private int yFloor;
    private int bitmapIdx;

    private final int GRAVITY = 5;

    public Llama(Context context){
        x = 0;
        y = 0;
        dx = 1;
        dy = 0;

        bitmapIdx = 0;
        bitmaps = new Bitmap[2];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama2);
    }

    public void update(){
        if (dx > 0) {
            bitmapIdx = 0;
        } else if (dx < 0) {
            bitmapIdx = 1;
        }
        x += dx;
        y += dy;
        if (y < yFloor){
            dy += GRAVITY;
        }
        if (y < (yFloor - 400) && dy < 0){
            dy = -dy;
        }
        if (y >= yFloor){
            y = yFloor;
            dy = 0;
        }
    }

    public void jump() {
        setDy(50);
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

    public void setDy(int dy) { this.dy = -dy; }

    public void setFloor(int yFloor){this.yFloor = yFloor;}
}
