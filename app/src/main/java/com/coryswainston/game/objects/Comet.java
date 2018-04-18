package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.coryswainston.game.R;

/**
 * For killin llamas
 *
 * @author Cory Swainston
 */

public class Comet extends Sprite implements Hittable {

    private int frameCtr;
    private boolean exploded;

    public Comet(Context context, int x){
        this.x = x;

        frameCtr = 0;
        alive = true;
        exploded = false;

        bitmaps = new Bitmap[3];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet2);
        bitmaps[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.comet3);
    }

    @Override
    public void update(){
        x += dx;
        y += dy;
        frameCtr++;
        if (frameCtr == 4) {
            toggleBitmap();
            frameCtr = 0;
        }
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX(), getY(), getX() + width, getY() + height);
    }

    private void toggleBitmap(){
        if (bitmapIdx == 2){
            alive = false;
            return;
        }
        bitmapIdx = (bitmapIdx == 1 ? 0 : 1);
    }

    public void explode(){
        if (!exploded) {
            bitmapIdx = 2;
            frameCtr = 0;
            exploded = true;
            dy = dy / 2;
            dx = 0;
        }
    }

    public void rotate(int degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);

        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = Bitmap.createBitmap(bitmaps[i], 0, 0, width, height, m, true);
        }
    }

    public boolean isExploded(){
        return exploded;
    }
}
