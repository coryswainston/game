package com.coryswainston.game.objects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

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

    private static final int GRAVITY = 6;

    public Llama(Context context, int width){
        this.context = context;
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
        alive = true;
        initializeDrawables();
        int height = width * 9 / 10;
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
            if (drawableIdx >= 4) {
                drawableIdx -= 4;
            }
        }

        for (int i = 0; i < sheepPile.size(); i++) {
            Sheep sheep = sheepPile.get(i);
            int sheepHeight = (int)Math.round(sheep.getHeight() * .8);
            sheep.setX(drawableIdx % 2 == 0 ? x : x + width / 5);
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
            drawableIdx = facingRight() ? 0 : 1;
        }
    }

    @Override
    public Rect getHitRect() {
        return new Rect(
                getX() + width / (facingRight() ? 8 : 4) ,
                getY() + height / 3,
                getX() + width * (facingRight() ? 6 : 7) / 8,
                getY() + height);
    }

    public void jump() {
        setDy(-height / 4);
        drawableIdx = facingRight() ? 4 : 5;
    }

    public void duck() {
        ducking = true;
        if (drawableIdx < 2) {
            drawableIdx += 2;
        }
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

    public void turnLeft(){
        if (facingRight()) {
            drawableIdx++;
        }
    }

    public void turnRight() {
        if (!facingRight()) {
            drawableIdx--;
        }
    }

    public boolean isDucking(){return ducking;}

    private void initializeDrawables() {
        Resources res = context.getResources();
        drawableIdx = 0;
        drawables = new Drawable[6];
        drawables[0] = res.getDrawable(R.drawable.llama1);
        drawables[1] = res.getDrawable(R.drawable.llama2);
        drawables[2] = res.getDrawable(R.drawable.llama_duck1);
        drawables[3] = res.getDrawable(R.drawable.llama_duck2);
        drawables[4] = res.getDrawable(R.drawable.llama_jump1);
        drawables[5] = res.getDrawable(R.drawable.llama_jump2);
    }

    private boolean facingRight() {
        return drawableIdx % 2 == 0;
    }
}
