package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Tile;
import com.loop.game.LoopGame;

/**
 * Created by tobi on 4/28/17.
 */

public class MainMenu implements Screen {
    private final LoopGame game;
    private final Stage stage;
    private final TextButton offlineBtn;
    private final TextButton onlineBtn;
    private final TextButton optBtn;
    private byte menuStructure[];

    public MainMenu(final LoopGame game) {
        this.game = game;
        this.offlineBtn = new TextButton(game.loc.get("startOffline"), game.skin);
        this.onlineBtn = new TextButton(game.loc.get("startOnline"), game.skin);
        this.optBtn = new TextButton(game.loc.get("options"), game.skin);
        this.stage = new Stage(new ScreenViewport(), game.batch);
        this.menuStructure = new byte[6];
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void buildMenuStructure() {
        menuStructure[0] = Tile.TYPE_NS;
        menuStructure[1] = Tile.TYPE_WE;
        menuStructure[2] = Tile.TYPE_SE;
        menuStructure[3] = Tile.TYPE_NE;
        menuStructure[4] = Tile.TYPE_NW;
        menuStructure[5] = Tile.TYPE_SW;
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.addActor(offlineBtn);
        vg.addActor(onlineBtn);
        vg.addActor(optBtn);
        vg.center();
        setButtonActions();
        stage.addActor(vg);
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
                System.out.println("Online Game Button Pressed");
            }
        });

        optBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                game.setScreen(new OptionsMenu(game));
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
}