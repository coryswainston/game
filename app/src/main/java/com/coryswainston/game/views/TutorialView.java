package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coryswainston.game.GameConditions;
import com.coryswainston.game.R;
import com.coryswainston.game.helpers.DrawingHelper;
import com.coryswainston.game.helpers.GestureHelper;
import com.coryswainston.game.helpers.HoorahManager;
import com.coryswainston.game.helpers.SpriteFactory;
import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.libraries.Collisions;
import com.coryswainston.game.sprites.Cloud;
import com.coryswainston.game.sprites.Comet;
import com.coryswainston.game.sprites.Llama;
import com.coryswainston.game.sprites.Sheep;
import com.coryswainston.game.sprites.Spitball;
import com.coryswainston.game.sprites.Ufo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.coryswainston.game.Game.HIGH_SCORE;
import static com.coryswainston.game.Game.LLAMA_PREFS;
import static com.coryswainston.game.Game.TARGET_MILLIS;
import static com.coryswainston.game.objects.Hoorah.TIME_MED;
/**
 * View that handles the game
 *
 * @author Cory Swainston
 */
public class TutorialView extends SurfaceView implements Runnable {

    private GameConditions gc;

    volatile boolean playing = true;
    private Thread gameThread = null;
    private SurfaceHolder surfaceHolder = getHolder();
    GestureHelper gestureHelper = new GestureHelper();
    private Point bounds;
    private int yFloor;
    private HoorahManager hoorahManager;
    private final Drawable LLAMA_FACE;

    private Context context;
    private ViewListener finishListener;
    private SharedPreferences sharedPreferences;

    private SpriteFactory spriteFactory;
    private Llama llama;
    private Rect aimer;
    private Ufo ufo;
    private List<Spitball> spitballs = new ArrayList<>();
    private List<Comet> comets = new ArrayList<>();
    private List<Sheep> sheeps = new ArrayList<>();
    private List<Cloud> clouds = new ArrayList<>(3);

    private List<String> instructions;
    private Iterator<String> instructionIt;
    private String instruction;

    /**
     * Constructor, initializes everything for the game.
     *
     */
    public TutorialView(Context context) {
        super(context);
        this.context = context;

        setUpBoundaries();
        this.gc = new GameConditions();
        hoorahManager = new HoorahManager(bounds);

        spriteFactory = new SpriteFactory(bounds, yFloor, context);
        llama = spriteFactory.buildLlama();
        clouds = spriteFactory.buildClouds();
        LLAMA_FACE = context.getResources().getDrawable(R.drawable.llama1);

        instructions = new ArrayList<>();
        instructions.add("Swipe left or right to \nskate across the screen.");
        instructions.add("Swipe down to pick up \nsheep.");
        instructions.add("Swipe up to jump.");
        instructions.add("Swipe and hold in any \ndirection to spit.");
        instructionIt = instructions.iterator();
        instruction = instructionIt.next();

    }

    private void setUpBoundaries() {
        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);
        yFloor = bounds.y - bounds.y / 5;
        surfaceHolder.setFixedSize(bounds.x, bounds.y);
    }

    /**
     * The game loop
     */
    public void run(){
        while (playing) {
            long startMillis = System.nanoTime() / 1000000;
            update();
            draw();

            long frameTime = (System.nanoTime() / 1000000) - startMillis;
            controlFrameRate(frameTime);
        }

        while (!surfaceHolder.getSurface().isValid()) {
            Log.d("Run", "Surface not valid. Waiting 100ms...");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        draw();
    }

    /**
     * Handles user input through touch
     *
     * @param e the motion event
     * @return whether event is consumed
     */
    @Override
    @SuppressWarnings("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent e){

        aimer = null;

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: // when finger hits the screen
                gestureHelper.down(e);
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                gestureHelper.down(e);
                return true;
            case MotionEvent.ACTION_MOVE:
                gestureHelper.up(e);
                if (gestureHelper.isLongSwipe(e)) {
                    float xRatio = gestureHelper.getXSwipeRatio();
                    float yRatio = gestureHelper.getYSwipeRatio();

                    Log.d("gesture", "Here will be targeting. x=" + xRatio + ", y=" + yRatio);

                    int llamaX = llama.getHeadX();
                    int llamaY = llama.getMouthY();

                    int xDist = (xRatio > 0 ? 0 : bounds.x) - llamaX;
                    int yDist = (yRatio > 0 ? 0 : bounds.y) - llamaY;
                    if (Math.abs(xRatio) > Math.abs(yRatio)) {
                        yDist = (int)(xDist * (yRatio / xRatio));
                    } else {
                        xDist = (int)(yDist * (xRatio / yRatio));
                    }


                    aimer = new Rect(llamaX, llamaY + yDist, llamaX + xDist, llamaY);
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                gestureHelper.up(e);
                return true;
            case MotionEvent.ACTION_UP: // when finger releases
                gestureHelper.up(e);

                if (gestureHelper.isLongSwipe(e)) {
                    Spitball s = new Spitball(context, bounds.x / 80);
                    s.setX(llama.getHeadX());
                    s.setY(llama.getMouthY());
                    float velocity = -bounds.x / 20;
                    s.setDy(gestureHelper.getYSwipeRatio() * velocity);
                    s.setDx(gestureHelper.getXSwipeRatio() * velocity);
                    spitballs.add(s);
                } else if (gestureHelper.noSwipe()) {
                    if (e.getX() > bounds.x * 3 / 8 && e.getY() > yFloor) {
                        if (e.getX() > bounds.x - 400 && e.getY() > bounds.y - 200) {
                            Log.d("GameView", "Bout to return with score " + gc.getPoints());
                            Intent intent = new Intent();
                            getFinishListener().onAction(intent);
                        } else {
                            if (instructionIt.hasNext()) {
                                instruction = instructionIt.next();
                            } else {
                                instructionIt = instructions.iterator();
                            }
                        }
                    }
                    llama.setDx(0);
                } else if (gestureHelper.isSwipeUp()) {    // Order is important here because
                    llama.jump();                          // diagonal swipes are more likely to
                } else if (gestureHelper.isSwipeDown()) {  // be jumps or ducks. Vertical distance
                    llama.duck();                          // should be checked for first.
                } else if (gestureHelper.isRightSwipe()){
                    llama.turnRight();
                    llama.setDx(30);
                } else if (gestureHelper.isLeftSwipe()) {
                    llama.turnLeft();
                    llama.setDx(-30);
                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * Move everything along
     */
    private void update(){
        updateSpitballs();
        updateComets();
        detectCollisions();
        updateClouds();
        updateSheep();
        checkBounds();
        llama.update();
        updateUfo();
        gc.passFrame();
    }

    private void updateUfo() {
        if (llama.getPileSize() >= 7 && ufo == null) {
            Log.d("GameView", "Creating UFO");
            ufo = spriteFactory.buildUfo();
        }
        if (ufo != null && !ufo.isAlive()) {
            ufo = null;
        }
        if (ufo != null) {
            ufo.update();
        }
    }

    private void updateSpitballs() {
        for (Iterator<Spitball> it = spitballs.iterator(); it.hasNext();) {
            Spitball s = it.next();

            s.update();
            if (s.getY() < 0) {
                s.kill();
            }
            if (!s.isAlive()) {
                it.remove();
            }
        }
    }

    private void updateClouds(){
        for (Cloud cloud : clouds){
            if (cloud.getX() < bounds.x){
                cloud.update();
            } else {
                cloud.setX(0 - cloud.getWidth());
            }
        }
    }

    private void updateSheep() {

    }

    private void updateComets() {
    }

    private void detectCollisions() {
        List<Sheep> allSheeps = new ArrayList<>();
        allSheeps.addAll(sheeps);
        allSheeps.addAll(llama.getSheepPile());
        for(Sheep sheep : allSheeps) {
            for (Comet comet : comets) {
                if (Collisions.intersect(sheep, comet)) {
                    if (!comet.isExploded()) {
                        comet.explode();
                        sheep.setSeated(false);
                        if (!sheep.isBurnt()) {
                            gc.subtractPoints(50);
                            hoorahManager.makeHoorah(new Point(comet.getX(), comet.getY()),
                                    HoorahManager.FontSize.SMALL,
                                    TIME_MED,
                                    "-50");
                            sheep.burn();
                        } else {
                            sheep.kill();
                            sheep.setRotation(180);
                            sheep.setX(sheep.getX() + sheep.getWidth());
                            sheep.setY(sheep.getY() + sheep.getHeight() / 2);
                            gc.loseLife();
                        }
                    }
                }
            }
            if (ufo != null && ufo.beamContains(sheep)) {
                ufo.addSheep(sheep);
                if (!sheeps.remove(sheep)) {
                    llama.getSheepPile().remove(sheep);
                }
                sheep.setX(ufo.getHitRect().centerX() - sheep.getWidth() / 2);
            }
        }

        for (Comet comet: comets) {
            if (Collisions.intersect(comet, llama)) {
                comet.explode();
                gc.setGameLost(true);
                playing = false;
                llama.kill();
            }

            for (Spitball spitball : spitballs) {
                if (Collisions.intersect(comet, spitball)) {
                    comet.explode();
                    spitball.kill();
                    hoorahManager.makeHoorah(new Point(comet.getX(), comet.getY()),
                            HoorahManager.FontSize.SMALL,
                            TIME_MED,
                            "+25");
                    gc.addPoints(25);
                }
            }
        }
        for(Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            Point sheepCenter = new Point(sheep.getX() + sheep.getWidth() / 2, sheep.getY());
            if (Collisions.intersect(llama, sheep) && llama.isDucking()) {
                it.remove();
                llama.addToPile(sheep);
                gc.addPoints(100);
                hoorahManager.makeHoorah(sheepCenter, HoorahManager.FontSize.SMALL, TIME_MED,
                        "+100");
            }
        }
    }

    private void checkBounds(){
        for (Sheep sheep : sheeps){
            if (Collisions.willHitRightWall(bounds, sheep)){
                sheep.turnLeft();
                sheep.setDx(-sheep.getDx());
            } else if (Collisions.willHitLeftWall(bounds, sheep)) {
                sheep.turnRight();
                sheep.setDx(-sheep.getDx());
            }
        }
        if (Collisions.willHitLeftWall(bounds, llama) ||
                Collisions.willHitRightWall(bounds, llama)) {
            llama.setDx(0);
        }
    }

    /**
     * You know what this one does
     */
    private void draw(){
        DrawingHelper drawingHelper = new DrawingHelper(context, surfaceHolder);
        if (!drawingHelper.readyToDraw()) {
            return;
        }

        // just to clear canvas
        drawingHelper.fillBackground(DrawingHelper.WHITE);
        drawingHelper.fillBackground(DrawingHelper.SKY_BLUE);

        drawingHelper.draw(clouds);
        drawingHelper.drawRectangle(0, yFloor, bounds.x, bounds.y, DrawingHelper.DARK_GREEN); // the ground
        drawingHelper.draw(llama.getSheepPile());
        drawingHelper.draw(llama);
        drawingHelper.draw(spitballs, sheeps, comets);
        if (aimer != null) {
            drawingHelper.drawDashedLine(aimer, DrawingHelper.WHITE, bounds.x / 200);
        }
        if (ufo != null) {
            drawingHelper.draw(ufo.getSheep());
            Rect beamRect = ufo.getHitRect();
            drawingHelper.drawRectangle(beamRect.left, beamRect.top, beamRect.right, beamRect.bottom, Color.argb(100, 255, 255, 0));
            drawingHelper.draw(ufo);
        }

        hoorahManager.drawHoorahs(drawingHelper);

        int fontSize = bounds.x / 20;
        drawingHelper.drawRoundedRect(new Rect(bounds.x * 3 / 8, yFloor + 20, bounds.x - 20, bounds.y - 20),
                DrawingHelper.WHITE, 40);

        drawingHelper.draw(LLAMA_FACE, new Rect(0, bounds.y - 400, 400, bounds.y));


        drawingHelper.drawRegularText("Skip tutorial", fontSize,
                bounds.x - 400, bounds.y - 50, DrawingHelper.DARK_GREEN);
        if (instruction != null) {
            String[] lines = instruction.split("\n");
            for (int i = 0; i < lines.length; i++) {
                drawingHelper.drawCenterText(lines[i], fontSize, bounds.x * 2 / 3,
                        yFloor + fontSize * 2 + fontSize * i, DrawingHelper.BLUE);
            }
        }

        drawingHelper.finish();
    }

    private void controlFrameRate(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
        Log.d("GAME VIEW", "Leftover Millis: " + leftoverMillis);
        if (leftoverMillis < 5){
            leftoverMillis = 5;
        }
        try {
            Thread.sleep(leftoverMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        playing = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void resumePaused() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setFinishListener(ViewListener listener) {
        this.finishListener = listener;
    }

    public ViewListener getFinishListener() {
        if (finishListener == null) {
            throw new IllegalStateException("Listener is null!");
        } else {
            return finishListener;
        }
    }

    public Bundle getStateVars() {
        Bundle data = new Bundle();
        data.putInt("score", gc.getPoints());
        data.putInt("level", llama.getPileSize());

        return data;
    }
}
