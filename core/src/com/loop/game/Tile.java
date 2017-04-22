package com.loop.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by marek on 21.04.17.
 */

public class Tile {
    private Vector3 position;
    private Texture tile;

    public Tile(int x, int y, int type) {
        position = new Vector3(x,y,0);
        tile = TileType.instace.getTile(type);

    }

    public Vector3 getPosition() {
        return position;
    }

    public Texture getTexture() {
        return tile;
    }

}
