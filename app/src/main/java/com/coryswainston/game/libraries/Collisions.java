package com.coryswainston.game.libraries;

import android.graphics.Point;

import com.coryswainston.game.objects.Hittable;
import com.coryswainston.game.sprites.Sprite;

/**
 * Class of helper methods
 */
public class Collisions {

    public static boolean intersect(Hittable h1, Hittable h2) {
        return h1.getHitRect().intersect(h2.getHitRect());
    }

    public static boolean willHitRightWall(Point bounds, Sprite s) {
        return s.getX() > bounds.x - s.getWidth() && s.getDx() > 0;
    }

    public static boolean willHitLeftWall(Point bounds, Sprite s) {
        return s.getX() < 0 && s.getDx() < 0;
    }
}
