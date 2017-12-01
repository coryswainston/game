package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.coryswainston.game.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cory Swainston
 */

public class Llama extends Sprite {

    private int yFloor;
    private int yCeiling;
    private boolean ducking;
    private int duckTimer;
    private List<Sheep> sheepPile = new ArrayList<>();

    private final int GRAVITY = 5;
    private final double EIGHTY_PERCENT = .8;

    public Llama(Context context, int height, int width){
        this.context = context;
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
        alive = true;
        yCeiling = 400;
        initializeBitmaps();
        setSize(width, height);
        duckTimer = 0;
    }

    @Override
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
        for (int i = 0; i < sheepPile.size(); i++) {
            sheepPile.get(i).setDx(0);
            sheepPile.get(i).setDy(0);
            sheepPile.get(i).setX(x);
            int sheepHeight = (int)Math.round(sheepPile.get(i).getHeight() * EIGHTY_PERCENT);
            sheepPile.get(i).setY(y - sheepHeight - sheepHeight * i);
        }

        duckTimer += ducking ? 1 : 0;
        if (duckTimer == 10) {
            ducking = false;
            duckTimer = 0;
        }
    }

    public void jump() {
        setDy((int)(-yCeiling / 6.5));
    }

    public void duck() {
        ducking = true;
    }

    public List<Sheep> getSheepPile() {
        return sheepPile;
    }

    public void addToPile(Sheep sheep) {
        sheepPile.add(sheep);
    }

    public void setFloor(int yFloor){ this.yFloor = yFloor; }

    public void setCeiling(int yCeiling) { this.yCeiling = yCeiling; }

    public void turnLeft(){ bitmapIdx = 1; }

    public void turnRight() { bitmapIdx = 0; }

    public boolean isDucking(){return ducking;}

    private void initializeBitmaps() {
        bitmapIdx = 0;
        bitmaps = new Bitmap[2];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama1);
        bitmaps[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.llama2);
    }
}
