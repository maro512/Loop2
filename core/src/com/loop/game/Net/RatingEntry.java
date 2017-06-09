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

    public RatingEntry(int lp, String name, int games, int points)
    {
        this.position=lp;
        this.name=name;
        this.points=points;
        this.gamesPlayed=games;
    }

    public static RatingEntry decode(String[] tokens)
    {
        return new RatingEntry(Integer.parseInt(tokens[1]), tokens[2],
                               Integer.parseInt(tokens[4]),Integer.parseInt(tokens[3]));
    }

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

    public String toString() // (miejsce: "nick" wygrane/rozegrane) 13: "gracz" 7/10
    {
        return String.format("#%3d: \"%s\"\t %d/%d", position, name, points, gamesPlayed);
    }

    public String encode()
    {
        return String.format(" %d %s %d %d", position, name, points, gamesPlayed);
    }
}
