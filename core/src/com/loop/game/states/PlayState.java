package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.loop.game.GameModel.BasicPosition;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Tile;
import com.loop.game.LoopGame;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.Input.Peripheral.Vibrator;

/**
 * Created by marek on 15.04.17.
 */

public class PlayState extends State {
    private Texture boardBackground;
    private Texture crrPlayer;
    private Texture chosenCell;
    private Texture background;
    private boolean firstPlayer;
    private Vector3 delta;
    private Sound error;

    private Map<Byte, Texture> tileTextures;
    private Map<Byte, Texture> shadowTileTextures;
    private Collection<Cell> cells;
    private final String test = "player name";

    private BitmapFont font;

    private Game game;
    private int chosenX, chosenY;
    private byte menuStructure[];

    private static final int NO_COLUMNS = 10;
    private static final int NO_ROW = 14;
    private static final int BOARD_MARGIN = 10;


    private final int boardWidth = LoopGame.WIDTH - 2 * BOARD_MARGIN;
    private final int cellSize = boardWidth / NO_COLUMNS;
    private final int boardHeight = NO_ROW * cellSize;

    private static final int MENU_MARGIN = 10;
    private static final int MENU_LEFT_MARGIN = 10;
    private static final int MENU_BOTTOM_MARGIN = 10;
    private static final int MENU_CELL_SIZE = (LoopGame.WIDTH - 2 * MENU_LEFT_MARGIN - 5 * MENU_MARGIN) / 6;

    private static final int BOTTOM_MARGIN = MENU_MARGIN + MENU_CELL_SIZE + BOARD_MARGIN;


    public PlayState(GameStateManager gsm) {
        super(gsm);

        buildMenuStructure();

        boardBackground = new Texture("boardBackground.png");
        crrPlayer = new Texture("crrPlayer.png");
        chosenCell = new Texture("chosenCell.png");
        background = new Texture("background.png");
        error = Gdx.audio.newSound(Gdx.files.internal("error.wav"));
        firstPlayer = true;
        cam.setToOrtho(false, LoopGame.WIDTH, LoopGame.HEIGHT);
        font = new BitmapFont(Gdx.files.internal("font.fnt"));

        game = new Game();

        cells = game.getBoardView();

        delta = new Vector3();
        delta.add(5 * cellSize, 7 * cellSize, 0);


        tileTextures = new HashMap<Byte, Texture>();
        shadowTileTextures = new HashMap<Byte, Texture>();
        addTileTextures();

    }

    @Override
    public void handleInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            int prevX, prevY;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 position = new Vector3();
                cam.unproject(position.set(screenX, screenY, 0));

                if (isOnBoard(position)) {
                    setChosenPosition(position);
                } else if (isOnMenu(position)) {
                    choseType(position);
                } else if (position.x >= 0
                        && position.x < BOARD_MARGIN
                        && position.y < BOTTOM_MARGIN + boardHeight
                        && position.y >= BOTTOM_MARGIN){
                    delta.x -= cellSize;
                } else if (position.x >= LoopGame.WIDTH - BOARD_MARGIN
                        && position.x < LoopGame.WIDTH
                        && position.y < BOTTOM_MARGIN + boardHeight
                        && position.y >= BOTTOM_MARGIN){
                    delta.x += cellSize;
                } else if (position.x >= BOARD_MARGIN
                        && position.x < LoopGame.WIDTH - BOARD_MARGIN
                        && position.y < BOTTOM_MARGIN
                        && position.y >= BOTTOM_MARGIN - BOARD_MARGIN){
                    delta.y -= cellSize;
                } else if (position.x >= BOARD_MARGIN
                        && position.x < LoopGame.WIDTH - BOARD_MARGIN
                        && position.y < BOTTOM_MARGIN + boardHeight + BOARD_MARGIN
                        && position.y >= BOTTOM_MARGIN + boardHeight){
                    delta.y += cellSize;
                }


                prevX = screenX;
                prevY = screenY;

                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
//                delta.x = (screenX - prevX)/10;
//                delta.y = (-screenY + prevY)/10;

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
        sb.draw(background, 0, 0, LoopGame.WIDTH, LoopGame.HEIGHT);
        sb.draw(boardBackground, BOARD_MARGIN, BOTTOM_MARGIN, boardWidth, boardHeight);

        font.draw(sb, test, firstPlayer ? 0 : LoopGame.WIDTH / 2,
                crrPlayer.getHeight() / 2 + BOTTOM_MARGIN + boardHeight);

        renderMenu(sb);

        boolean isTerminated = game.isTerminated();
        for (Cell c : cells) {
            renderCell(sb, c, isTerminated);
        }
        if (isTerminated){
            List<Tile> tiles = game.getWinLine();
            for (Tile t : tiles) {
                renderCell(sb, t, false);
            }
        }

        sb.end();
    }

    @Override
    public void dispose() {
        boardBackground.dispose();
        crrPlayer.dispose();
        chosenCell.dispose();
        background.dispose();
        Collection<Texture> textures = tileTextures.values();
        for (Texture t : textures) {
            t.dispose();
        }
        textures = shadowTileTextures.values();
        for (Texture t : textures) {
            t.dispose();
        }
        error.dispose();
    }

    private void setChosenPosition(Vector3 position) {
        int x = (int) (position.x - BOARD_MARGIN) / cellSize;
        int y = (int) (position.y - BOTTOM_MARGIN) / cellSize;

        chosenX = x - (int) delta.x / cellSize;
        chosenY = y - (int) delta.y / cellSize;

        if (game.getPossibleMoves(chosenX, chosenY).isEmpty()){
            error.play();
        }

    }

    private void choseType(Vector3 position) {
        int x = (int) (position.x - MENU_LEFT_MARGIN) * 6 / (6 * MENU_CELL_SIZE + 5 * MENU_MARGIN);

        byte type = menuStructure[x];

        List<Byte> possibleMoves = game.getPossibleMoves(chosenX, chosenY);
        if (possibleMoves.contains(type)) {
            game.makeMove(chosenX, chosenY, type);
            firstPlayer = !firstPlayer;
        } else {
            error.play();
        }

    }

    private boolean isOnBoard(Vector3 position) {
        return position.x >= BOARD_MARGIN
                && position.x < LoopGame.WIDTH - BOARD_MARGIN
                && position.y < BOTTOM_MARGIN + boardHeight
                && position.y >= BOTTOM_MARGIN;
    }


    private boolean isOnMenu(Vector3 position) {
        return position.x > MENU_LEFT_MARGIN
                && position.x < LoopGame.WIDTH - MENU_LEFT_MARGIN
                && position.y < MENU_BOTTOM_MARGIN + MENU_CELL_SIZE
                && position.y > MENU_BOTTOM_MARGIN;
    }

    private void addTileTextures() {
        byte[] allTypes = Tile.ALL_TYPES;
        for (Byte b : allTypes) {
            String s = "tile_" + b + ".png";
            tileTextures.put(b, new Texture(s));
            String ss = "tile_" + b + "s.png";
            shadowTileTextures.put(b, new Texture(ss));
        }
        tileTextures.put((byte) 0, new Texture("emptyCell.png"));
    }

    private void renderMenu(SpriteBatch sb) {
        int dx = MENU_MARGIN + MENU_CELL_SIZE;

        List<Byte> possibleMoves = game.getPossibleMoves(chosenX, chosenY);

        for (int i = 0; i < 6; i++) {
            if (possibleMoves.contains(menuStructure[i])) {
                sb.draw(tileTextures.get(menuStructure[i]), MENU_LEFT_MARGIN + i * dx, MENU_BOTTOM_MARGIN, MENU_CELL_SIZE, MENU_CELL_SIZE);
            } else {
                sb.draw(shadowTileTextures.get(menuStructure[i]), MENU_LEFT_MARGIN + i * dx, MENU_BOTTOM_MARGIN, MENU_CELL_SIZE, MENU_CELL_SIZE);
            }
        }

    }

    private void renderCell(SpriteBatch sb, Cell cell, boolean isTerminated) {
        if (cell.getType()<0) return;
        Vector3 position = new Vector3();
        position.set(cell.getX() * cellSize + BOARD_MARGIN + delta.x, cell.getY() * cellSize + BOTTOM_MARGIN + delta.y, 0);

        if (isTerminated) {
            if (isOnBoard(position) && cell.getType() > 0) {
                Texture texture = shadowTileTextures.get(cell.getType());
                sb.draw(texture, position.x, position.y, cellSize, cellSize);
            }

        } else {
            if (isOnBoard(position)) {
                Texture texture = tileTextures.get(cell.getType());
                sb.draw(texture, position.x, position.y, cellSize, cellSize);
            }
            if (cell.getX() == chosenX && cell.getY() == chosenY && cell.getType() == 0) {
                sb.draw(chosenCell, position.x, position.y, cellSize, cellSize);
            }
        }


    }

    private void buildMenuStructure() {
        menuStructure = new byte[6];
        menuStructure[0] = Tile.TYPE_NS;
        menuStructure[1] = Tile.TYPE_WE;
        menuStructure[2] = Tile.TYPE_SE;
        menuStructure[3] = Tile.TYPE_NE;
        menuStructure[4] = Tile.TYPE_NW;
        menuStructure[5] = Tile.TYPE_SW;
    }

}
