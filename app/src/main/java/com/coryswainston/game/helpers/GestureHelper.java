package com.coryswainston.game.helpers;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Cory Swainston
 */

public class GestureHelper {

    private Touch initialTouch = null;
    private float xDiff;
    private float yDiff;
    private int numFingers;
    private final int SWIPE_THRESHOLD = 100;
    private final int TIME_THRESHOLD = 200;

    public boolean isSwipeDown() {
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

    public boolean isDoubleTouch() {
        return numFingers > 1;
    }

    public boolean isLongSwipe(MotionEvent e) {
        return e.getEventTime() - e.getDownTime() > TIME_THRESHOLD && !noSwipe();
    }

    public boolean isLongTouch(MotionEvent e) {
        return e.getEventTime() - e.getDownTime() > TIME_THRESHOLD && noSwipe();
    }

    public boolean isDoubleSwipe() {
        return isDoubleTouch() && !noSwipe();
    }

    public float getYSwipeRatio() {
        if (noSwipe()) {
            return 0;
        }
        float sum = Math.abs(xDiff) + Math.abs(yDiff);
        return yDiff / sum;
    }

    public float getXSwipeRatio() {
        if (noSwipe()) {
            return 0;
        }
        float sum = Math.abs(xDiff) + Math.abs(yDiff);
        return xDiff / sum;
    }

    public void up(MotionEvent e) {
        if (e.getPointerId(0) == initialTouch.id) {
            Log.d("GESTURE", "up called");
            numFingers = initialTouch.numFingers;
            xDiff = e.getX() - initialTouch.x;
            yDiff = e.getY() - initialTouch.y;
        } else {
            Log.d("GESTURE", "pointer up");
        }
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            initialTouch = null;
        }
    }

    public void down(MotionEvent e) {
        Log.d("GESTURE", "down called");
        if (initialTouch == null) {
            initialTouch = new Touch(e.getX(), e.getY(), e.getPointerCount(), e.getPointerId(0));
        } else {
            initialTouch.numFingers = e.getPointerCount();
        }
        if (e.getPointerCount() > 1) {
            Log.d("gesture", "Touch is more than one");
        }
    }

    private class Touch {

        Touch(float x, float y, int numFingers, int id) {
            this.x = x;
            this.y = y;
            this.numFingers = numFingers;
            this.id = id;
        }

        float x;
        float y;
        int numFingers;
        int id;
    }
}