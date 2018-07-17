package com.coryswainston.game.helpers;

import android.content.Context;
import android.graphics.Point;

import com.coryswainston.game.sprites.Cloud;
import com.coryswainston.game.sprites.Comet;
import com.coryswainston.game.sprites.Llama;
import com.coryswainston.game.sprites.Sheep;
import com.coryswainston.game.sprites.Ufo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Static methods to create new Sprites
 */

public class SpriteFactory {

    private Point bounds;
    private int floor;
    private Context context;
    private Random random;

    public SpriteFactory(Point bounds, int floor, Context context) {
        this.bounds = bounds;
        this.floor = floor;
        this.context = context;
        this.random = new Random();
    }

    public Llama buildLlama() {
        int width = bounds.x / 6;
        Llama llama = new Llama(context, width);
        llama.setX(bounds.x / 2);
        llama.setY(floor - llama.getHeight());
        llama.setFloor(llama.getY());

        return llama;
    }

    public Comet buildComet() {
        Random random = new Random();
        Comet newComet = new Comet(context, random.nextInt(bounds.x));
        int height = (int) (bounds.x / 6.5);
        int width = (int) (height * 0.6);
        newComet.setSize(width, height);
        int speed = bounds.x / 75;
        int degrees = random.nextInt(20) - 10;
        int angle = 90 - degrees;
        float dx = speed * -(float)(Math.cos((float)Math.PI / 180.0f * angle));
        float dy = speed * (float)(Math.sin((float)Math.PI / 180.0f * angle));
        newComet.setDy(dy);
        newComet.setDx(dx);
        newComet.setRotation(degrees / 3);
        newComet.setY(-newComet.getHeight());

        return newComet;
    }

    public Ufo buildUfo() {
        Ufo ufo = new Ufo(context);

        ufo.setSize(bounds.x / 4, bounds.x / 8);
        ufo.setX(new Random().nextInt(bounds.x - ufo.getWidth()));
        ufo.setY(-ufo.getHeight());
        ufo.setDy(25);
        ufo.setDx(0);
        ufo.setFloor(floor);

        return ufo;
    }

    public Sheep buildSheep() {
        Sheep sheep = new Sheep(context);
        sheep.setSize(bounds.x / 7, bounds.x / 10);
        sheep.setX(random.nextInt(2) == 1 ? bounds.x : -sheep.getWidth());
        sheep.setY(floor - sheep.getHeight());
        sheep.setDx(sheep.getX() > 0 ? -5 : 5);
        if (sheep.getDx() < 0) {
            sheep.turnLeft();
        }

        return sheep;
    }

    public List<Cloud> buildClouds() {
        List<Cloud> clouds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Cloud cloud = new Cloud(context);
            cloud.setSize(bounds.x / 6, bounds.x / 8);
            cloud.setDx((float) (.4));
            clouds.add(i, cloud);
        }
        clouds.get(0).setX(bounds.x / 2);
        clouds.get(1).setX(bounds.x * 7 / 8);
        clouds.get(2).setX(bounds.x / 6);
        clouds.get(0).setY(bounds.y * 3 / 5);
        clouds.get(1).setY(bounds.y * 2 / 5);
        clouds.get(2).setY(bounds.y / 5);

        return clouds;
    }
}
