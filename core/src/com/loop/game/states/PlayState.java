package com.loop.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.loop.game.LoopGame;
import com.loop.game.Tile;
import com.loop.game.TileType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by marek on 15.04.17.
 */

public class PlayState extends State {
    private Texture boardBackground;
    private Texture crrPlayer;
    private boolean firstPlayer;
    //public AssetManager manager = new AssetManager();

    private Map<Byte, Tile> menu;
    private Array<Tile> cells;
    private List<Byte> menuTiles;

    private static final int NO_COLUMNS = 10;
    private static final int NO_ROW = 11;
    private static final int BOARD_MARGIN = 10;
    private static final int BOTTOM_MARGIN = 190;

    private final int boardWidth = LoopGame.WIDTH - 2 * BOARD_MARGIN;
    private final int cellSize = boardWidth / NO_COLUMNS;
    private final int boardHeight = NO_ROW * cellSize;

    private static final int MENU_MARGIN = 10;
    private static final int MENU_LEFT_MARGIN = 10;
    private static final int MENU_BOTTOM_MARGIN = 10;
    private static final int MENU_CELL_SIZE = 50;


    public PlayState(GameStateManager gsm) {
        super(gsm);
        TileType.load();
        menu = new HashMap<Byte, Tile>();
        addMenuTiles();
        cells = new Array<Tile>();
        menuTiles = new ArrayList<Byte>();
        resetTilesMenu();
        boardBackground = new Texture("board.png");
        crrPlayer = new Texture("crrPlayer.png");
        firstPlayer = true;
        cam.setToOrtho(false, LoopGame.WIDTH, LoopGame.HEIGHT);



//        manager.load("tile_10.png", Texture.class);
//        manager.load("tile_5.png", Texture.class);
//        manager.load("tile_9.png", Texture.class);
//        manager.load("tile_3.png", Texture.class);
//        manager.load("tile_6.png", Texture.class);
//        manager.load("tile_12.png", Texture.class);
    }

    @Override
    public void handleInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 position = new Vector3();
                cam.unproject(position.set(screenX, screenY, 0));

                if (isOnBoard(position)) {
                    sendBoardPosition(position);
                } else if (isOnMenu(position)) {
                    sendType(position);
                }


                return true;
            }

        });

    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(boardBackground, BOARD_MARGIN, BOTTOM_MARGIN, boardWidth, boardHeight);
        sb.draw(crrPlayer, firstPlayer ? 0 : LoopGame.WIDTH / 2, BOTTOM_MARGIN + boardHeight,
                LoopGame.WIDTH / 2, LoopGame.HEIGHT - BOTTOM_MARGIN - boardHeight);

        for (Tile t : cells) {
            sb.draw(t.getTexture(), t.getPosition().x, t.getPosition().y, cellSize, cellSize);
        }

        for (Byte b : menuTiles) {
            Tile t = menu.get(b);
            sb.draw(t.getTexture(), t.getPosition().x, t.getPosition().y, MENU_CELL_SIZE, MENU_CELL_SIZE);
        }

        sb.end();
    }

    @Override
    public void dispose() {
        boardBackground.dispose();
        crrPlayer.dispose();
    }

    public void addCell(int x, int y, byte type) {
        int boardX = x * cellSize + BOARD_MARGIN;
        int boardY = y * cellSize + BOTTOM_MARGIN;
        cells.add(new Tile(boardX, boardY, (int) type));
    }

    private void sendBoardPosition(Vector3 position) {
        int x = (int) (position.x - BOARD_MARGIN) / cellSize;
        int y = (int) (position.y - BOTTOM_MARGIN) / cellSize;
        //game.getposiblemoves?

        //do testow
        Random rand = new Random();
        int r = rand.nextInt(6);
        byte b = 3;

        switch (r){
            case 0:
                b = 10;
                break;
            case 1:
                b = 5;
                break;
            case 2:
                b = 9;
                break;
            case 3:
                b = 3;
                break;
            case 4:
                b = 6;
                break;
            case 5:
                b = 12;
                break;
        }

        addCell(x, y, b);
    }

    private void sendType(Vector3 position) {
        int x = (int) (position.x - MENU_LEFT_MARGIN) * 3 / (3 * MENU_CELL_SIZE + 2 * MENU_MARGIN);
        int y = (int) (position.y - MENU_BOTTOM_MARGIN) * 2 / (2 * MENU_CELL_SIZE + MENU_MARGIN);

        int type;

        if (y == 0) {
            if (x == 0) type = 10;
            else if (x == 1) type = 9;
            else type = 6;
        } else {
            if (x == 0) type = 5;
            else if (x == 1) type = 3;
            else type = 12;
        }
        //game.type
        System.out.println(type);
    }

    public void displayPossibleTiles(List<Byte> tiles) {
        menuTiles.clear();
        menuTiles.addAll(tiles);
    }

    public void displayCrrPlayer(int player) { //player 1 albo 2
        firstPlayer = player == 1;
    }

    public void resetTilesMenu() {
        menuTiles.clear();
        menuTiles.add((byte) 10);
        menuTiles.add((byte) 5);
        menuTiles.add((byte) 9);
        menuTiles.add((byte) 3);
        menuTiles.add((byte) 6);
        menuTiles.add((byte) 12);

    }

    private boolean isOnBoard(Vector3 position) {
        return position.x > BOARD_MARGIN
                && position.x < LoopGame.WIDTH - BOARD_MARGIN
                && position.y < BOTTOM_MARGIN + boardHeight
                && position.y > BOTTOM_MARGIN;
    }

    private void addMenuTiles() {
        int column1 = MENU_LEFT_MARGIN;
        int column2 = column1 + MENU_MARGIN + MENU_CELL_SIZE;
        int column3 = column2 + MENU_MARGIN + MENU_CELL_SIZE;
        int row1 = MENU_BOTTOM_MARGIN;
        int row2 = row1 + MENU_MARGIN + MENU_CELL_SIZE;

        menu.put((byte) 10, new Tile(column1, row1, 10));
        menu.put((byte) 5, new Tile(column1, row2, 5));
        menu.put((byte) 9, new Tile(column2, row1, 9));
        menu.put((byte) 3, new Tile(column2, row2, 3));
        menu.put((byte) 6, new Tile(column3, row1, 6));
        menu.put((byte) 12, new Tile(column3, row2, 12));
    }

    private boolean isOnMenu(Vector3 position) {
        return position.x > MENU_LEFT_MARGIN
                && position.x < MENU_LEFT_MARGIN + 3 * MENU_CELL_SIZE + 2 * MENU_MARGIN
                && position.y < MENU_BOTTOM_MARGIN + 2 * MENU_CELL_SIZE + MENU_MARGIN
                && position.y > MENU_BOTTOM_MARGIN;
    }
}
