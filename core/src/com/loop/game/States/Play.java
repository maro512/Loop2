package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Player;
import com.loop.game.GameModel.Tile;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;
import com.loop.game.Widgets.BoardWidget;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tobi on 5/23/17.
 */

public class Play extends BasicScreen implements PlayScreen
{
    private Game game;
    private final int BUTTONS_AMOUNT = 6;
    private Map<Button, Byte> buttons;
    private Label[] playersLabels;
    private Label.LabelStyle activeStyle;
    private Label.LabelStyle passiveStyle;
    private BoardWidget bv;
    private Table table;
    private Image playerBg;
    private Image playerBg2;
    private final float SCALE = 70f;

    private ClickListener buttonClick = new ClickListener(){
        @Override
        public void clicked(InputEvent e, float x, float y){
            makeMove(buttons.get(e.getListenerActor()));
        }
    };

    public Play(final LoopGame loopGame , Player[] players) {
        super(loopGame);
        this.game = new Game(players);
        this.buttons = new HashMap<Button, Byte>();
        this.playersLabels = new Label[2];
        this.activeStyle = new Label.LabelStyle(getApp().font, new Color(240/255f, 204/255f, 0, 1));
        this.passiveStyle = new Label.LabelStyle(getApp().font, new Color(112/255f, 101/255f, 34/255f, 1));
        this.playerBg = new Image(new Texture(Gdx.files.internal("playerWhite.png")));
        playerBg.setScaling(Scaling.stretch);
        this.playerBg2 = new Image(new Texture(Gdx.files.internal("players.png")));
        playerBg2.setScaling(Scaling.stretch);
        this.bv = new BoardWidget(getApp().skin, game, this);
        game.pickFirstPlayer();

        for (int i=0; i<players.length; ++i) {
            playersLabels[i] = new Label(players[i].getName(), getApp().skin);
            playersLabels[i].setAlignment(Align.center);
        }
        makeButtons();
        fillStage();
        Gdx.input.setInputProcessor(getStage());
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
        table = new Table(getApp().skin);
        table.setBackground("bg_black");
        table.setTouchable(Touchable.enabled);
        table.setFillParent(true);
        table.setDebug(true);
        table.add(new Stack(playerBg, playersLabels[0])).colspan((int)Math.floor(BUTTONS_AMOUNT*.5)).expandX().height(SCALE);
        table.add(new Stack(playerBg2, playersLabels[1])).colspan((int)Math.ceil(BUTTONS_AMOUNT*.5)).expandX().height(SCALE);
        table.row().expand();
        table.add(bv).pad(10).colspan(BUTTONS_AMOUNT).fill();
        table.row();

        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            table.add(entry.getKey()).colspan(1).height(SCALE).width(SCALE);
        }
        disableAllButtons();
        getStage().addActor(table);
        highlightCurrentPlayer();
    }

    private void highlightCurrentPlayer () {
        int curr = game.getCrrPlayer().getNumber();

        playersLabels[curr].setStyle(activeStyle);
        playersLabels[curr^1].setStyle(passiveStyle);
    }

    public void disableAllButtons() {
        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            entry.getKey().setDisabled(true);
            entry.getKey().setTouchable(Touchable.disabled);
        }
    }

    private void makeMove (byte type) {
        game.makeMove(type);

        if (!game.isTerminated()) {
            highlightCurrentPlayer();
            game.setSelected(null);
            disableAllButtons();
        } else {
            showWinScreen();
        }
    }

    public void updateMenu(Cell selected) {
        List<Byte> possibleMoves = game.getPossibleMoves(selected.getX(), selected.getY());

        for (Map.Entry<Button, Byte> entry : buttons.entrySet()) {
            if (possibleMoves.contains(entry.getValue())) {
                entry.getKey().setDisabled(false);
                entry.getKey().setTouchable(Touchable.enabled);
            } else {
                entry.getKey().setDisabled(true);
                entry.getKey().setTouchable(Touchable.disabled);
            }
        }
    }

    private void showWinScreen() {
        table.clearChildren();
        table.add(new Label(game.whoWon().getName() + " "
                            + getString("won"), getApp().skin)).expandX();
        table.row().fillX().expandY();
        table.add(bv).pad(10).colspan(BUTTONS_AMOUNT).fill();
        table.row().expandX();

        ClickListener backListener = new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
           getApp().setScreen(new MainMenu(getApp()));
           dispose();
            }
        };

        TextButton backButton = new TextButton(getString("back"), getApp().skin);
        backButton.addListener(backListener);
        table.add(backButton);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
            backToMainMenu();
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        getStage().act(delta);
        getStage().draw();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("Play: "+ Arrays.toString(command)+" !?!");
        return command[0].equals(Client.CMD_CLEAR);
    }
}
