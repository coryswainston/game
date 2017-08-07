package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

/**
 * For killin llamas
 *
 * @author Cory Swainston
 */

public class Comet {
    private Bitmap bitmap;
    private int x;
    private int y;
    private int dx;
    private int dy;
    private int yFloor;

    public Comet(Context context, int x){
        this.x = x;
        y = 0;
        dy = 40;
        dx = 0;

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama1);
    }

    public void update(){
        x += dx;
        y += dy;
    }

    public Bitmap getBitmap(){
        return bitmap;
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

    public void setFloor(int yFloor){this.yFloor = yFloor;}
}
