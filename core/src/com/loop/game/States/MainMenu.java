package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;
import com.loop.game.Net.ConnectionListener;
import com.loop.game.Net.RatingEntry;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tobi on 4/28/17.
 */

public class MainMenu implements Screen, ConnectionListener
{
    private final LoopGame game;
    private final Stage stage;
    private final Image logo;
    private final TextButton offlineBtn;
    private final TextButton onlineBtn;
    private final TextButton registerBtn;
    private final TextButton logBtn;
    private final float BUTTON_PAD = 5;

    public MainMenu(final LoopGame game)
    {
        game.getClient().setConnectionListener(this);
        this.logo = new Image(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("logo-loop.png"))))
                              , Scaling.fit, Align.top);
        this.game = game;
        this.offlineBtn = new TextButton(game.loc.get("startOffline"), game.skin);
        this.onlineBtn = new TextButton(game.loc.get("startOnline"), game.skin);
        this.registerBtn = new TextButton(game.loc.get("register"), game.skin);
        this.logBtn = new TextButton(game.loc.get("login"), game.skin);
        if (game.getClient().isServerConnected()) logBtn.setText(game.loc.get("logout"));
        this.stage = new Stage(game.VIEWPORT, game.BATCH);
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void fillStage() {
        Table table = new Table();
        table.setFillParent(true);
        table.padTop(5*BUTTON_PAD);
        table.add(logo);
        table.row(); table.add(offlineBtn).padBottom(BUTTON_PAD);
        table.row(); table.add(onlineBtn).padBottom(BUTTON_PAD);
        table.row(); table.add(registerBtn).padBottom(BUTTON_PAD);
        table.row(); table.add(logBtn).padBottom(BUTTON_PAD);
        setButtonActions();
        stage.addActor(table);
    }

    private void setButtonActions() {
        offlineBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                game.setScreen(new OfflinePlayerInput(game));
                dispose();
            }
        });

        onlineBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                if(game.getClient()!=null && game.getClient().isServerConnected())
                {
                    game.setScreen(new OnlinePlayInput(game));
                    dispose();
                } else
                    try {
                        System.out.println(InetAddress.getLocalHost().getHostAddress());
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
            }
        });

        registerBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                game.setScreen(new Register(game));
                dispose();
            }
        });

        logBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getClient().isServerConnected())
                {
                    System.out.println("Już zalogowano!");
                    return;
                }
                game.setScreen(new Log(game));
                dispose();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            Gdx.app.exit();
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public boolean processCommand(String[] command)
    {
        if (command[0].equals(Client.CMD_PLAY))
        {
            Gdx.app.postRunnable(new Runnable(){
                @Override
                public void run()
                {
                    game.setScreen(new PlayOnline(game));
                    dispose();
                }});
        }
        return true;
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
}
