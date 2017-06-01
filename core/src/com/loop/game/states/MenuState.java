package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.loop.game.LoopGame;

/**
 * Created by marek on 15.04.17.
 */

public class MenuState extends State {
    private Texture playBtn;


    public MenuState(GameStateManager gsm) {
        super(gsm);
        playBtn = new Texture("play.png");
        cam.setToOrtho(false, LoopGame.WIDTH, LoopGame.HEIGHT);
    }

    @Override
    public void handleInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                gsm.set(new PlayState(gsm));


                return true;
            }

        });
    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void dispose() {
        playBtn.dispose();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(playBtn, (LoopGame.WIDTH / 2) - (playBtn.getWidth() / 2), (LoopGame.HEIGHT / 2) - (playBtn.getHeight() / 2));
        sb.end();
    }
}
