package com.loop.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.I18NBundle;
import com.loop.game.States.MainMenu;

import java.util.Locale;

public class LoopGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public Skin skin;
    public I18NBundle loc;
    public static final int WIDTH = 480;//Gdx.app.getGraphics().getWidth();
    public static final int HEIGHT = 800;//Gdx.app.getGraphics().getHeight();
    public static final String TITLE = "Loop";

    private FileHandle langPath;

    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        skin.add("bg_black", texture);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texture = new Texture(pixmap);
        skin.add("bg_white", texture);
        langPath = Gdx.files.internal("bundle/Loc");
        loc = I18NBundle.createBundle(langPath, new Locale("en"));
        this.setScreen(new MainMenu(this));
    }

    public void render() {
        Gdx.gl.glClearColor(48/255f, 48/255f, 48/255f, 1);
        super.render();
    }

    public void changeLang ( Locale lng ) { loc = I18NBundle.createBundle(langPath, lng); }

    public void dispose() {
        batch.dispose();
        font.dispose();
        skin.dispose();
    }

}
