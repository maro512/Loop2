package com.loop.game.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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

/**
 * Created by marek on 15.04.17.
 */

public class PlayState extends State {
    private Texture boardBackground;
    private Texture crrPlayer;
    private boolean firstPlayer;
    private Vector3 delta;

    private Map<Byte, Texture> tileTextures;
    private Collection<Cell> cells;
    private final String test = "player name";
    private BitmapFont font;

    private Game game;
    private int chosenX, chosenY;
    private byte menuStructure[][];

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

        buildMenuStructure();

        boardBackground = new Texture("board.png");
        crrPlayer = new Texture("crrPlayer.png");
        firstPlayer = true;
        cam.setToOrtho(false, LoopGame.WIDTH, LoopGame.HEIGHT);
        font = new BitmapFont(Gdx.files.internal("font.fnt"));

        game = new Game();

        cells = game.getBoardView();

        delta = new Vector3();
        delta.add(5*cellSize, 5*cellSize,0);

        tileTextures = new HashMap<Byte, Texture>();
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
        cells = game.getBoardView();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(boardBackground, BOARD_MARGIN, BOTTOM_MARGIN, boardWidth, boardHeight);
        sb.draw(crrPlayer, firstPlayer ? 0 : LoopGame.WIDTH / 2, BOTTOM_MARGIN + boardHeight,
                LoopGame.WIDTH / 2, LoopGame.HEIGHT - BOTTOM_MARGIN - boardHeight);
        font.draw(sb, test, firstPlayer ? 0 : LoopGame.WIDTH / 2,
                  crrPlayer.getHeight()/2 + BOTTOM_MARGIN + boardHeight );

        renderMenu(sb);

        for (Cell c : cells){
            renderCell(sb, c);
        }

        sb.end();
    }

    @Override
    public void dispose() {
        boardBackground.dispose();
        crrPlayer.dispose();
        Collection<Texture> textures = tileTextures.values();
        for (Texture t : textures){
            t.dispose();
        }
    }

    private void setChosenPosition(Vector3 position) {
        int x = (int) (position.x - BOARD_MARGIN) / cellSize;
        int y = (int) (position.y - BOTTOM_MARGIN) / cellSize;

        chosenX = x - 5;
        chosenY = -y + 5;
    }

    private void choseType(Vector3 position) {
        int x = (int) (position.x - MENU_LEFT_MARGIN) * 3 / (3 * MENU_CELL_SIZE + 2 * MENU_MARGIN);
        int y = (int) (position.y - MENU_BOTTOM_MARGIN) * 2 / (2 * MENU_CELL_SIZE + MENU_MARGIN);

        byte type = menuStructure[x][y];

        List<Byte> possibleMoves = game.getPossibleMoves(chosenX, chosenY);
        if (possibleMoves.contains(type)){
            game.makeMove(chosenX, chosenY, type);
            firstPlayer = !firstPlayer;
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
                && position.x < MENU_LEFT_MARGIN + 3 * MENU_CELL_SIZE + 2 * MENU_MARGIN
                && position.y < MENU_BOTTOM_MARGIN + 2 * MENU_CELL_SIZE + MENU_MARGIN
                && position.y > MENU_BOTTOM_MARGIN;
    }

    private void addTileTextures(){
        byte[] allTypes = Tile.ALL_TYPES;
        for (Byte b : allTypes) {
            String s = "tile_" + b + ".png";
            tileTextures.put(b, new Texture(s));
        }
    }

    private void renderMenu(SpriteBatch sb){
        int column[] = {MENU_LEFT_MARGIN,
                MENU_LEFT_MARGIN + MENU_MARGIN + MENU_CELL_SIZE,
                MENU_LEFT_MARGIN + 2*(MENU_MARGIN + MENU_CELL_SIZE)};
        int row[] = {MENU_BOTTOM_MARGIN,
                MENU_BOTTOM_MARGIN + MENU_MARGIN + MENU_CELL_SIZE};

        List<Byte> possibleMoves = game.getPossibleMoves(chosenX, chosenY);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                if (possibleMoves.contains(menuStructure[i][j])) {
                    sb.draw(tileTextures.get(menuStructure[i][j]), column[i], row[j], MENU_CELL_SIZE, MENU_CELL_SIZE);
                } else {
                    sb.draw(crrPlayer, column[i], row[j], MENU_CELL_SIZE, MENU_CELL_SIZE);
                }
            }
        }

    }

    private void renderCell(SpriteBatch sb, Cell cell){
        BasicPosition bp = cell.getPosition();
        Vector3 position = new Vector3();
        position.set(bp.getX() * cellSize + BOARD_MARGIN + delta.x, -bp.getY() * cellSize + BOTTOM_MARGIN + delta.y, 0);

        if (cell.isTile()){
            Texture texture = tileTextures.get(((Tile) cell).getType());
            sb.draw(texture, position.x, position.y, cellSize, cellSize);
        } else if (isOnBoard(position)){
            sb.draw(crrPlayer, position.x, position.y, cellSize, cellSize);
        }
    }

    private void buildMenuStructure(){
        menuStructure = new byte[3][2];
        menuStructure[0][0] = Tile.TYPE_NS;
        menuStructure[0][1] = Tile.TYPE_WE;
        menuStructure[1][0] = Tile.TYPE_SE;
        menuStructure[1][1] = Tile.TYPE_NE;
        menuStructure[2][0] = Tile.TYPE_NW;
        menuStructure[2][1] = Tile.TYPE_SW;
    }

}
