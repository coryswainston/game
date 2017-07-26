package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Cory Swainston
 */

public class Llama {

    private Bitmap bitmap;
    private int x;
    private int y;
    private int dx;
    private int dy;

    public Llama(Context context){
        x = 0;
        y = 0;
        dx = 1;
        dy = 0;

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

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
