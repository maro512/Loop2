package com.loop.game.States;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Created by tobi on 5/24/17.
 */

public class BoardView extends Widget {
    private Texture emptyCell;
    private Vector2 pos;
    private Vector2 size;
    Pixmap pixmap;
    Texture texture;
    TextureRegion region;
    TextureRegionDrawable drawable;

    public final InputListener listener = new InputListener() {
        private float lastUpdateX;
        private float lastUpdateY;

        @Override
        public boolean touchDown(InputEvent e, float x, float y, int pointer, int button)
        {
            System.out.println(" " + x + " " + y);
            return super.touchDown(e, x, y, pointer, button);
        }

        @Override
        public void touchUp(InputEvent e, float x, float y, int pointer, int button)
        {
            System.out.println(" " + x + " " + y);
            super.touchUp(e, x, y, pointer, button);
        }
    };

    private float amountWidth = 5;
    private float amountHeight = 8;

    BoardView(Skin skin) {
        emptyCell = new Texture("emptyCell.png");
        emptyCell.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        region = new TextureRegion(emptyCell);
        drawable = new TextureRegionDrawable(region);

        addListener(listener);
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(skin.getColor("black"));
        pixmap.fill();
        texture = new Texture(pixmap);
    }

    @Override
    public void act(float delta) {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(1, 1, 1, parentAlpha);
        batch.draw(texture, pos.x, pos.y, size.x, size.y);
        batch.draw(region, pos.x, pos.y, size.x, size.y);
        /*for(int y=(int)-amountHeight/2-1; y<amountHeight/2+1; y++) {
            for(int x=(int)-amountWidth/2-1; x<amountWidth/2+1; x++) {
                emptyCellSprite.setPosition(pos.x+x*w, pos.y+y*h);
                emptyCellSprite.draw(batch);
            }
        }*/
    }

    @Override
    protected void sizeChanged() {
        pos = new Vector2(getX(), getY());
        size = new Vector2(getParent().getWidth(), getHeight());
        System.out.println(size);
        region.setRegion(pos.x, pos.y, emptyCell.getWidth()*size.x*amountWidth/480,
                emptyCell.getHeight()*size.y*amountHeight/628);
    }
}
