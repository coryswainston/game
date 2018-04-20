package com.coryswainston.game.objects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

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

        Resources res = context.getResources();

        drawables = new Drawable[3];
        drawables[0] = res.getDrawable(R.drawable.comet1);
        drawables[1] = res.getDrawable(R.drawable.comet2);
        drawables[2] = res.getDrawable(R.drawable.comet3);;
    }

    @Override
    public void update(){
        x += dx;
        y += dy;
        frameCtr++;
        if (frameCtr == 4) {
            toggleDrawable();
            frameCtr = 0;
        }
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX(), getY(), getX() + width, getY() + height);
    }

    private void toggleDrawable(){
        if (drawableIdx == 2){
            alive = false;
            return;
        }
        drawableIdx = (drawableIdx == 1 ? 0 : 1);
    }

    public void explode(){
        if (!exploded) {
            drawableIdx = 2;
            frameCtr = 0;
            exploded = true;
            dy = dy / 2;
            dx = 0;
        }
    }

    public boolean isExploded(){
        return exploded;
    }
}
