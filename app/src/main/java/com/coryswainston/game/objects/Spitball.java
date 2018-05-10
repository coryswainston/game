package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.coryswainston.game.R;

/**
 * Llama phlegm projectiles
 */

public class Spitball extends Sprite implements Hittable {

    private Drawable drawable;

    public Spitball(Context context, int width) {
        this.context = context;
        drawable = context.getResources().getDrawable(R.drawable.spit);
        setSize(width, width);
        alive = true;
        x = y = dx = 0;
    }

    @Override
    public void update() {
        x += dx;
        y += dy;
    }

    @Override
    public Rect getHitRect() {
        return new Rect(getX(), getY(), getX() + getWidth(), getY() + getHeight());
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }
}
