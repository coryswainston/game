package com.coryswainston.game.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.SoundPool;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
    private ViewListener viewListener;
    private SoundPool soundPool;
    private SharedPreferences sharedPreferences;

    private Llama llama;
    private List<Comet> comets = new ArrayList<>();
    private List<Sheep> sheeps = new ArrayList<>();
    private final List<Cloud> clouds = new ArrayList<>(3);
    private int cometsPerMinute;
    private int framesSinceLastComet;
    private int sheepPerMinute;
    private int numberOfSheep;

    int points;
    int level;
    int highScore;
    boolean newHigh;
    boolean gameWon;

    /**
     * Standard View constructor.
     *
     * @param context of the activity.
     */
    public GameView(Context context) {
        this(context, 0, 1);
    }

    /**
     * Constructor, initializes everything for the game.
     *
     * @param context the activity we're in.
     * @param points carried over from previous level.
     * @param level the user is currently in.
     */
    public GameView(Context context, int points, int level) {
        super(context);
        this.context = context;
        this.points = points;
        this.level = level;

        setUpBoundaries();
        createLlama();
        setUpClouds();
        loadHighScore();
        initializeGameConditions();

        hoorahManager.makeHoorah(hoorahManager.getCenter(), HoorahManager.FontSize.MEDIUM,
                Hoorah.TIME_LONG, "LEVEL " + level);
    }

    private void initializeGameConditions() {
        cometsPerMinute = BASE_PER_MINUTE + level * 2;
        framesSinceLastComet = 0;
        sheepPerMinute = BASE_PER_MINUTE * 2;
        numberOfSheep = level;
    }

    private void createLlama() {
        int width = bounds.y / 5;
        llama = new Llama(context, width);
        llama.setX(bounds.x / 2);
        llama.setY(yFloor - llama.getHeight());
        llama.setFloor(llama.getY());
    }

    private void setUpBoundaries() {
        bounds = new Point();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getSize(bounds);
        yFloor = bounds.y - bounds.y / 7;
        surfaceHolder.setFixedSize(bounds.x, bounds.y);
        hoorahManager = new HoorahManager(bounds);
    }

    private void setUpClouds() {
        for(int i = 0; i < 3; i++){
            Cloud cloud = new Cloud(context);
            cloud.setSize(bounds.y / 4, bounds.y / 6);
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
        while (!gameWon && llama.isAlive()){
            long startMillis = System.currentTimeMillis();
            if (playing) {
                update();
            }
            draw();

            long frameTime = System.currentTimeMillis() - startMillis;
            controlFrameRate(frameTime);
        }
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

        switch (e.getActionMasked()){
            case MotionEvent.ACTION_DOWN: // when finger hits the screen
                if (gestureHelper.isFirstTouch()) {
                    gestureHelper.down(e);
                    return true;
                }
                return true;
            case MotionEvent.ACTION_UP: // when finger releases
                if(!playing) {
                    if (llama.isAlive() && !gameWon) {
                        playing = true;
                    } else {
                        Log.d("GameView", "Bout to return with score " + points);
                        Intent intent = new Intent();
                        intent.putExtra("score", gameWon ? points : 0);
                        intent.putExtra("continue", gameWon);
                        getListener().onAction(intent);
                    }
                }

                gestureHelper.up(e);

                if (gestureHelper.noSwipe()) {
                    if (e.getX() > bounds.x - 90 && e.getY() < 90) {
                        playing = false;
                    }
                    llama.setDx(0);
                } else if (gestureHelper.isRightSwipe()){
                    llama.turnRight();
                    llama.setDx(30);
                } else if (gestureHelper.isLeftSwipe()) {
                    llama.turnLeft();
                    llama.setDx(-30);
                } else if (gestureHelper.isSwipeUp()){
                    llama.jump();
                } else {
                    llama.duck();
                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * Move everything along
     */
    private void update(){
        if (llama.getPileSize() == numberOfSheep) {
            playing = false;
            gameWon = true;
        }
        llama.update();
        updateComets();
        detectCollisions();
        updateClouds();
        updateSheep();
        checkBounds();
        updateHighScore();
    }

    private void updateHighScore() {
        if (!playing && points > highScore){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, points);
            editor.apply();
            newHigh = true;
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
        Sheep newSheep = null;
        Random random = new Random();
        if (random.nextInt(sheepPerMinute) == 1 && sheeps.size() + llama.getSheepPile().size() < numberOfSheep) {
            newSheep = new Sheep(getContext());
        }
        if (newSheep != null) {
            newSheep.setSize(bounds.y / 6, bounds.y / 9);
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
        if ((random.nextInt(odds) == 1 || framesSinceLastComet == odds * 2) && comets.size() < Math.max(level, 2)) {
            framesSinceLastComet = 0;
            comets.add(getNewComet());
        } else {
            framesSinceLastComet++;
        }
        for (Iterator<Comet> it = comets.iterator(); it.hasNext();){
            Comet comet = it.next();
            if ((comet.getY() >= yFloor - comet.getHeight()) && !comet.isExploded()){
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
        int height = (int) (bounds.y / 5.5);
        int width = (int) (height * 0.6);
        newComet.setSize(width, height);
        int speed = bounds.y / 75;
        int degrees = random.nextInt(30) - 15;
        int angle = 90 - degrees;
        float dx = speed * -(float)(Math.cos((float)Math.PI / 180.0f * angle));
        float dy = speed * (float)(Math.sin((float)Math.PI / 180.0f * angle));
        newComet.setDy(dy);
        newComet.setDx(dx);
        newComet.rotate(degrees / 3);
        newComet.setY(-newComet.getHeight());

        return newComet;
    }

    private void detectCollisions() {
        for(Comet comet : comets){
            if (comet.getHitRect().intersect(llama.getHitRect())) {
                comet.explode();
                playing = false;
                llama.kill();
            }
            List<Sheep> allSheeps = new ArrayList<>();
            allSheeps.addAll(sheeps);
            allSheeps.addAll(llama.getSheepPile());
            for (Sheep sheep : allSheeps) {
                if (sheep.getHitRect().intersect(comet.getHitRect())) {
                    comet.explode();
                    if (!sheep.isBurnt()) {
                        points -= 50;
                        hoorahManager.makeHoorah(new Point(comet.getX(), comet.getY()),
                                HoorahManager.FontSize.SMALL,
                                Hoorah.TIME_MED,
                                "-50");
                        sheep.burn();
                    }
                }
            }
        }
        for(Iterator<Sheep> it = sheeps.iterator(); it.hasNext();) {
            Sheep sheep = it.next();
            Point sheepCenter = new Point(sheep.getX() + sheep.getWidth() / 2, sheep.getY());
            if (llama.getHitRect().intersect(sheep.getHitRect()) && llama.isDucking()) {
                it.remove();
                llama.addToPile(sheep);
                points += 100;
                hoorahManager.makeHoorah(sheepCenter, HoorahManager.FontSize.SMALL, Hoorah.TIME_MED,
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
        if (llama.getX() > bounds.x - llama.getWidth() || llama.getX() < 0){
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
        drawingHelper.drawRectangle(bounds.x - 70, 20, bounds.x - 55, 70, DrawingHelper.BLACK);
        drawingHelper.drawRectangle(bounds.x - 40, 20, bounds.x - 25, 70, DrawingHelper.BLACK);
        drawingHelper.draw(llama.getSheepPile());
        drawingHelper.draw(llama);
        drawingHelper.draw(sheeps, comets);

        hoorahManager.drawHoorahs(drawingHelper);

        int fontSize = bounds.y / 20;
        drawingHelper.drawScoreAndLevel(points, level, fontSize, 40, 70);

        if (!playing){
            drawingHelper.throwShade();
            int xTextPosition = bounds.x / 2;
            int yTextPosition = bounds.y / 3;
            fontSize = bounds.y / 4;
            String messageText = "GAME OVER";
            if (llama.isAlive()) {
                messageText = gameWon ? "YOU WIN!" : "PAUSED";
            }
            drawingHelper.drawBoldText(messageText, fontSize, xTextPosition, yTextPosition,
                    DrawingHelper.BLUE);
            drawingHelper.drawBoldText((newHigh ? "NEW HIGH SCORE: " : "SCORE: ") + points,
                    newHigh ? fontSize / 2 : fontSize, xTextPosition, yTextPosition + fontSize + 50,
                    DrawingHelper.DARK_GREEN);
            drawingHelper.drawBoldText("Tap to continue", bounds.y / 12, xTextPosition,
                    bounds.y * 3/4, DrawingHelper.DARK_RED);
        }

        drawingHelper.finish();
    }

    private void controlFrameRate(long frameTime){
        long leftoverMillis = TARGET_MILLIS - frameTime;
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
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setListener(ViewListener listener) {
        this.viewListener = listener;
    }

    public ViewListener getListener() {
        if (viewListener == null) {
            throw new IllegalStateException("Listener is null!");
        } else {
            return viewListener;
        }
    }
}
