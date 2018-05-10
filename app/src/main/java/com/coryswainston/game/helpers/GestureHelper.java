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
    private boolean gestureCompleted = false;
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

    public boolean isDoubleTouch() {
        return numFingers > 1;
    }

    public void up(MotionEvent e) {
        Log.d("GESTURE", "up called");
        numFingers = initialTouch.getNumFingers();
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            Log.d("GESTURE", "action up");
            if (numFingers < 1) {
                xDiff = e.getX() - initialTouch.getX();
                yDiff = e.getY() - initialTouch.getY();
            }
            gestureCompleted = true;
            initialTouch = null;
        } else if (e.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            Log.d("GESTURE", "pointer up");
            xDiff = e.getX() - initialTouch.getX();
            yDiff = e.getY() - initialTouch.getY();
        } else {
            throw new IllegalStateException("Must call up() on ACTION_UP or ACTION_POINTER_UP");
        }
    }

    public void down(MotionEvent e) {
        Log.d("GESTURE", "down called");
        initialTouch = new Touch(e.getX(), e.getY(), e.getPointerCount());
        if (e.getPointerCount() > 1) {
            Log.d("gesture", "Touch is more than one");
        }
        gestureCompleted = false;
    }

    private class Touch {

        Touch() {
            this(0, 0);
        }

        Touch(float x, float y) {
            this(x, y, 1);
        }

        Touch(float x, float y, int numFingers) {
            this.x = x;
            this.y = y;
            this.numFingers = numFingers;
        }

        private float x;
        private float y;
        private int numFingers;

        float getX() {return x;}
        float getY() {return y;}
        void setX(float x) {this.x = x;}
        void setY(float y) {this.y = y;}
        int getNumFingers() {return numFingers;}
        void setNumFingers(int numFingers) {this.numFingers = numFingers;}
    }
}