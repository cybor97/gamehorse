package com.cybor.gamehorse.core;

import java.util.ArrayList;
import java.util.List;

public class GameMap
{
    public static final int INVALID = -1, EMPTY = 0, HORSE = 1, STEP = 2;
    private List<List<Integer>> map;

    public GameMap(int width, int height)
    {
        map = new ArrayList<>();

        for (int x = 0; x < width; x++)
        {
            List<Integer> column = new ArrayList<>();
            for (int y = 0; y < height; y++)
                column.add(EMPTY);
            map.add(column);
        }
    }

    public int getCell(int x, int y)
    {
        return getWidth() > x && getHeight() > y ? map.get(x).get(y) : INVALID;
    }

    public void setCell(int x, int y, int value)
    {
        map.get(x).set(y, value);
    }

    public int getWidth()
    {
        return map.size();
    }

    public int getHeight()
    {
        return map.isEmpty() ? 0 : map.get(0).size();
    }
}
