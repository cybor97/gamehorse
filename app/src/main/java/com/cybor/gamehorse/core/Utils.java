package com.cybor.gamehorse.core;

import java.util.ArrayList;
import java.util.List;

public class Utils
{
    public static List<Integer> parseCoordinates(String value)
    {
        //Два любых числа, разделенных _. В шарпе - Regex.Match(...
        if (value.matches("[0-9].*_[0-9].*"))
        {
            List<Integer> result = new ArrayList<>();
            String[] blocks = value.split("_");
            result.add(Integer.parseInt(blocks[0]));
            result.add(Integer.parseInt(blocks[1]));
            return result;
        } else return null;
    }

    public static String packCoordinates(List<Integer> coords)
    {
        //Не баг. В Java значения идут по порядку.
        return String.format("%s_%s", coords.get(0), coords.get(1));
    }
}
