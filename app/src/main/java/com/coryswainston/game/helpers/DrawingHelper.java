package com.coryswainston.game.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.coryswainston.game.R;
import com.coryswainston.game.sprites.Sprite;

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
    public static final int GREY = Color.argb(100, 60, 60, 60);

    /**
     * Alignments
     */
    public static final Paint.Align LEFT_ALIGN = Paint.Align.LEFT;
    public static final Paint.Align CENTER_ALIGN = Paint.Align.CENTER;

    private final Typeface VALERA;
    private final Typeface JUA;
    private final Typeface PRESS_START_2P;

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
        AssetManager assets = context.getAssets();
        VALERA = Typeface.createFromAsset(assets, "VarelaRound-Regular.ttf");
        JUA = Typeface.createFromAsset(assets, "Jua-Regular.ttf");
        PRESS_START_2P = Typeface.createFromAsset(assets, "PressStart2P-Regular.ttf");
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

    public void drawWatermark(Bitmap b) {
        paint.setAlpha(80);
        canvas.drawBitmap(b, 0, 0, paint);
    }

    /**
     * Draws a given Sprite.
     *
     * @param sprite to draw.
     */
    public void draw(Sprite sprite) {
        sprite.getDrawable().setBounds(sprite.getX(), sprite.getY(),
                sprite.getX() + sprite.getWidth(),
                sprite.getY() + sprite.getHeight());
        if (sprite.getRotation() != 0) {
            canvas.save();
            canvas.rotate(sprite.getRotation(), sprite.getX(), sprite.getY());
        }
        sprite.getDrawable().draw(canvas);
        if (sprite.getRotation() != 0) {
            canvas.restore();
        }
    }

    public void draw(Drawable d, Rect bounds) {
        d.setBounds(bounds);
        d.draw(canvas);
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

    public void drawRoundedRect(Rect r, int color, int radius) {
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(r), radius, radius, paint);
    }

    /**
     * Draws an oval.
     *
     * @param left boundary.
     * @param top "
     * @param right "
     * @param bottom "
     * @param color of the oval.
     */
    public void drawOval(int left, int top, int right, int bottom, int color) {
        paint.setColor(color);
        RectF r = new RectF();
        r.left = left;
        r.top = top;
        r.right = right;
        r.bottom = bottom;
        canvas.drawOval(r, paint);
    }

    public void drawDashedLine(Rect coords, int color, float width) {
        paint.setColor(color);
        paint.setPathEffect(new DashPathEffect(new float[] {width * 2, width * 4}, 0));
        paint.setStrokeWidth(width);
        canvas.drawLine(coords.left, coords.bottom, coords.right, coords.top, paint);
        paint.reset();
    }

    /**
     * Draws the score and number of lives
     *
     * @param points to be drawn.
     * @param high score to be drawn.
     * @param lives the number of sheep to be drawn.
     * @param fontSize of text.
     * @param x coordinate.
     * @param y coordinate.
     */
    public void drawScore(int points, int high, int lives, Drawable icon, int fontSize, int x, int y) {
        paint.setColor(BLACK);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(VALERA);
        canvas.drawText("SCORE: " + points, x, y, paint);
        paint.setColor(DARK_GREEN);
        canvas.drawText("HIGH: " + high, x, y + fontSize * 1.2f, paint);

        int sheepWidth = fontSize * 10 / 7;
        int sheepY = y + fontSize * 2;
        for (int i = 0; i < lives; i++) {
            Rect bounds = new Rect(x, sheepY, x + sheepWidth, sheepY + fontSize);
            bounds.left += sheepWidth * i;
            bounds.right += sheepWidth * i;
            icon.setBounds(bounds);
            icon.draw(canvas);
        }
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
        canvas.drawText(text, x, y, paint);
    }

    /**
     * Draws centered text for the menu.
     *
     * @param text to draw.
     * @param fontSize of the text.
     * @param x coordinate.
     * @param y coordinate.
     * @param color of the text.
     */
    public void drawMenuText(String text, int fontSize, int x, int y, int color) {
        paint.setColor(color);
        paint.setTextSize(fontSize);
        paint.setTextAlign(CENTER_ALIGN);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(PRESS_START_2P);
        canvas.drawText(text, x, y, paint);
    }

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
        canvas.drawColor(Color.argb(170, 255, 255, 255));
    }

    /**
     * To be called when drawing instructions are given. This method actually performs the
     * drawing.
     */
    public void finish() {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}
