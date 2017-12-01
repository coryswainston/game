package com.coryswainston.game.helpers;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Cory Swainston
 */

public class GestureHelper {

    private TouchPoint initialTouch = null;
    private float xDiff;
    private float yDiff;
    private final int SWIPE_THRESHOLD = 200;

    public boolean isSwipeDown(MotionEvent e) {
        return yDiff - SWIPE_THRESHOLD > 0;
    }

    public boolean isSwipeUp() {
        return SWIPE_THRESHOLD + yDiff < 0;
    }

    public boolean noSwipe() {
        return Math.abs(xDiff) < SWIPE_THRESHOLD && Math.abs(yDiff) < SWIPE_THRESHOLD;
    }

    private boolean horizontalSwipe() {
        return Math.abs(xDiff) > SWIPE_THRESHOLD;
    }

    public boolean isLeftSwipe() {
        return horizontalSwipe() && xDiff < 0;
    }

    public boolean isRightSwipe() {
        return horizontalSwipe() && xDiff > 0;
    }

    public boolean isFirstTouch() {
        return initialTouch == null;
    }

    public void up(MotionEvent e) {
        xDiff = e.getX() - initialTouch.getX();
        yDiff = e.getY() - initialTouch.getY();
        initialTouch = null;
    }

    public void down(MotionEvent e) {
        initialTouch = new TouchPoint(e.getX(), e.getY());
    }

    private class TouchPoint {

        TouchPoint() {
            this(0, 0);
        }

        TouchPoint(float x, float y) {
            setX(x);
            setY(y);
        }

        private float x;
        private float y;

        float getX() {return x;}
        float getY() {return y;}
        void setX(float x) {this.x = x;}
        void setY(float y) {this.y = y;}
    }
}