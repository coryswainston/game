package com.coryswainston.game.objects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.coryswainston.game.R;

/**
 * @author Cory Swainston
 */

public class Sheep extends Sprite implements Hittable {

    private int stepTimer;
    private boolean burnt;
    private boolean seated;
    private Drawable[][] drawables;

    public Sheep(Context context){
        x = y = dy = dx = 0;
        Resources res = context.getResources();
        drawables = new Drawable[2][4];
        drawables[0][0] = res.getDrawable(R.drawable.sheep1);
        drawables[0][1] = res.getDrawable(R.drawable.sheep2);
        drawables[0][2] = res.getDrawable(R.drawable.sheep3);
        drawables[0][3] = res.getDrawable(R.drawable.sheep4);
        drawables[1][0] = res.getDrawable(R.drawable.sheep_burnt1);
        drawables[1][1] = res.getDrawable(R.drawable.sheep_burnt2);
        drawables[1][2] = res.getDrawable(R.drawable.sheep_burnt3);
        drawables[1][3] = res.getDrawable(R.drawable.sheep_burnt4);
        drawableIdx = 0;
        alive = true;
        burnt = false;
        seated = false;
        stepTimer = 0;
    }

    @Override
    public Drawable getDrawable() {
        return drawables[burnt ? 1 : 0][drawableIdx];
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
        drawableIdx = 2;
    }

    public void turnRight(){
        drawableIdx = 0;
    }

    private void step() {
        if (drawableIdx % 2 == 0) {
            drawableIdx++;
        } else {
            drawableIdx--;
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
