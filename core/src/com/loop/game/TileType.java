package com.loop.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marek on 21.04.17.
 */

public class TileType {
    private Map<Integer, Texture> tiles;
    public static TileType instance;

    public static void load(){
        instance = new TileType();
    }
    private TileType() {
        tiles = new HashMap<Integer, Texture>();
        tiles.put(10, new Texture("tile_10.png"));
        tiles.put(5, new Texture("tile_5.png"));
        tiles.put(9, new Texture("tile_9.png"));
        tiles.put(3, new Texture("tile_3.png"));
        tiles.put(6, new Texture("tile_6.png"));
        tiles.put(12, new Texture("tile_12.png"));
    }

    public Texture getTile(int nr) {
        return tiles.get(nr);
    }

}
