package com.coryswainston.game.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

import com.coryswainston.game.activities.MainActivity;
import com.coryswainston.game.objects.Sprite;

/**
 * @author Cory Swainston
 */

public class DrawingHelper {
    private Typeface normal;
    private Typeface bold;
    private Paint paint = new Paint();
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    public DrawingHelper(Context context, SurfaceHolder surfaceHolder) {
        normal = Typeface.createFromAsset(context.getAssets(), MainActivity.HANKEN_BOOK_FONT);
        bold = Typeface.create(normal, Typeface.BOLD);
        paint.setTypeface(normal);
        this.surfaceHolder = surfaceHolder;
        canvas = surfaceHolder.lockCanvas();
    }

    public void fillBackground(int color) {
        canvas.drawColor(color);
    }

    public void draw(Sprite sprite) {
        canvas.drawBitmap(sprite.getBitmap(), sprite.getX(), sprite.getY(), paint);
    }

    public void drawRectangle(int left, int top, int right, int bottom, int color) {
        paint.setColor(color);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    public void drawScore(int points, int fontSize, int x, int y) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTypeface(bold);
        canvas.drawText("SCORE: " + points, x, y, paint);
    }

    public void drawBoldText(String text, int fontSize, int x, int y, int color) {
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
