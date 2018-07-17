package com.coryswainston.game;

/**
 * To encapsulate the conditions of the game
 */
public class GameConditions {

    private static final int BASE_PER_MINUTE = 20;

    private int points;
    private int highScore;
    private int lives;
    private int cometsPerMinute;
    private int framesSinceLastComet;
    private int sheepPerMinute;
    private int framesSinceLastSheep;
    private boolean newHigh;
    private boolean gameWon;
    private boolean gameLost;


    // Initializes to starting defaults
    public GameConditions() {
        points = 0;
        highScore = 0;
        lives = 3;
        cometsPerMinute = BASE_PER_MINUTE;
        framesSinceLastComet = 0;
        sheepPerMinute = BASE_PER_MINUTE * 2;
        framesSinceLastSheep = 0;
        newHigh = false;
        gameWon = false;
        gameLost = false;
    }

    public boolean isGameLost() {
        return gameLost;
    }

    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }

    public int getCometsPerMinute() {
        return cometsPerMinute;
    }

    public void setCometsPerMinute(int cometsPerMinute) {
        this.cometsPerMinute = cometsPerMinute;
    }

    public int getFramesSinceLastComet() {
        return framesSinceLastComet;
    }

    public void resetFramesSinceLastComet() {
        framesSinceLastComet = 0;
    }

    public int getSheepPerMinute() {
        return sheepPerMinute;
    }

    public void setSheepPerMinute(int sheepPerMinute) {
        this.sheepPerMinute = sheepPerMinute;
    }

    public int getFramesSinceLastSheep() {
        return framesSinceLastSheep;
    }

    public void resetFramesSinceLastSheep() {
        framesSinceLastSheep = 0;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void subtractPoints(int points) {
        addPoints(-points);
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public boolean isNewHigh() {
        return newHigh;
    }

    public void setNewHigh(boolean newHigh) {
        this.newHigh = newHigh;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public void passFrame() {
        framesSinceLastComet++;
        framesSinceLastSheep++;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
    }
}
