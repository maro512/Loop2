package com.loop.game.Net;

/**
 * Created by Piotr on 2017-05-30.
 */

public class RatingEntry
{
    private String name;
    private int gamesPlayed;
    private int points;
    private int position;

    public int getPosition()
    {
        return position;
    }

    public String getName()
    {
        return name;
    }

    public int getGamesPlayed()
    {
        return gamesPlayed;
    }

    public int getPoints()
    {
        return points;
    }

    public RatingEntry(int lp, String name, int games, int points)
    {
        this.position=lp;
        this.name=name;
        this.points=points;
        this.gamesPlayed=games;
    }
}
