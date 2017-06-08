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
import com.loop.game.LoopGame;

/**
 * Created by tobi on 4/28/17.
 */

public class Register extends BasicScreen
{
    private TextField [] inputField;
    private TextButton registerBtn;
    private TextButton backBtn;
    private final int TEXT_FIELDS = 3;

    public Register(final LoopGame game) {
        super(game);
        inputField = new TextField [TEXT_FIELDS];
        this.registerBtn = new TextButton(getString("makeRegister"), game.skin);
        this.backBtn = new TextButton(getString("back"), game.skin);

        for (int i=0; i<TEXT_FIELDS; ++i) {
            inputField[i] = new TextField("", game.skin);
        }

        inputField[0].setMessageText(getString("username"));

        for (int i=1; i<TEXT_FIELDS; ++i) {
            inputField[i].setMessageText(getString("password"));
            inputField[i].setPasswordCharacter('*');
            inputField[i].setPasswordMode(true);
        }
        fillStage();
        Gdx.input.setInputProcessor(getStage());
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.center();

        for (int i=0; i<TEXT_FIELDS; ++i) {
            vg.addActor(inputField[i]);
        }

        vg.addActor(registerBtn);
        vg.addActor(backBtn);

        setButtonActions();
        getStage().addActor(vg);
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
                backToMainMenu();
            }
        });
    }

    protected void backButtonClicked()
    {
        backToMainMenu();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        return false;
    }
}
