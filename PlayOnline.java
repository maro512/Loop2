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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Player;
import com.loop.game.GameModel.Tile;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;
import com.loop.game.Widgets.BoardWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kamil
 */

public class PlayOnline implements Screen {
    private LoopGame loopGame;
    private Game game;
    private Stage stage;
    private final int BUTTONS_AMOUNT = 6;
    private Map<Button, Byte> buttons;
    private Label[] playersLabels;
    private Label.LabelStyle activeStyle;
    private Label.LabelStyle passiveStyle;
    private BoardWidget bv;
    private Table table;
    private ClickListener buttonClick = new ClickListener(){
        @Override
        public void clicked(InputEvent e, float x, float y){
            if(loopGame.client.isMyMove()) {//TODO: dobre działanie ?
                makeMove(buttons.get(e.getListenerActor()));
                Cell tmp = game.getSelected();
                loopGame.client.commitMove(tmp.getX(),tmp.getY(),tmp.getType());
            }
            else if(!loopGame.client.isMyMove() && !loopGame.client.isMoveCommited()){
                //makeMove(loopGame.client.getX(),loopGame.client.getY(),loopGame.client.getType()); //TODO: potrzebuję czegoś takiego
                loopGame.client.otherPlayerMoveCommited();
            }
        }
    };

    public PlayOnline(final LoopGame loopGame) {
        loopGame.client.startGame(null);
        while(!loopGame.client.isOtherPlayerConnected());
        Player[] players = {new Player(loopGame.client.getName(),0),new Player(loopGame.client.getOpponentName(),1)};
        this.loopGame = loopGame;
        this.game = new Game(players);
        this.stage = new Stage(new ScreenViewport(), loopGame.batch);
        this.buttons = new HashMap<Button, Byte>();
        this.playersLabels = new Label[2];
        this.activeStyle = new Label.LabelStyle(loopGame.font, new Color(240/255f, 204/255f, 0, 1));
        this.passiveStyle = new Label.LabelStyle(loopGame.font, new Color(112/255f, 101/255f, 34/255f, 1));
        this.bv = new BoardWidget(loopGame.skin, game, this);
        game.pickFirstPlayer();
        makeButtons();
        fillStage();
        Gdx.input.setInputProcessor(stage);
        //Gdx.input.setCatchBackKey(true);
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
        table = new Table(loopGame.skin);
        table.setBackground("bg_black");
        table.setTouchable(Touchable.enabled);
        table.setFillParent(true);
        table.setDebug(true);
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
        loopGame.client.commitGameEnd(game.getCrrPlayer().getName().equals(loopGame.client.getName()));
        table.clearChildren();
        table.add(new Label(game.getCrrPlayer().getName() + " "
                + loopGame.loc.get("won"), loopGame.skin)).expandX();
        table.row().fillX().expandY();
        table.add(bv).fill();
        table.row().expandX();

        ClickListener backListener = new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                loopGame.setScreen(new MainMenu(loopGame));
                dispose();
            }
        };

        TextButton backButton = new TextButton(loopGame.loc.get("back"), loopGame.skin);
        backButton.addListener(backListener);
        table.add(backButton);
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





/*package com.loop.game.States;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.loop.game.LoopGame;
import com.loop.game.Widgets.BoardWidget;
import com.loop.game.Net.*;
import java.awt.EventQueue;
import java.util.*;
import java.util.List;
import com.loop.game.GameModel.Game;
import com.loop.game.GameModel.Cell;
import com.loop.game.GameModel.Tile;

/**
 * Created by Kamil on 2017-05-25.


public class PlayOnline implements Screen, ConnectionListener {
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
    private Label.LabelStyle activeStyle;
    private Label.LabelStyle passiveStyle;
    private BoardWidget bv;
//SERVER
    private Client client;
    private Boolean gameEnded;
    private ClickListener buttonClick = new ClickListener(){
        @Override
        public void clicked(InputEvent e, float x, float y){
            if (((Button) e.getTarget()).isDisabled()) return;
            chosenType = buttons.get(e.getListenerActor());
            makeMove();
        }
    };

    public PlayOnline(final LoopGame loopGame, String[] data) {
        this.loopGame = loopGame;
        client = new Client(this);
        //while(!client.isServerConnected())
            logOnServer(data);//TODO: Wyswietlanie okna do logowania

        this.game = new Game();
        this.stage = new Stage(new ScreenViewport(), loopGame.batch);
        this.buttons = new HashMap<Button, Byte>();
        this.playersLabels = new Label[2];
        this.chosenX = 0;
        this.chosenY = 0;
        this.activeStyle = new Label.LabelStyle(loopGame.font, new Color(240/255f, 204/255f, 0, 1));
        this.passiveStyle = new Label.LabelStyle(loopGame.font, new Color(112/255f, 101/255f, 34/255f, 1));
        for (int i=0; i<data.length; ++i) {
            playersLabels[i] = new Label(data[i], loopGame.skin);
        }
        makeButtons();
        fillStage();
        Gdx.input.setInputProcessor(stage);
    }

    private String[] logOnServer(String []data){
        client.logIn(data[0],data[1]);
        client.startGame(null);
        System.out.println("Gracz "+client.getName()+" próbuje rozpocząć grę...");
        String opponentName = client.getOpponentName();
        String[] output = {data[0],opponentName};
        return output;

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
    public boolean processCommand(String[] command)
    {
        System.out.println("Komunikat: "+ Arrays.toString(command));
        if (command[0].equals(Client.CMD_CLEAR))
        {
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    client.close();
                }
            });
        }
        //else playGame();
        return true;
    }

    @Override
    public void connectionDown(boolean server)
    {
        System.out.println(" Przerwano połączenie z "+ (server ? "serwerem!" : "graczem!"));
        //client.close();
    }

    @Override
    public void done(boolean success)
    {
        //doneGame= success;
        if (success)
        {
            System.out.print("OK ");
            //playGame();
        }
        else
        {
            System.out.println("\n\tNiepowodzenie operacji. Zamykam połaczenie z serwerem.");
            client.close();
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
*/