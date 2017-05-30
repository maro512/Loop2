package com.loop.game.GameModel;

/**
 * Created by marek on 11.04.17.
 */

public class Player {
    private String name;
    private int number;

    public Player(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public int getNumber() { return number; }
}
