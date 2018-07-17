package com.coryswainston.game.sprites;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.coryswainston.game.R;

/**
 * @author Cory Swainston
 */

public class Cloud extends Sprite {
    public Cloud(Context context){
        drawables = new Drawable[1];
        drawables[0] = context.getResources().getDrawable(R.drawable.cloud);
        drawableIdx = 0;
    }

    public void update(){
        x += dx;
    }
}
