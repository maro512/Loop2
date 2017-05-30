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
    private TextField userField, passField;
    private TextButton logBtn;
    private TextButton backBtn;
    private final int TEXT_FIELDS = 2;

    public Log(final LoopGame game) {
        this.game = game;
        this.logBtn = new TextButton(game.loc.get("login"), game.skin);
        this.backBtn = new TextButton(game.loc.get("back"), game.skin);

        userField = new TextField("", game.skin);
        passField = new TextField("", game.skin);

        userField.setMessageText(game.loc.get("username"));
        passField.setMessageText(game.loc.get("password"));
        passField.setPasswordCharacter('*');
        passField.setPasswordMode(true);

        this.stage = new Stage(new ScreenViewport(), game.batch);
        fillStage();
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.center();
        vg.addActor(userField);
        vg.addActor(passField);
        vg.addActor(logBtn);
        vg.addActor(backBtn);
        setButtonActions();
        stage.addActor(vg);
    }

    private void hideKeyboard()
    {
        passField.getOnscreenKeyboard().show(false);
        userField.getOnscreenKeyboard().show(false);
    }

    private void setButtonActions() {
        logBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                hideKeyboard();
                // TODO
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideKeyboard();
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
    public void dispose()
    {
        hideKeyboard();
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
