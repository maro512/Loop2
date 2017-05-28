package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.LoopGame;

/**
 * Created by Lord Actias on 2017-05-26.
 */

public class OnlinePlayerInput implements Screen {
    private final LoopGame game;
    private final Stage stage;
    private final int DATA = 2;
    private TextField[] inputField;
    private TextButton startBtn;

    public OnlinePlayerInput(final LoopGame game) {
        this.game = game;
        //players = new Player [2];
        inputField = new TextField [2];
        this.startBtn = new TextButton(game.loc.get("startGame"), game.skin);

        inputField[0] = new TextField("", game.skin);
        inputField[0].setMessageText("Login");
        inputField[1] = new TextField("", game.skin);
        inputField[1].setMessageText("Password");
        inputField[1].setPasswordMode(true);
        inputField[1].setPasswordCharacter('#');

        this.stage = new Stage(new ScreenViewport(), game.batch);
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.center();

        for (int i = 0; i< DATA; ++i) {
            vg.addActor(inputField[i]);
        }

        vg.addActor(startBtn);

        setButtonActions();
        stage.addActor(vg);
    }

    private void setButtonActions() {
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                String data[] = new String[DATA];
                for (int i = 0; i< DATA; ++i) {
                    data[i] = inputField[i].getText();
                }

                game.setScreen(new PlayOnline(game, data));
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
