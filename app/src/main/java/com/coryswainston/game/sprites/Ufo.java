package com.coryswainston.game.sprites;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.coryswainston.game.R;
import com.coryswainston.game.objects.Hittable;

import java.util.ArrayList;
import java.util.Collections;
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
    private int numSheeps;
    private boolean leaving;

    public Ufo(Context context) {
        this.context = context;
        sheeps = new ArrayList<>();
        alive = true;
        leaving = false;
        numSheeps = 0;

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

        if (!leaving) {
            if (y <= height) {
                y += dy;
                beamPos = getY() + height;
            } else {
                if (beamPos + dy < floor) {
                    beamPos += dy;
                } else {
                    beamPos = floor;
                }
            }
        } else {
            if (beamPos > getY() + height) {
                if (beamPos + dy > getY() + height) {
                    beamPos += dy;
                } else {
                    beamPos = getY() + height;
                }
            } else {
                if (y >= 0) {
                    y += dy;
                    beamPos = getY() + height;
                } else {
                    alive = false;
                }
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
            if (numSheeps >= 10) {
                leave();
            }
        }
    }

    @Override
    public Drawable getDrawable() {
        return drawables[drawableIdx];
    }

    private void leave() {
        if (!leaving) {
            leaving = true;
            dy = -dy;
        }
    }

    public List<Sheep> getSheep() {
        return Collections.unmodifiableList(sheeps);
    }

    public void addSheep(Sheep sheep) {
        sheeps.add(sheep);
        numSheeps++;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX() + width / 5, getY() + height, getX() + width * 4  / 5, beamPos);
    }

    public boolean beamContains(Sheep s) {
        Rect beam = getHitRect();
        Rect r = s.getHitRect();
        float dx = s.getDx();

        int left = Math.min(beam.left, beam.left + (int)dx);
        int right = Math.max(beam.right, beam.right + (int)dx);

        return beam.top <= r.top
                && beam.bottom >= r.bottom
                && right >= r.right
                && left <= r.left;
    }
}
