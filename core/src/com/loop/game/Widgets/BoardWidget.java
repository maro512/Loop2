package com.loop.game.Widgets;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Tile;
import com.loop.game.States.PlayScreen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tobi on 5/24/17.
 */

public class BoardWidget extends Widget {
    private Vector2 pos, size, camera, dragPos;
    private Rectangle bounds;
    private Pixmap pixmap;
    private Texture texture;
    private Map<Byte, Texture> tileTextures;
    private Map<Byte, Texture> shadowedTileTextures;
    private Texture selectedTexture;
    private Collection<Cell> cells;
    private int tileWidth, tileHeight;
    private final float SCALE = .25f;
    private boolean started = false, drag = false;
    private Game game;
    private PlayScreen play;

    // TODO: Make fitviewport work

    public final InputListener listener = new InputListener() {
        private Vector2 dragStart;

        @Override
        public boolean touchDown(InputEvent e, float x, float y, int pointer, int button)
        {
            if(game.getSelected() != null) {
                game.setSelected(null);
                play.disableAllButtons();
                return false;
            }
            Vector2 pos = new Vector2((int)Math.floor((x-camera.x)/tileWidth),
                    (int)Math.floor((y-camera.y)/tileHeight));
            Cell hovered = game.getCell(pos);
            if(hovered != null && hovered.getType() == 0) {
                game.setSelected(hovered);
                play.updateMenu(hovered);
            } else {
                drag = true;
                dragStart = new Vector2(x, y);
            }
            return drag;
        }

        @Override
        public void touchDragged(InputEvent e, float x, float y, int pointer)
        {
            dragPos.x = x - dragStart.x;
            dragPos.y = y - dragStart.y;
        }

        @Override
        public void touchUp(InputEvent e, float x, float y, int pointer, int button)
        {
            drag = false;
            camera.x += dragPos.x;
            camera.y += dragPos.y;
            dragPos = new Vector2(0,0);
            super.touchUp(e, x, y, pointer, button);
        }
    };

    public BoardWidget(Skin skin, Game game, PlayScreen play) {
        tileTextures = new HashMap<Byte, Texture>();
        shadowedTileTextures= new HashMap<Byte, Texture>();
        this.game = game;
        this.play = play;
        this.cells = game.getBoardView();
        byte[] allTypes = Tile.ALL_TYPES;
        for (Byte b : allTypes) {
            tileTextures.put(b, new Texture("tile_" + b + ".png"));
            shadowedTileTextures.put(b, new Texture("tile_" + b + "s.png"));
        }
        tileTextures.put((byte)-1, new Texture("deadTile.png"));
        Texture emptyCell = new Texture("emptyCell.png");
        tileTextures.put((byte)0, emptyCell);
        tileWidth = (int) (emptyCell.getWidth() * SCALE);
        tileHeight = (int) (emptyCell.getHeight() * SCALE);
        addListener(listener);
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(skin.getColor("boardBackground"));
        pixmap.fill();
        texture = new Texture(pixmap);
        camera = new Vector2(0, 0);
        bounds = new Rectangle(0, 0, 0, 0);
        dragPos = new Vector2(0,0);
        selectedTexture = new Texture("chosenCell.png");
    }

    @Override
    public void act(float delta) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(texture, pos.x, pos.y, size.x, size.y);
        ScissorStack.pushScissors(bounds);


        if (!game.isTerminated()) {
            Cell selected = game.getSelected();
            for (Cell cell : cells) {
                batch.setColor(1, 1, 1, parentAlpha);
                if (cell == selected) {
                    batch.draw(selectedTexture, pos.x + camera.x + dragPos.x + cell.getX() * tileWidth,
                            pos.y + camera.y + dragPos.y + cell.getY() * tileHeight, tileWidth, tileHeight);
                } else {
                    batch.draw(tileTextures.get(cell.getType()), pos.x + camera.x + dragPos.x + cell.getX() * tileWidth,
                            pos.y + camera.y + dragPos.y + cell.getY() * tileHeight, tileWidth, tileHeight);
                }
            }
        } else {
            List<Tile> winningLine = game.getWinningLine();

            batch.setColor(.5f, .5f, .5f, parentAlpha);
            for (Cell cell : cells) {
                if (cell.isTile()) {
                    batch.draw(tileTextures.get(cell.getType()), pos.x + camera.x + dragPos.x + cell.getX() * tileWidth,
                            pos.y + camera.y + dragPos.y + cell.getY() * tileHeight, tileWidth, tileHeight);
                }
            }

            batch.setColor(1, 1, 1, parentAlpha);
            for (Tile tile : winningLine) {
                if (tile != null) {
                    batch.draw(tileTextures.get(tile.getType()), pos.x + camera.x + dragPos.x + tile.getX() * tileWidth,
                            pos.y + camera.y + dragPos.y + tile.getY() * tileHeight, tileWidth, tileHeight);
                }

            }
        }
        batch.flush();
        ScissorStack.popScissors();
    }

    @Override
    protected void sizeChanged() {
        pos = new Vector2(getX(), getY());
        size = new Vector2(getWidth(), getHeight());
        bounds.setPosition(pos.x, pos.y);
        bounds.setSize(size.x, size.y);
        if(!started) {
            camera.x = getWidth() / 2 - tileWidth / 2;
            camera.y = getHeight() / 2 - tileHeight / 2;
            started = true;
        }
    }
}
