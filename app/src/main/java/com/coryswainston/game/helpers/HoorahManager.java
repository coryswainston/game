package com.coryswainston.game.helpers;

import android.graphics.Color;
import android.graphics.Point;

import com.coryswainston.game.objects.Hoorah;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for managing the drawing and timing of Hoorahs
 */
public class HoorahManager {

    private Point bounds;
    private List<Hoorah> hoorahs = new ArrayList<>();

    /**
     * For simply defining font size
     */
    public enum FontSize {
        SMALL, MEDIUM, LARGE
    }

    /**
     * Constructor
     *
     * @param bounds of the screen
     */
    public HoorahManager(Point bounds) {
        this.bounds = bounds;
    }

    private int getIntFromFontSize(FontSize fontSize) {
        switch (fontSize) {
            case LARGE:
                return bounds.y / 4;
            case MEDIUM:
                return bounds.y / 8;
            case SMALL: default:
                return bounds.y / 16;
        }
    }

    public Point getCenter() {
        return new Point(bounds.x / 2, bounds.y / 2);
    }

    public void makeHoorah(Point pos, FontSize size, int duration, String text) {
        Hoorah hoorah = new Hoorah();
        hoorah.setPosition(pos);
        hoorah.setDuration(duration);
        hoorah.setSize(getIntFromFontSize(size));
        hoorah.setText(text);
        hoorah.setAlpha(255);

        hoorahs.add(hoorah);
    }

    public void drawHoorahs(DrawingHelper drawingHelper) {
        List<Hoorah> toRemove = new ArrayList<>();

        for (Hoorah hoorah : hoorahs) {
            drawingHelper.drawBoldText(hoorah.getText(), hoorah.getSize(), hoorah.getPosition().x,
                    hoorah.getPosition().y, getColorWithFade(hoorah));

            hoorah.getPosition().offset(0, -bounds.y / 500);

            boolean keepDrawing = hoorah.countdown();
            if (!keepDrawing) {
                toRemove.add(hoorah);
            }
        }

        hoorahs.removeAll(toRemove);
    }

    private int getColorWithFade(Hoorah hoorah) {
        boolean threeQuarterMark = hoorah.getDuration() / 4 >= hoorah.getFramesLeft();
        if (threeQuarterMark) {
            int dAlpha = 255 / (hoorah.getDuration() / 4);
            hoorah.fade(dAlpha);
            if (hoorah.getAlpha() < 0) {
                hoorah.setAlpha(0);
            }
        }
        return Color.argb(hoorah.getAlpha(), 180, 0, 0);
    }
}
