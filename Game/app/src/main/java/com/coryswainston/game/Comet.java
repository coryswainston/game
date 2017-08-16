package com.coryswainston.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * For killin llamas
 *
 * @author Cory Swainston
 */

public class Comet extends Sprite{

    private int frameCtr;
    private boolean exploded;

    public Comet(Context context, int x){
        this.x = x;
        y = 0;
        dy = 20;
        dx = (int)(Math.random() * 20) - 10;
        frameCtr = 0;
        alive = true;
        exploded = false;

        bitmaps = new Bitmap[3];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet2);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet3);
        width = 60;
        height = 90;
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

    private void toggleBitmap(){
        if (bitmapIdx == 2){
            alive = false;
            return;
        }
        bitmapIdx = (bitmapIdx == 1 ? 0 : 1);
    }

    public void explode(){
        bitmapIdx = 2;
        frameCtr = 0;
        exploded = true;
        dy = 0;
        dx = 0;
    }

    public boolean isExploded(){
        return exploded;
    }
}
