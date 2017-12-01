package com.coryswainston.game.objects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.coryswainston.game.R;

/**
 * @author Cory Swainston
 */

public class Cloud extends Sprite {
    public Cloud(Context context){
        bitmaps = new Bitmap[1];
        bitmaps[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.cloud);
        bitmapIdx = 0;
    }

    public void update(){
        x += dx;
    }
}
