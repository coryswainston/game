package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author Cory Swainston
 */

public class Llama extends Sprite{

    private int yFloor;
    private int yCeiling;
    private boolean ducking;
    private int duckTimer;

    private final int GRAVITY = 5;

    public Llama(Context context){
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
        alive = true;
        yCeiling = 400;
        bitmapIdx = 0;
        bitmaps = new Bitmap[2];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama2);
        height = bitmaps[0].getHeight();
        width = bitmaps[1].getWidth();
        duckTimer = 0;
    }

    public void update(){

        x += dx;
        y += dy;
        if (y < yFloor){
            dy += GRAVITY;
        }
        if (y < (yFloor - yCeiling) && dy < 0){
            dy = -dy;
        }
        if (y >= yFloor){
            y = yFloor;
            dy = 0;
        }
        if (duckTimer > 0){
            duckTimer--;
        } else {
            ducking = false;
        }
    }

    public void jump() {
        setDy((int)(-yCeiling / 6.5));
    }

    public void duck() {
        ducking = true;
        duckTimer = 8;
    }

    public void setFloor(int yFloor){ this.yFloor = yFloor; }

    public void setCeiling(int yCeiling) { this.yCeiling = yCeiling; }

    public void turnLeft(){ bitmapIdx = 1; }

    public void turnRight() { bitmapIdx = 0; }

    public boolean isDucking(){return ducking;}

}
