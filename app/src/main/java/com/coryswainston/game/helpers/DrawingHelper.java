package com.coryswainston.game.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.objects.Llama;
import com.coryswainston.game.objects.Sprite;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author Cory Swainston
 */

public class DrawingHelper {
    /**
     * Colors for convenience
     */
    public static final int DARK_GREEN = Color.rgb(0, 100, 0);
    public static final int SKY_BLUE = Color.rgb(100, 215, 255);
    public static final int DARK_RED = Color.rgb(180, 0, 0);
    public static final int WHITE = Color.WHITE;
    public static final int BLUE = Color.BLUE;
    public static final int YELLOW = Color.YELLOW;
    public static final int BLACK = Color.BLACK;

    /**
     * Alignments
     */
    public static final Paint.Align LEFT_ALIGN = Paint.Align.LEFT;
    public static final Paint.Align CENTER_ALIGN = Paint.Align.CENTER;

    private final Typeface VALERA;
    private final Typeface JUA;

    private Paint paint = new Paint();
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    /**
     * Constructor.
     *
     * @param context of the activity.
     * @param surfaceHolder for view on which to draw.
     */
    public DrawingHelper(Context context, SurfaceHolder surfaceHolder) {
        VALERA = Typeface.createFromAsset(context.getAssets(), "VarelaRound-Regular.ttf");
        JUA = Typeface.createFromAsset(context.getAssets(), "Jua-Regular.ttf");
        paint.setTypeface(JUA);
        this.surfaceHolder = surfaceHolder;
        if (readyToDraw()) {
            canvas = surfaceHolder.lockCanvas();
        }
    }

    /**
     * Is the surface ready to draw? Cannot draw anything if this is false.
     *
     * @return true if the surface is ready to be drawn on.
     */
    public boolean readyToDraw() {
        return surfaceHolder.getSurface().isValid();
    }

    /**
     * Fill the background with a certain color.
     *
     * @param color of the background.
     */
    public void fillBackground(int color) {
        canvas.drawColor(color);
    }

    /**
     * Draws a given Sprite.
     *
     * @param sprite to draw.
     */
    public void draw(Sprite sprite) {
        canvas.drawBitmap(sprite.getBitmap(), sprite.getX(), sprite.getY(), paint);
    }

    /**
     * Draws a collection o Sprites.
     *
     * @param sprites to draw.
     */
    public void draw(Collection<? extends Sprite> sprites) {
        for (Sprite sprite : sprites) {
            draw(sprite);
        }
    }

    /**
     * Draw a series of Sprites. This method allows for passing several collections.
     *
     * @param collections of Sprites to draw.
     */
    @SafeVarargs
    public final void draw(Collection<? extends Sprite> ... collections) {
        for (Collection<? extends Sprite> collection : collections) {
            draw(collection);
        }
    }

    /**
     * Draw a series of Sprites. This allows for the passing of several individual Sprites
     * not in a collection.
     *
     * @param sprites to draw.
     */
    public void draw(Sprite ... sprites) {
        for (Sprite sprite : sprites) {
            draw(sprite);
        }
    }

    /**
     * Draws a rectangle.
     *
     * @param left boundary.
     * @param top "
     * @param right "
     * @param bottom "
     * @param color of the rectangle.
     */
    public void drawRectangle(int left, int top, int right, int bottom, int color) {
        paint.setColor(color);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    /**
     * Draws the score and level.
     *
     * @param points to be drawn.
     * @param level to be drawn.
     * @param fontSize of the score.
     * @param x coordinate.
     * @param y coordinate.
     */
    public void drawScoreAndLevel(int points, int level, int fontSize, int x, int y) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(VALERA);
        canvas.drawText("SCORE: " + points + "  LEVEL: " + level, x, y, paint);
    }

    /**
     * Draws centered text.
     *
     * @param text to draw.
     * @param fontSize of the text.
     * @param x coordinate.
     * @param y coordinate.
     * @param color of the text.
     */
    public void drawCenterText(String text, int fontSize, int x, int y, int color) {
        paint.setColor(color);
        paint.setTextSize(fontSize);
        paint.setTextAlign(CENTER_ALIGN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(JUA);
        canvas.drawText(text, x, y, paint);    }

    /**
     * Draws text in a regular typeface. (Left aligned)
     *
     * @param text to draw.
     * @param fontSize of the text.
     * @param x coordinate.
     * @param y coordinate.
     * @param color of the text.
     */
    public void drawRegularText(String text, int fontSize, int x, int y, int color) {
        paint.setColor(color);
        paint.setTextSize(fontSize);
        paint.setTextAlign(LEFT_ALIGN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(VALERA);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * Casts a white shadow over the scene.
     */
    public void throwShade() {
        canvas.drawColor(Color.argb(150, 255, 255, 255));
    }

    /**
     * To be called when drawing instructions are given. This method actually performs the
     * drawing.
     */
    public void finish() {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}
