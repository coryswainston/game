package com.coryswainston.game.objects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.coryswainston.game.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * It's a spaceship
 */

public class Ufo extends Sprite implements Hittable {
    private List<Sheep> sheeps;
    private int counter;
    private static final int FRAMES = 5;
    private int floor;
    private int beamPos;

    public Ufo(Context context) {
        this.context = context;
        sheeps = new ArrayList<>();
        alive = true;

        Resources res = context.getResources();
        drawables = new Drawable[2];
        drawables[0] = res.getDrawable(R.drawable.ufo1);
        drawables[1] = res.getDrawable(R.drawable.ufo2);
        drawableIdx = 0;
        counter = 0;
    }

    @Override
    public void update() {

        if (counter++ == FRAMES) {
            counter = 0;
            drawableIdx = drawableIdx == 0 ? 1 : 0;
        }

        x += dx;
        if (y <= 0) {
            y += dy;
            beamPos = getY() + height;
        } else {
            if (beamPos + dy < floor) {
                beamPos += dy;
            } else {
                beamPos = floor;
            }
        }
        if (!sheeps.isEmpty()) {
            for(Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
                Sheep sheep = it.next();
                sheep.setY(sheep.getY() - 30);
                if (sheep.getY() + sheep.getHeight() < y + height) {
                    it.remove();
                }
            }
            if (sheeps.isEmpty()) {
                alive = false;
            }
        }
    }

    @Override
    public Drawable getDrawable() {
        return drawables[drawableIdx];
    }

//    @Override
//    public void kill() {
//        dy = -dy;
//        floor = height;
//    }

    public List<Sheep> getSheep() {
        return sheeps;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX() + width / 5, getY() + height, getX() + width * 4  / 5, beamPos);
    }
}
