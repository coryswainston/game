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
import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.libraries.Collisions;
import com.coryswainston.game.helpers.SpriteFactory;
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
public class GameView extends SurfaceView implements Runnable {

    private GameConditions gc;

    volatile boolean playing = true;
    private Thread gameThread = null;
    private SurfaceHolder surfaceHolder = getHolder();
    GestureHelper gestureHelper = new GestureHelper();
    private Point bounds;
    private int yFloor;
    private HoorahManager hoorahManager;
    private final Drawable SHEEP_IMAGE;

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

    /**
     * Standard View constructor.
     *
     * @param context of the activity.
     */
    public GameView(Context context) {
        this(context, 0);
    }

    /**
     * Constructor, initializes everything for the game.
     *
     * @param context the activity we're in.
     * @param points carried over from previous level.
     */
    public GameView(Context context, int points) {
        super(context);
        this.context = context;

        setUpBoundaries();
        this.gc = new GameConditions();
        gc.setPoints(points);
        hoorahManager = new HoorahManager(bounds);

        spriteFactory = new SpriteFactory(bounds, yFloor, context);
        llama = spriteFactory.buildLlama();
        clouds = spriteFactory.buildClouds();
        SHEEP_IMAGE = context.getResources().getDrawable(R.drawable.sheep1);

        sharedPreferences = context.getSharedPreferences(LLAMA_PREFS, Context.MODE_PRIVATE);
        gc.setHighScore(sharedPreferences.getInt(HIGH_SCORE, 0));
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
                if(!playing) {
                    if (llama.isAlive() && !gc.isGameWon()) {
                        resume();
                        return super.onTouchEvent(e);
                    } else {
                        Log.d("GameView", "Bout to return with score " + gc.getPoints());
                        Intent intent = new Intent();
                        intent.putExtra("score", gc.isGameWon() ? gc.getPoints() : 0);
                        intent.putExtra("continue", gc.isGameWon());
                        getFinishListener().onAction(intent);
                    }
                }

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
                    if (e.getX() > bounds.x - 90 && e.getY() < 90) {
                        playing = false;
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
//        TODO Add a new way to win the game
        updateSpitballs();
        updateComets();
        detectCollisions();
        updateClouds();
        updateSheep();
        checkBounds();
        llama.update();
        updateUfo();
        updateHighScore();
        gc.passFrame();

        if (gc.getLives() == 0) {
            gc.setGameLost(true);
            playing = false;
        }
    }

    private void updateHighScore() {
        if (gc.getPoints() > gc.getHighScore() && !playing) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, gc.getPoints());
            editor.apply();

            gc.setNewHigh(true);
        }
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
        float sheepPerFrame = gc.getSheepPerMinute() / 60.0f / 30.0f;
        int odds = (int) (1 / sheepPerFrame);
        Random random = new Random();
        if (random.nextInt(odds) == 1 && gc.getFramesSinceLastSheep() > 30) {
            gc.resetFramesSinceLastSheep();
            sheeps.add(spriteFactory.buildSheep());
        }

        for (Iterator<Sheep> it = llama.getSheepPile().iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            if (!sheep.isSeated() && sheep.getDy() == 0 || !sheep.isAlive()) {
                sheeps.add(sheep);
                it.remove();
            }
        }

        for (Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            if (!sheep.isSeated()) {
                sheep.setDy(30);
                sheep.setDx(0);
            }
            sheep.update();
            if (sheep.getY() >= yFloor - sheep.getHeight()) {
                if (sheep.isAlive()) {
                    sheep.setY(yFloor - sheep.getHeight());
                    sheep.setSeated(true);
                    sheep.setDy(0);
                    sheep.setDx(sheep.facingRight() ? 5 : -5);
                }
            }
            if (!sheep.isAlive() && sheep.getY() > bounds.y) {
                it.remove();
            }
        }
    }

    private void updateComets() {
        float cometsPerFrame = gc.getCometsPerMinute() / 60.0f / 30.0f;
        int odds = (int) (1 / cometsPerFrame);
        Log.d("ODDS", "odds are " + odds);
        Random random = new Random();
        if ((random.nextInt(odds) == 1 || gc.getFramesSinceLastComet() == odds) && comets.size() < 5) {
            gc.resetFramesSinceLastComet();
            comets.add(spriteFactory.buildComet());
        }

        for (Iterator<Comet> it = comets.iterator(); it.hasNext();){
            Comet comet = it.next();
            if ((comet.getHitRect().top >= yFloor - comet.getHeight()) && !comet.isExploded()){
                comet.explode();
            }
            comet.update();
            if (!comet.isAlive()){
                it.remove();
            }
        }
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

        // pause button
        drawingHelper.drawRectangle(bounds.x - 70, 20, bounds.x - 55, 70, DrawingHelper.BLACK);
        drawingHelper.drawRectangle(bounds.x - 40, 20, bounds.x - 25, 70, DrawingHelper.BLACK);

        int fontSize = bounds.x / 20;
        drawingHelper.drawScore(gc.getPoints(),
                gc.getHighScore(),
                gc.getLives(),
                SHEEP_IMAGE,
                fontSize, 40, 70);

        if (!playing) {
            drawingHelper.throwShade();
            int xTextPosition = bounds.x / 2;
            int yTextPosition = (int) (bounds.y / 2.5);
            fontSize = bounds.x / 6;
            String messageText;
            if (gc.isGameLost()) {
                messageText = "GAME OVER";
            } else {
                messageText = gc.isGameWon() ? "YOU WIN!" : "PAUSED";
            }
            drawingHelper.drawCenterText(messageText, fontSize, xTextPosition, yTextPosition,
                    DrawingHelper.BLUE);
            drawingHelper.drawCenterText((gc.isNewHigh() ? "NEW HIGH SCORE: " : "SCORE: ") + gc.getPoints(),
                    gc.isNewHigh() ? fontSize / 2 : fontSize, xTextPosition, yTextPosition + fontSize,
                    DrawingHelper.DARK_GREEN);
            drawingHelper.drawCenterText("Tap to continue", bounds.x / 15, xTextPosition,
                    yTextPosition + fontSize * 3 / 2, DrawingHelper.DARK_RED);
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
