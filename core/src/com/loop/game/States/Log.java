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
import com.loop.game.LoopGame;

/**
 * Created by tobi on 4/28/17.
 */

public class Log implements Screen {
    private final LoopGame game;
    private final Stage stage;
    private TextField [] inputField;
    private TextButton logBtn;
    private TextButton backBtn;
    private final int TEXT_FIELDS = 2;

    public Log(final LoopGame game) {
        this.game = game;
        inputField = new TextField [TEXT_FIELDS];
        this.logBtn = new TextButton(game.loc.get("login"), game.skin);
        this.backBtn = new TextButton(game.loc.get("back"), game.skin);

        for (int i=0; i<TEXT_FIELDS; ++i) {
            inputField[i] = new TextField("", game.skin);
        }

        inputField[0].setMessageText(game.loc.get("username"));
        inputField[1].setMessageText(game.loc.get("password"));
        inputField[1].setPasswordCharacter('*');
        inputField[1].setPasswordMode(true);

        this.stage = new Stage(new ScreenViewport(), game.batch);
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.center();

        for (int i=0; i<TEXT_FIELDS; ++i) {
            vg.addActor(inputField[i]);
        }

        vg.addActor(logBtn);
        vg.addActor(backBtn);

        setButtonActions();
        stage.addActor(vg);
    }

    private void setButtonActions() {
        logBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // TODO
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            back();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            back();
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

    private void back() {
        game.setScreen(new MainMenu(game));
        dispose();
    }
}
