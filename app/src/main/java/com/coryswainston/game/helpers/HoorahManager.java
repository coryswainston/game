package com.coryswainston.game.helpers;

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
                return bounds.y / 8;
            case MEDIUM:
                return bounds.y / 12;
            case SMALL: default:
                return bounds.y / 16;
        }
    }

    public void makeHoorah(Point pos, FontSize size, int duration, String text) {
        Hoorah hoorah = new Hoorah();
        hoorah.setPosition(pos);
        hoorah.setDuration(duration);
        hoorah.setSize(getIntFromFontSize(size));
        hoorah.setText(text);

        hoorahs.add(hoorah);
    }

    public void drawHoorahs(DrawingHelper drawingHelper) {
        List<Hoorah> toRemove = new ArrayList<>();

        for (Hoorah hoorah : hoorahs) {
            drawingHelper.drawBoldText(hoorah.getText(), hoorah.getSize(), hoorah.getPosition().x,
                    hoorah.getPosition().y, DrawingHelper.DARK_RED);

            hoorah.getPosition().offset(0, -bounds.y / 500);
            boolean keepDrawing = hoorah.countdown();
            if (!keepDrawing) {
                toRemove.add(hoorah);
            }
        }

        hoorahs.removeAll(toRemove);
    }
}
