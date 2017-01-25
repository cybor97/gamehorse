package com.cybor.gamehorse.core;

import java.util.ArrayList;
import java.util.List;

import static com.cybor.gamehorse.core.Horse.LOOSE;
import static com.cybor.gamehorse.core.Horse.PLAYING;
import static java.lang.Math.abs;

public class HorseGame implements Horse.OnPositionChangeListener
{
    private static HorseGame instance;
    private GameMap map;
    private List<List<Horse>> history;
    private boolean multiplayer;
    private OnStateChangeListener onStateChangeListener;

    private HorseGame(boolean multiplayer)
    {
        map = new GameMap(15, 15);
        history = new ArrayList<>();
        this.multiplayer = multiplayer;

        for (int i = 0; i < (multiplayer ? 2 : 1); i++)
        {
            List<Horse> playerHistory = new ArrayList<>();
            playerHistory.add(new Horse());
            history.add(playerHistory);
        }

    }

    public static HorseGame getInstance(boolean multiplayer)
    {
        if (instance == null)
            instance = new HorseGame(multiplayer);
        return instance;
    }

    public void setOnStateChange(OnStateChangeListener onStateChange)
    {
        this.onStateChangeListener = onStateChange;
    }

    public List<Horse> getPlayerHistory(int playerId)
    {
        return history.get(playerId);
    }

    public GameMap getMap()
    {
        return map;
    }

    public boolean tryStep(int player, int x, int y)
    {
        Horse horse = getHorse(player);
        if (horse.getState() == PLAYING && stepAvailable(horse, x, y))
        {
            horse.setX(x);
            horse.setY(y);
            return true;
        } else
        {
            if (multiplayer)
            {

            } else horse.setState(LOOSE);
            return false;
        }
    }

    public boolean stepsAvailable(Horse horse)
    {
        boolean available = false;
        for (int x = 0; x < map.getWidth(); x++)
        {
            for (int y = 0; y < map.getHeight(); y++)
                if (stepAvailable(horse, x, y))
                {
                    available = true;
                    break;
                }
            if (available) break;
        }
        return available;
    }

    public boolean stepAvailable(Horse horse, int x, int y)
    {
        GameMap map = getMap();
        return (abs(horse.getX() - x) == 2 && abs(horse.getY() - y) == 1 ||
                abs(horse.getX() - x) == 1 && abs(horse.getY() - y) == 2) &&
                x < map.getWidth() && y < map.getHeight() &&
                map.getCell(x, y) == GameMap.EMPTY;
    }

    @Override
    public void onPositionChange(Horse horse)
    {
        if (onStateChangeListener != null)
            onStateChangeListener.onStateChange(horse);
    }

    public Horse getHorse(int playerId)
    {
        List<Horse> playerHistory = history.get(playerId);
        return playerHistory.get(playerHistory.size());
    }

    interface OnStateChangeListener
    {
        void onStateChange(Horse horse);
    }
}
