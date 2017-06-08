package com.loop.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.loop.game.Net.Client;
import com.loop.game.Net.ConnectionListener;
import com.loop.game.Net.RatingEntry;
import com.loop.game.States.MainMenu;

import java.util.List;
import java.util.Locale;

public class LoopGame extends Game {
    public FreeTypeFontGenerator generator;
    public SpriteBatch BATCH;
    public BitmapFont font;
    public Skin skin;
    public I18NBundle loc;
    public static final String TITLE = "Loop";
    public static final int WIDTH = 480;
    public static final int HEIGHT = 800;
    public static final Viewport VIEWPORT = new FitViewport(WIDTH, HEIGHT);
    private final Client client;
    public static final ConnectionListener VOID_CONNECTION= new ConnectionListener(){
        @Override
        public boolean processCommand(String[] command)
        {
            return false;
        }

        @Override
        public void recieveRating(List<RatingEntry> rating)
        {
        }

        @Override
        public void connectionDown(boolean server)
        {
        }

        @Override
        public void done(boolean success)
        {
        }
    };

    private FileHandle langPath;

    public void create() {
        BATCH = new SpriteBatch();

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnmĘÓĄŚŁŻŹĆŃęóąśłżźćń1234567890!@#$%^&*():";
        parameter.size = 35;
        generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
        font = generator.generateFont(parameter);
        generator.dispose();

        skin = new Skin();
        skin.add("myFont", font, BitmapFont.class);
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin.atlas")));
        skin.load(Gdx.files.internal("uiskin.json"));
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

        loc = I18NBundle.createBundle(langPath, java.util.Locale.getDefault());
        this.setScreen(new MainMenu(this));
        Gdx.input.setCatchBackKey(true);
    }

    public void render() {
        Gdx.gl.glClearColor(48/255f, 48/255f, 48/255f, 1);
        super.render();
    }

    public void dispose() {
        BATCH.dispose();
        font.dispose();
        // skin.dispose();
    }

    public final Client getClient() { return client; }

    public LoopGame()
    {
        super();
        client = new Client(VOID_CONNECTION);
    }
}
