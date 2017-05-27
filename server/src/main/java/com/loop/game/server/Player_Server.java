package com.loop.game.server;

/**
 * Created by Kamil on 21-05-17
 */

public class Player_Server {
    private String nick;
    private String pass;
    private int points;
    Player_Server(String name, String pass){
        this.nick = name;
        this.pass = pass;
        points = 0;
    }
    public boolean check(String name, String pass){
        if(this.nick.equals(name) && this.pass.equals(pass)) return true;
        return false;
    }
}
