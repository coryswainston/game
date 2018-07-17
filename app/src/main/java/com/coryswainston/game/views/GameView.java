package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coryswainston.game.helpers.DrawingHelper;
import com.coryswainston.game.helpers.GestureHelper;
import com.coryswainston.game.helpers.HoorahManager;
import com.coryswainston.game.helpers.ViewListener;
import com.coryswainston.game.objects.Cloud;
import com.coryswainston.game.objects.Comet;
import com.coryswainston.game.objects.Hoorah;
import com.coryswainston.game.objects.Llama;
import com.coryswainston.game.objects.Sheep;
import com.coryswainston.game.objects.Spitball;
import com.coryswainston.game.objects.Sprite;
import com.coryswainston.game.objects.Ufo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.coryswainston.game.objects.Hoorah.TIME_MED;

/**
 * View that handles the game
 *
 * @author Cory Swainston
 */
public class GameView extends SurfaceView implements Runnable {

    private final long TARGET_MILLIS = 33;
    private final int BASE_PER_MINUTE = 20;
    private final String HIGH_SCORE = "high_score";
    private final String LLAMA_PREFS = "llama_prefs";

    volatile boolean playing = true;
    private Thread gameThread = null;
    private SurfaceHolder surfaceHolder = getHolder();
    GestureHelper gestureHelper = new GestureHelper();
    private Point bounds;
    private int yFloor;
    private HoorahManager hoorahManager;

    private Context context;
    private ViewListener finishListener;
    private SoundPool soundPool;
    private SharedPreferences sharedPreferences;

    private Llama llama;
    private Rect aimer;
    private Ufo ufo;
    private List<Spitball> spitballs = new ArrayList<>();
    private List<Comet> comets = new ArrayList<>();
    private List<Sheep> sheeps = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>(3);
    private int cometsPerMinute;
    private int framesSinceLastComet;
    private int sheepPerMinute;
    private int framesSinceLastSheep;
    private int totalSheep;

    private List<String> instructions;
    private Iterator<String> instructionIt;
    private String instruction;

    int points;
    int highScore;
    boolean newHigh;
    boolean gameWon;

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
        this.points = points;
        totalSheep = 0;

        setUpBoundaries();
        createLlama();
        setUpClouds();
        loadHighScore();
        initializeGameConditions();

        instructions = new ArrayList<>();
        instructions.add("Swipe left or right to \nskate across the screen.");
        instructions.add("Swipe down to pick up \nsheep.");
        instructions.add("Swipe up to jump.");
        instructions.add("Swipe and hold in any \ndirection to spit.");
        instructionIt = instructions.iterator();
        instruction = instructionIt.next();
    }

    private void initializeGameConditions() {
        cometsPerMinute = BASE_PER_MINUTE;
        framesSinceLastComet = 0;
        sheepPerMinute = BASE_PER_MINUTE * 2;
        framesSinceLastSheep = 0;
    }

    private void createLlama() {
        int width = bounds.x / 6;
        llama = new Llama(context, width);
        llama.setX(bounds.x / 2);
        llama.setY(yFloor - llama.getHeight());
        llama.setFloor(llama.getY());
    }

    private void setUpBoundaries() {
        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);
        yFloor = bounds.y - bounds.y / 5;
        surfaceHolder.setFixedSize(bounds.x, bounds.y);
        hoorahManager = new HoorahManager(bounds);
    }

    private void setUpClouds() {
        for(int i = 0; i < 3; i++){
            Cloud cloud = new Cloud(context);
            cloud.setSize(bounds.x / 6, bounds.x / 8);
            cloud.setDx((float)(.5 - 0.1 * i));
            clouds.add(i, cloud);
        }
        clouds.get(0).setX(bounds.x / 2);
        clouds.get(1).setX(bounds.x * 7 / 8);
        clouds.get(2).setX(bounds.x / 6);
        clouds.get(0).setY(bounds.y / 2);
        clouds.get(1).setY(bounds.y / 4);
        clouds.get(2).setY(0);
    }

    private void loadHighScore() {
        sharedPreferences = context.getSharedPreferences(LLAMA_PREFS, Context.MODE_PRIVATE);
        highScore = sharedPreferences.getInt(HIGH_SCORE, 0);
        newHigh = false;
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
                if (instructionIt.hasNext()) {
                    instruction = instructionIt.next();
                } else {
                    instruction = null;
                }

                if(!playing) {
                    if (llama.isAlive() && !gameWon) {
                        resume();
                        return super.onTouchEvent(e);
                    } else {
                        Log.d("GameView", "Bout to return with score " + points);
                        Intent intent = new Intent();
                        intent.putExtra("score", gameWon ? points : 0);
                        intent.putExtra("continue", gameWon);
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
//        if (llama.getPileSize() == numberOfSheep) {
//            playing = false;
//            gameWon = true;
//        }
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
    }

    private void updateHighScore() {
        if (points > highScore && !playing) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, points);
            editor.apply();

            newHigh = true;
        }
    }

    private void updateUfo() {
        if (llama.getPileSize() >= 1 && ufo == null) {
            Log.d("GameView", "Creating UFO");
            ufo = new Ufo(context);

            ufo.setSize(bounds.x / 4, bounds.x / 8);
            ufo.setX(new Random().nextInt(bounds.x - ufo.getWidth()));
            ufo.setY(-ufo.getHeight());
            ufo.setDy(25);
            ufo.setDx(0);
            ufo.setFloor(yFloor);
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
        float sheepPerFrame = sheepPerMinute / 60.0f / 30.0f;
        int odds = (int) (1 / sheepPerFrame);
        Sheep newSheep = null;
        Random random = new Random();
        if (random.nextInt(odds) == 1 && framesSinceLastSheep > 30) {
            newSheep = new Sheep(getContext());
            framesSinceLastSheep = 0;
        } else {
            framesSinceLastSheep++;
        }
        if (newSheep != null) {
            newSheep.setSize(bounds.x / 7, bounds.x / 10);
            newSheep.setX(random.nextInt(2) == 1 ? bounds.x : -newSheep.getWidth());
            newSheep.setY(yFloor - newSheep.getHeight());
            newSheep.setDx(newSheep.getX() > 0 ? -5 : 5);
            if (newSheep.getDx() < 0) {
                newSheep.turnLeft();
            }
            sheeps.add(newSheep);
        }
        for (Sheep sheep : sheeps) {
            sheep.update();
        }
    }

    private void updateComets() {
        float cometsPerFrame = cometsPerMinute / 60.0f / 30.0f;
        int odds = (int) (1 / cometsPerFrame);
        Random random = new Random();
        if ((random.nextInt(odds) == 1 || framesSinceLastComet == odds * 2) && comets.size() < Math.max(llama.getPileSize(), 2)) {
            framesSinceLastComet = 0;
            comets.add(getNewComet());
        } else {
            framesSinceLastComet++;
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

    private Comet getNewComet() {
        Random random = new Random();
        Comet newComet = new Comet(getContext(), random.nextInt(bounds.x));
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

    private void detectCollisions() {
        List<Sheep> allSheeps = new ArrayList<>();
        allSheeps.addAll(sheeps);
        allSheeps.addAll(llama.getSheepPile());
        for (Sheep sheep : allSheeps) {
            for (Comet comet : comets) {
                if (sheep.getHitRect().intersect(comet.getHitRect())) {
                    comet.explode();
                    if (!sheep.isBurnt()) {
                        points -= 50;
                        hoorahManager.makeHoorah(new Point(comet.getX(), comet.getY()),
                                HoorahManager.FontSize.SMALL,
                                TIME_MED,
                                "-50");
                        sheep.burn();
                    } else {
                        sheep.kill();
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
            if (comet.getHitRect().intersect(llama.getHitRect())) {
                comet.explode();
                playing = false;
                llama.kill();
            }

            for (Spitball spitball : spitballs) {
                if (spitball.getHitRect().intersect(comet.getHitRect())) {
                    comet.explode();
                    spitball.kill();
                    hoorahManager.makeHoorah(new Point(comet.getX(), comet.getY()),
                            HoorahManager.FontSize.SMALL,
                            TIME_MED,
                            "+25");
                    points += 25;
                }
            }
        }
        for(Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            Point sheepCenter = new Point(sheep.getX() + sheep.getWidth() / 2, sheep.getY());
            if (llama.getHitRect().intersect(sheep.getHitRect()) && llama.isDucking()) {
                it.remove();
                llama.addToPile(sheep);
                totalSheep += 1;
                points += 100;
                hoorahManager.makeHoorah(sheepCenter, HoorahManager.FontSize.SMALL, TIME_MED,
                        "+100");
            }
        }
    }

    private void checkBounds(){
        for (Sheep sheep : sheeps){
            if (sheep.getX() > bounds.x - sheep.getWidth() && sheep.getDx() > 0){
                sheep.turnLeft();
                sheep.setDx(-sheep.getDx());
            } else if (sheep.getX() < 0 && sheep.getDx() < 0){
                sheep.turnRight();
                sheep.setDx(-sheep.getDx());
            }
        }
        if (llama.getX() > bounds.x - llama.getWidth() && llama.getDx() > 0 ||
                llama.getX() < 0 && llama.getDx() < 0) {
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
        drawingHelper.drawScoreAndLevel(points, highScore, totalSheep, fontSize, 40, 70);

        if (instruction != null) {
            String[] lines = instruction.split("\n");
            for (int i = 0; i < lines.length; i++) {
                drawingHelper.drawCenterText(lines[i], fontSize, bounds.x / 2,
                        yFloor + fontSize * 2 + fontSize * i, DrawingHelper.WHITE);
            }
        }

        if (!playing){
            drawingHelper.throwShade();
            int xTextPosition = bounds.x / 2;
            int yTextPosition = (int) (bounds.y / 2.5);
            fontSize = bounds.x / 6;
            String messageText = "GAME OVER";
            if (llama.isAlive()) {
                messageText = gameWon ? "YOU WIN!" : "PAUSED";
            }
            drawingHelper.drawCenterText(messageText, fontSize, xTextPosition, yTextPosition,
                    DrawingHelper.BLUE);
            drawingHelper.drawCenterText((newHigh ? "NEW HIGH SCORE: " : "SCORE: ") + points,
                    newHigh ? fontSize / 2 : fontSize, xTextPosition, yTextPosition + fontSize,
                    DrawingHelper.DARK_GREEN);
            drawingHelper.drawCenterText("Tap to continue", bounds.x / 15, xTextPosition,
                    yTextPosition + fontSize * 3 / 2, DrawingHelper.DARK_RED);
        }

        drawingHelper.finish();
    }

    private void controlFrameRate(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
//        Log.d("GAME VIEW", "Leftover Millis: " + leftoverMillis);
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
        data.putInt("score", points);
        data.putInt("level", llama.getPileSize());

        return data;
    }
}
