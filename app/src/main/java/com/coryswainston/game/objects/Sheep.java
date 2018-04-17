package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.coryswainston.game.R;

/**
 * @author Cory Swainston
 */

public class Sheep extends Sprite implements Hittable {

    private int stepTimer;
    private boolean burnt;
    private boolean seated;

    public Sheep(Context context){
        x = y = dy = dx = 0;
        bitmaps = new Bitmap[4];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep2);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep3);
        bitmaps[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.sheep4);
        bitmapIdx = 0;
        alive = true;
        burnt = false;
        seated = false;
        stepTimer = 0;
    }

    @Override
    public void update(){
        x += dx;
        y += dy;

        if (stepTimer == 4){
            step();
            stepTimer = 0;
        }
        stepTimer ++;
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX(), getY(), getX() + width, getY() + height);
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

    public boolean isSeated() {
        return seated;
    }

    public void setSeated(boolean seated) {
        this.seated = seated;
    }

    public boolean isBurnt() {
        return burnt;
    }

    public void setBurnt(boolean burnt) {
        this.burnt = burnt;
    }
}
