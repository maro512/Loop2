package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Player;
import com.loop.game.GameModel.Tile;
import com.loop.game.LoopGame;
import com.loop.game.Widgets.BoardWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tobi on 5/23/17.
 */

public class Play implements Screen {
    private final int BUTTONS_AMOUNT = 6;
    int chosenX;
    int chosenY;
    byte chosenType;
    private LoopGame loopGame;
    private Game game;
    private Stage stage;
    private Map<Button, Byte> buttons;
    private Label[] playersLabels;
    private int currentPlayer;
    // private Pixmap pm;
    private Label.LabelStyle activeStyle;
    private Label.LabelStyle passiveStyle;
    private BoardWidget bv;
    private ClickListener buttonClick = new ClickListener(){
        @Override
        public void clicked(InputEvent e, float x, float y){
            if (((Button) e.getTarget()).isDisabled()) return;
            chosenType = buttons.get(e.getListenerActor());
            makeMove();
        }
    };

    /*
        TODO:
          - listenery do buttonow
          - render planszy
          - interaktywnosc planszy
          - wyroznienie aktualnego gracza - ready
          - tla do tabel
    */

    public Play(final LoopGame loopGame, Player[] players) {
        this.loopGame = loopGame;
        this.game = new Game(players);
        this.stage = new Stage(new ScreenViewport(), loopGame.batch);
        this.buttons = new HashMap<Button, Byte>();
        this.playersLabels = new Label[2];
        this.chosenX = 0;
        this.chosenY = 0;
        this.activeStyle = new Label.LabelStyle(loopGame.font, new Color(240/255f, 204/255f, 0, 1));
        this.passiveStyle = new Label.LabelStyle(loopGame.font, new Color(112/255f, 101/255f, 34/255f, 1));
        this.bv = new BoardWidget(loopGame.skin, game, this);

        // this.pm = new Pixmap(1, 1, Pixmap.Format.RGB888);
        // pm.setColor(24, 24, 24, 1);
        // pm.fill();

        for (int i=0; i<players.length; ++i) {
            playersLabels[i] = new Label(players[i].getName(), loopGame.skin);
        }
        makeButtons();
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private void makeButtons () {
        for (Byte b : Tile.ALL_TYPES) {
            Button.ButtonStyle bs = new Button.ButtonStyle();
            String s = "tile_" + b + ".png";
            String ss = "tile_" + b + "s.png";
            bs.up = new TextureRegionDrawable(new TextureRegion(new Texture(s)));
            bs.down = bs.up;
            bs.disabled = new TextureRegionDrawable(new TextureRegion(new Texture(ss)));
            Button button = new Button(bs);
            buttons.put(button, b);
            button.addListener(buttonClick);
        }
    }

    private void fillStage() {
        Table table = new Table(loopGame.skin);
        table.setBackground("bg_black");
        table.setTouchable(Touchable.enabled);
        table.setFillParent(true);
        // table.setDebug(true);
        table.add(playersLabels[0]).colspan((int)Math.floor(BUTTONS_AMOUNT*.5)).expandX().height(30f);
        table.add(playersLabels[1]).colspan((int)Math.ceil(BUTTONS_AMOUNT*.5)).expandX().height(30f);
        table.row().fillX().expandY();
        table.add(bv).colspan(BUTTONS_AMOUNT).fill();
        table.row();

        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            table.add(entry.getKey()).colspan(1).height(70f).width(70f);
        }

        disableAllButtons();

        stage.addActor(table);

        highlightCurrentPlayer();
    }

    private void highlightCurrentPlayer () {
        int curr = game.getCurrentPlayerNumber();

        playersLabels[curr].setStyle(activeStyle);
        playersLabels[curr^1].setStyle(passiveStyle);
    }

    public void disableAllButtons() {
        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            entry.getKey().setDisabled(true);
        }
    }

    private void makeMove () {
        chosenX = game.getSelected().getX();
        chosenY = game.getSelected().getY();
        game.makeMove(chosenX, chosenY, chosenType);
        highlightCurrentPlayer();
        game.setSelected(null);
        disableAllButtons();
    }

    public void updateMenu (Cell selected) {
        List<Byte> possibleMoves = game.getPossibleMoves(selected.getX(), selected.getY());

        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            if (possibleMoves.contains(entry.getValue())) {
                entry.getKey().setDisabled(false);
            } else {
                entry.getKey().setDisabled(true);
            }
        }
    }



    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
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
