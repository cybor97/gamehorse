package com.cybor.gamehorse.core;

public class Horse
{
    public static final int PLAYING = 0, WIN = 1, LOOSE = 2;
    private int x, y, state = PLAYING;
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
        if (onPositionChangeListener != null)
            onPositionChangeListener.onPositionChange(this);
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
        if (onPositionChangeListener != null)
            onPositionChangeListener.onPositionChange(this);
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
        if (onPositionChangeListener != null)
            onPositionChangeListener.onPositionChange(this);
    }

    public Horse copy()
    {
        Horse horse = new Horse();
        horse.setX(x);
        horse.setY(y);
        horse.setState(state);

        //Можно через Cloneable, но не нужно, т.к. нужна ссылка на коллбэк
        //Да и старую надо снести, чтобы не засорять стек
        //...сказал он и осознал всю глупость данного утверждения
        horse.setOnPositionChangeListener(onPositionChangeListener);
        setOnPositionChangeListener(onPositionChangeListener);

        return horse;
    }

    interface OnPositionChangeListener
    {
        void onPositionChange(Horse sender);
    }

}
