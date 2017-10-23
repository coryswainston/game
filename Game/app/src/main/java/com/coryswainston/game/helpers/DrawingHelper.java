package com.coryswainston.game.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.objects.Sprite;

import java.util.Collection;
import java.util.List;

/**
 * @author Cory Swainston
 */

public class DrawingHelper {
    private final Typeface NORMAL_FONT;
    private final Typeface BOLD_FONT;
    private Paint paint = new Paint();
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    public DrawingHelper(Context context, SurfaceHolder surfaceHolder) {
        NORMAL_FONT = Typeface.createFromAsset(context.getAssets(), MainActivity.HANKEN_BOOK_FONT);
        BOLD_FONT = Typeface.create(NORMAL_FONT, Typeface.BOLD);
        paint.setTypeface(NORMAL_FONT);
        this.surfaceHolder = surfaceHolder;
        canvas = surfaceHolder.lockCanvas();
    }

    public void fillBackground(int color) {
        canvas.drawColor(color);
    }

    public void draw(Sprite sprite) {
        canvas.drawBitmap(sprite.getBitmap(), sprite.getX(), sprite.getY(), paint);
    }

    public void draw(Collection<? extends Sprite> sprites) {
        for (Sprite sprite : sprites) {
            draw(sprite);
        }
    }

    public void drawRectangle(int left, int top, int right, int bottom, int color) {
        paint.setColor(color);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void drawScore(int points, int fontSize, int x, int y) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTypeface(BOLD_FONT);
        canvas.drawText("SCORE: " + points, x, y, paint);
    }

    public void drawBoldText(String text, int fontSize, int x, int y, int color) {
        paint.setTypeface(BOLD_FONT);
        paint.setColor(color);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(true);
        canvas.drawText(text, x, y, paint);
    }

    public void throwShade() {
        canvas.drawColor(Color.argb(150, 255, 255, 255));
    }

    public void finish() {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }
}
