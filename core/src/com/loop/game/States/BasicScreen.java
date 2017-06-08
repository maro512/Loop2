package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Player;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;
import com.loop.game.Net.ConnectionListener;
import com.loop.game.Net.RatingEntry;

import java.util.List;

/**
 * Created by Piotr on 30.05.2017.
 */
public abstract class BasicScreen implements Screen, ConnectionListener
{
    private final LoopGame application;
    private Stage stage;
    protected final float BUTTON_PAD = 5;

    public BasicScreen(final LoopGame game)
    {
        application = game;
        application.getClient().setConnectionListener(this);
        stage = new Stage(game.VIEWPORT, game.BATCH) {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                super.unfocusAll();
                Gdx.input.setOnscreenKeyboardVisible(false);
                return super.touchDown(screenX, screenY, pointer, button);
            }
        };
    }

    protected final Stage getStage() { return stage;}
    protected final Skin getSkin() { return application.skin;}
    protected final String getString(String id) { return application.loc.get(id);}
    protected final SpriteBatch getBatch() { return application.BATCH; }
    protected final LoopGame getApp() { return application; }
    protected final Client getClient() { return application.getClient(); }

    @Override
    public void resize(int width, int height)
    {
        application.VIEWPORT.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)) backButtonClicked();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    protected void backButtonClicked() {};

    protected final void backToMainMenu()
    {
        application.setScreen(new MainMenu(application));
        dispose();
    }

    @Override
    public void dispose()
    {
        stage.dispose();
    }

    @Override
    public void show()
    {
    }

    @Override
    public void hide()
    {
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resume()
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

    @Override
    public void recieveRating(List<RatingEntry> rating)
    {
    }
}
