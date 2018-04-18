package com.coryswainston.game.objects;

import android.content.Context;
import android.content.res.Resources;
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
    private Bitmap[][] bitmaps;

    public Sheep(Context context){
        x = y = dy = dx = 0;
        Resources res = context.getResources();
        bitmaps = new Bitmap[2][4];
        bitmaps[0][0] = BitmapFactory.decodeResource(res, R.drawable.sheep1);
        bitmaps[0][1] = BitmapFactory.decodeResource(res, R.drawable.sheep2);
        bitmaps[0][2] = BitmapFactory.decodeResource(res, R.drawable.sheep3);
        bitmaps[0][3] = BitmapFactory.decodeResource(res, R.drawable.sheep4);
        bitmaps[1][0] = BitmapFactory.decodeResource(res, R.drawable.sheep_burnt1);
        bitmaps[1][1] = BitmapFactory.decodeResource(res, R.drawable.sheep_burnt2);
        bitmaps[1][2] = BitmapFactory.decodeResource(res, R.drawable.sheep_burnt3);
        bitmaps[1][3] = BitmapFactory.decodeResource(res, R.drawable.sheep_burnt4);
        bitmapIdx = 0;
        alive = true;
        burnt = false;
        seated = false;
        stepTimer = 0;
    }

    @Override
    public Bitmap getBitmap() {
        return bitmaps[burnt ? 1 : 0][bitmapIdx];
    }

    @Override
    public void setSize(int width, int height) {
        for (int i = 0; i < bitmaps.length; i++) {
            for (int j = 0; j < bitmaps[i].length; j++) {
                bitmaps[i][j] = Bitmap.createScaledBitmap(bitmaps[i][j], width, height, false);
            }
        }
        this.width = width;
        this.height = height;
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
        return new Rect(getX() + width / 10, getY(), getX() + width * 9 / 10, getY() + height);
    }

    public void turnLeft() {
        bitmapIdx = 2;
    }

    public void turnRight(){
        bitmapIdx = 0;
    }

    private void step() {
        if (bitmapIdx % 2 == 0) {
            bitmapIdx++;
        } else {
            bitmapIdx--;
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

    public void burn() {
        if (!burnt) {
            burnt = true;
        }
    }
}
