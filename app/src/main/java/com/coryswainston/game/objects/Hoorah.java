package com.coryswainston.game.objects;

import android.graphics.Bitmap;
import android.graphics.Point;

/**
 * A class for objects to be temporarily drawn
 *
 * @author coryswainston
 */

public class Hoorah {
    public static final int TIME_LONG = 75;
    public static final int TIME_MED = 50;
    public static final int TIME_SHORT = 25;

    private Point position;
    private int size;
    private int duration;
    private String text;
    private int alpha;
    private int framesLeft;

    public boolean countdown() {
        framesLeft--;

        return framesLeft != 0;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        framesLeft = duration;
    }

    public int getFramesLeft() {
        return framesLeft;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void fade(int dAlpha) {
        alpha -= dAlpha;
    }

    // TODO implement to take drawText() params and an optional background
    // TODO contain a draw() function that uses DrawingHelper.drawText()
    // TODO implement container class that draws Hoorahs until they're timed out
    // TODO create a Hoorah when picking up a sheep (+100), new high score, etc.
    // TODO potentially attach these to sound effects
}
