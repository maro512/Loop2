package com.loop.game.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
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
 * Created by Kamil
 */

public class PlayOnline extends BasicScreen implements PlayScreen
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
            if(getClient().isMyMove())
            {
                byte type = buttons.get(e.getListenerActor());
                makeMoveLocal(type);
            }
        }
    };

    public PlayOnline(final LoopGame loopGame) {
        super(loopGame);
        Player[] players = getClient().getPlayerTable();
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
        //game.pickFirstPlayer(); // Nie możemy losować kto zaczyna!

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

    private void makeMoveLocal(byte type)
    {
        Cell tmp = game.getSelected();
        game.makeMove(type);
        getClient().commitMove(tmp.getX(),tmp.getY(),type);
        if (game.isTerminated())
            getClient().commitGameEnd(game.getCrrPlayer()==game.whoWon());
        updateInterfaceAfterMove();
    }

    private void makeMoveRemote(String[] data)
    {
        game.setSelected(game.getCell(parseVector(data)));
        game.makeMove(Byte.valueOf(data[3]));
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run()
            {
                updateInterfaceAfterMove();
            }});
    }

    private void updateInterfaceAfterMove()
    {
        if (game.isTerminated()) showWinScreen();
        else
        {
            highlightCurrentPlayer();
            disableAllButtons();
        }
    }

    public void updateMenu(Cell selected) {
        if (!getClient().isMyMove()) return;
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
                                    + getApp().loc.get("won"), getApp().skin)).expandX();
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

        TextButton backButton = new TextButton(getApp().loc.get("back"), getApp().skin);
        backButton.addListener(backListener);
        table.add(backButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(!getClient().isMyMove()) game.setSelected(null);
        getStage().act(delta);
        getStage().draw();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("PlayOnline: "+ Arrays.toString(command));
        if (command[0].equals(Client.CMD_MOVE))
        {
            try{
                makeMoveRemote(command);
            } catch (Exception ex) { return false; }
            return true;
        }
        if (command[0].equals(Client.CMD_GAMEEND))
            return game.isTerminated();
        if (command[0].equals(Client.CMD_CLEAR))
        {
            if (! game.isTerminated())
                System.err.println("Połącznie nieoczekiwanie przerwane!");
            else return true;
        }
        return false;
    }

    private static Vector2 parseVector(String[] moveArgs)
    {
        return new Vector2(Integer.parseInt(moveArgs[1]),Integer.parseInt(moveArgs[2]));
    }

    @Override
    public void connectionDown(boolean server)
    {
        if(server)
        {
            System.err.println("Przerwano połączenie z serwerem!");
            Gdx.app.postRunnable(new Runnable(){
                @Override
                public void run()
                {
                    backToMainMenu();
                }});
        }
    }

}