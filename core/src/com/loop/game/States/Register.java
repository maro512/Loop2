package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Player;
import com.loop.game.LoopGame;

import static com.loop.game.States.MainMenu.BUTTON_PAD;

/**
 * Created by tobi on 4/28/17.
 */

public class Register implements Screen {
    private final LoopGame game;
    private final Stage stage;
    private TextField [] inputField;
    private TextButton registerBtn;
    private TextButton backBtn;
    private final int TEXT_FIELDS = 3;

    public Register(final LoopGame game) {
        this.game = game;
        inputField = new TextField [TEXT_FIELDS];
        this.registerBtn = new TextButton(game.loc.get("makeRegister"), game.skin);
        this.backBtn = new TextButton(game.loc.get("back"), game.skin);

        for (int i=0; i<TEXT_FIELDS; ++i) {
            inputField[i] = new TextField("", game.skin);
        }

        inputField[0].setMessageText(game.loc.get("username"));

        for (int i=1; i<TEXT_FIELDS; ++i) {
            inputField[i].setMessageText(game.loc.get("password"));
            inputField[i].setPasswordCharacter('*');
            inputField[i].setPasswordMode(true);
        }

        this.stage = new Stage(new ScreenViewport(), game.batch);
        fillStage();
        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);
    }

    private void fillStage()
    {
        Table vg = new Table();
        vg.setFillParent(true);
        //vg.space(2);
        //vg.center();
        for (int i=0; i<TEXT_FIELDS; ++i) {
            vg.row(); vg.add(inputField[i]).padBottom(BUTTON_PAD).width(stage.getWidth()*.7f);
        }

        vg.row(); vg.add(registerBtn).padBottom(BUTTON_PAD).width(stage.getWidth()*.5f);
        vg.row(); vg.add(backBtn).padBottom(BUTTON_PAD);
        vg.row().fillY();
        setButtonActions();
        stage.addActor(vg);
    }

    private void setButtonActions() {
        registerBtn.addListener(new ChangeListener() {
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

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            back();
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        hideKeyboard();
        stage.dispose();
    }

    private void hideKeyboard()
    {
        inputField[0].getOnscreenKeyboard().show(false);
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
