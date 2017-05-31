package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Player;
import com.loop.game.LoopGame;

import java.util.Locale;

/**
 * Created by tobi on 4/28/17.
 */

public class OfflinePlayerInput implements Screen {
    private final LoopGame game;
    private final Stage stage;
    private TextField [] inputField;
    private Player [] players;
    private TextButton startBtn;
    private final int PLAYERS = 2;

    public OfflinePlayerInput(final LoopGame game) {
        this.game = game;
        players = new Player [2];
        inputField = new TextField [2];
        this.startBtn = new TextButton(game.loc.get("startGame"), game.skin);

        for (int i=0; i<PLAYERS; ++i) {
            inputField[i] = new TextField("", game.skin);
            inputField[i].setMessageText(game.loc.get("playerPrompt") + " " + (i+1));
        }

        this.stage = new Stage(new ScreenViewport(), game.batch);
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.center();

        for (int i=0; i<PLAYERS; ++i) {
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
                for (int i=0; i<PLAYERS; ++i) {
                    players[i] = new Player(inputField[i].getText(), i);
                }

                game.setScreen(new Play(game, players));
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta)
    {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            back();
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    private void back()
    {
        game.setScreen(new MainMenu(game));
        dispose();
    }

    @Override
    public void dispose()
    {
        for(TextField field : inputField)
            field.getOnscreenKeyboard().show(false);
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
