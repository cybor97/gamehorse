package com.cybor.gamehorse.core;

public class Horse
{
    public static final int PLAYING = 0, WIN = 1, LOOSE = 2;
    private int x, y, state = PLAYING;
    private int score;
    private OnPositionChangeListener onPositionChangeListener;

    public void setOnPositionChangeListener(OnPositionChangeListener onPositionChange)
    {
        this.onPositionChangeListener = onPositionChange;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
        invokeOnPositionChangeListener();
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
        invokeOnPositionChangeListener();
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
        invokeOnPositionChangeListener();
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
        invokeOnPositionChangeListener();
    }

    public Horse copy()
    {
        Horse horse = new Horse();
        horse.setX(x);
        horse.setY(y);
        horse.setState(state);
        horse.setScore(score);

        horse.setOnPositionChangeListener(onPositionChangeListener);
        setOnPositionChangeListener(onPositionChangeListener);

        return horse;
    }

    private void invokeOnPositionChangeListener()
    {
        if (onPositionChangeListener != null)
            onPositionChangeListener.onPositionChange(this);
    }

    interface OnPositionChangeListener
    {
        void onPositionChange(Horse sender);
    }

}
