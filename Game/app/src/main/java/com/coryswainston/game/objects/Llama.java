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
        for (Sheep sheep : sheepPile) {
            sheep.setDx(0);
            sheep.setDy(0);
            if (sheep.isSeated) {
                sheep.setX(x);
            } else {
                sheep.setX(sheep.getX() + ((sheep.getX() - x) > 0 ? 1 : -1));
                sheep.setY(new Float(sheep.getA() * Math.pow(x - sheep.getX(), 2) - yFloor + y));
                Log.d("Here's some info", "A: " + sheep.getA() + ", " + sheep.getX() + ", " + sheep.getY());
                if (sheep.getY() == y) {
                    sheep.isSeated = true;
                }
            }
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
        float xDelta = sheep.getX() - x;
        double a = y / Math.pow(xDelta, 2);
        sheep.setA(a);
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
