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
import com.loop.game.LoopGame;

import java.util.Locale;

/**
 * Created by tobi on 4/28/17.
 */

public class OptionsMenu implements Screen {
    // TODO: plik cfg?
    private final LoopGame game;
    private final Stage stage;
    private final TextButton soundBtn;
    private final TextButton skinBtn;
    private final TextButton langBtn;
    private final TextButton backBtn;
    private boolean soundFlag;

    // zmienne temp, do testow
        private boolean langFlag; // 1 - ang, 0 - polski
    // koniec temp

    public OptionsMenu(final LoopGame game) {
        this.game = game;
        this.soundFlag = true; // docelowo pobranie z cfg
        this.langFlag = true; // docelowo pobranie z cfg
        this.soundBtn = new TextButton(game.loc.get(soundFlag ? "soundOn" : "soundOff"), game.skin);
        this.skinBtn = new TextButton(game.loc.get("skinText"), game.skin); // docelowo pobieranie z cfg
        this.langBtn = new TextButton(game.loc.get(langFlag ? "lngEnglish" : "lngPolish"), game.skin);
        this.backBtn = new TextButton(game.loc.get("back"), game.skin);
        this.stage = new Stage(new ScreenViewport());
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void fillStage() {
        VerticalGroup vg = new VerticalGroup();
        vg.setFillParent(true);
        vg.space(2);
        vg.addActor(soundBtn);
        vg.addActor(skinBtn);
        vg.addActor(langBtn);
        vg.addActor(backBtn);
        vg.center();
        setButtonActions();
        stage.addActor(vg);
    }

    private void setButtonActions() {
        soundBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                soundFlag = !soundFlag;
                soundBtn.setText(game.loc.get(soundFlag ? "soundOn" : "soundOff"));
            }
        });

        skinBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                System.out.println("Skin Button Pressed");
            }
        });

        langBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                langFlag = !langFlag;
                game.changeLang(langFlag ? new Locale("en") : new Locale("pl"));
                langBtn.setText(game.loc.get(langFlag ? "lngEnglish" : "lngPolish"));
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                // TODO: zapis opcji

                game.setScreen(new MainMenu(game));
                dispose();
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(game.WIDTH, game.HEIGHT, true);
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
