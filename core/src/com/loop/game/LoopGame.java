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
    public static final int WIDTH = 640;//Gdx.app.getGraphics().getWidth();
    public static final int HEIGHT = 800;//Gdx.app.getGraphics().getHeight();
    public static final String TITLE = "Loop";

    private FileHandle langPath;

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        langPath = Gdx.files.internal("bundle/Loc");
        loc = I18NBundle.createBundle(langPath, new Locale("en"));
        this.setScreen(new MainMenu(this));
    }

    public void render() { super.render(); }

    public void changeLang ( Locale lng ) { loc = I18NBundle.createBundle(langPath, lng); }

    public void dispose() {
        batch.dispose();
        font.dispose();
        skin.dispose();
    }

}
