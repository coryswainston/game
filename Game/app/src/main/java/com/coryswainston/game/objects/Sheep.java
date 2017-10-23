package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.coryswainston.game.R;
import com.coryswainston.game.objects.Sprite;

/**
 * @author Cory Swainston
 */

public class Sheep extends Sprite {

    private int stepTimer;
    private double a;
    boolean isSeated;

    public Sheep(Context context){
        x = y = dy = dx = 0;
        bitmaps = new Bitmap[4];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep2);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep3);
        bitmaps[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep4);
        bitmapIdx = 0;
        alive = true;
        stepTimer = 0;
    }

    public void update(){
        x += dx;
        y += dy;

        if (stepTimer == 5){
            step();
            stepTimer = 0;
        }
        stepTimer ++;
    }

    public void turnLeft(){
        bitmapIdx = 2;
    }

    public void turnRight(){
        bitmapIdx = 0;
    }

    private void step(){
        switch (bitmapIdx){
            case 0:
                bitmapIdx = 1;
                break;
            case 1:
                bitmapIdx = 0;
                break;
            case 2:
                bitmapIdx = 3;
                break;
            case 3:
                bitmapIdx = 2;
                break;
        }
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getA() {
        return a;
    }
}
