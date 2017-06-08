package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;

import java.util.Locale;

/**
 * Created by tobi on 4/28/17.
 */

public class OptionsMenu extends BasicScreen {
    // TODO: plik cfg?

    private final TextButton soundBtn;
    private final TextButton langBtn;
    private final TextButton backBtn;
    private boolean soundFlag;

    // zmienne temp, do testow
        private boolean langFlag; // 1 - ang, 0 - polski
    // koniec temp

    public OptionsMenu(final LoopGame game) {
        super(game);
        this.soundFlag = true; // docelowo pobranie z cfg
        this.langFlag = true; // docelowo pobranie z cfg
        this.soundBtn = new TextButton(getString(soundFlag ? "soundOn" : "soundOff"), game.skin);
        this.langBtn = new TextButton(getString(langFlag ? "lngEnglish" : "lngPolish"), game.skin);
        this.backBtn = new TextButton(getString("back"), game.skin);
        fillStage();
        Gdx.input.setInputProcessor(getStage());
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.addActor(soundBtn);
        vg.addActor(langBtn);
        vg.addActor(backBtn);
        vg.center();
        setButtonActions();
        getStage().addActor(vg);
    }

    private void setButtonActions() {
        soundBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
            soundFlag = !soundFlag;
            soundBtn.setText(getString(soundFlag ? "soundOn" : "soundOff"));
            }
        });

        langBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
            langFlag = !langFlag;
            getApp().changeLang(langFlag ? new Locale("en") : new Locale("pl"));
            updateLabels();
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
            // TODO: zapis opcji

                backToMainMenu();
            }
        });
    }

    private void updateLabels () {
        this.soundBtn.setText(getString(soundFlag ? "soundOn" : "soundOff"));
        this.langBtn.setText(getString(langFlag ? "lngEnglish" : "lngPolish"));
        this.backBtn.setText(getString("back"));
    }

    protected void backButtonClicked()
    {
        backToMainMenu();
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
                    getApp().setScreen(new PlayOnline(getApp()));
                    dispose();
                }});
        }
        return true;
    }
}
