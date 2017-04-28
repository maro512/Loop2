package com.loop.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.loop.game.States.MainMenu;

import java.util.Locale;

public class LoopGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public Skin skin;
    public I18NBundle loc;
    public static final int WIDTH = 640;//Gdx.graphics.getWidth();
    public static final int HEIGHT = 800;//Gdx.graphics.getHeight();
    public static final String TITLE = "Loop";

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        FileHandle baseFileHandle = Gdx.files.internal("bundle/Loc");
        Locale locale = new Locale("pl");
        loc = I18NBundle.createBundle(baseFileHandle, locale);
        this.setScreen(new MainMenu(this));
    }

    public void render() { super.render(); }

    public void dispose() {
        batch.dispose();
        font.dispose();
        skin.dispose();
    }

}
