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

        switch (type){
            case 10:
                tile = TileType.TILE_10;
                break;
            case 5:
                tile = TileType.TILE_5;
                break;
            case 9:
                tile = TileType.TILE_9;
                break;
            case 3:
                tile = TileType.TILE_3;
                break;
            case 6:
                tile = TileType.TILE_6;
                break;
            case 12:
                tile = TileType.TILE_12;
                break;
        }

    }

    public Vector3 getPosition() {
        return position;
    }

    public Texture getTexture() {
        return tile;
    }

    public void dispose(){
        tile.dispose();
    }
}
