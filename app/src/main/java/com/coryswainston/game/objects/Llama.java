package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.coryswainston.game.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cory Swainston
 */

public class Llama extends Sprite implements Hittable {

    private int floor;
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
        initializeBitmaps();
        setSize(width, height);
        duckTimer = 0;
    }

    @Override
    public void update(){
        x += dx;
        y += dy;
        if (y < floor){
            dy += GRAVITY;
        }
        if (y >= floor){
            y = floor;
            dy = 0;
        }

        for (int i = 0; i < sheepPile.size(); i++) {
            Sheep sheep = sheepPile.get(i);
            int sheepHeight = (int)Math.round(sheep.getHeight() * .8);
            sheep.setX(x);
            final int sheepVelocity = 40;
            float targetY = y - height / 4 - sheepHeight * i;
            if (sheep.getY() != targetY) {
                if (sheep.getY() - targetY > sheepVelocity) {
                    sheep.setY(sheep.getY() - sheepVelocity);
                } else {
                    sheep.setY(targetY);
                    sheep.setSeated(true);
                }
            }
        }

        duckTimer += ducking ? 1 : 0;
        if (duckTimer == 5) {
            ducking = false;
            duckTimer = 0;
        }
    }

    @Override
    public Rect getHitRect() {
        return new Rect(
                getX() + width / 8,
                getY() + height / 3,
                getX() + width * 7 / 8,
                getY() + height);
    }

    public void jump() {
        setDy(-height / 4);
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

    public int getPileSize() {
        int size = 0;
        for (Sheep sheep : sheepPile) {
            if (sheep.isSeated()) {
                size++;
            }
        }
        return size;
    }

    public void setFloor(int floor){ this.floor = floor; }

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
