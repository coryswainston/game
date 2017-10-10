package com.coryswainston.game.helpers;

import android.view.MotionEvent;

/**
 * @author Cory Swainston
 */

public class GestureHelper {

    private float[] initialTouchPoint = null;
    private float xDiff;
    private float yDiff;
    private final int SWIPE_THRESHOLD = 200;

    public boolean isSwipeDown(MotionEvent e) {
        return e.getY() - initialTouchPoint[1] > SWIPE_THRESHOLD;
    }

    public boolean isSwipeUp() {
        return Math.abs(yDiff) > SWIPE_THRESHOLD && yDiff < 0;
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
        return initialTouchPoint == null;
    }

    public void release(MotionEvent e) {
        xDiff = e.getX() - initialTouchPoint[0];
        yDiff = e.getY() - initialTouchPoint[1];
    }

    public void registerTouch(MotionEvent e) {
        initialTouchPoint = new float[2];
        initialTouchPoint[0] = e.getX();
        initialTouchPoint[1] = e.getY();
    }
}