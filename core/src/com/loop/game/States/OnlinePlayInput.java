package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;
import com.loop.game.Net.RatingEntry;
import java.util.Arrays;

/**
 * Created by Piotr on 06.06.2017.
 */

public class OnlinePlayInput extends BasicScreen
{
    private TextField playerField;
    private TextButton playBtn, searchBtn;
    private Label playerInfoLbl;
    private TextButton backBtn;
    private boolean avaiting=false;
    String playerName=null;

    public OnlinePlayInput(final LoopGame game)
    {
        super(game);
        playBtn = new TextButton(game.loc.get("startGame"), getSkin());
        searchBtn = new TextButton(game.loc.get("search"), getSkin());
        backBtn = new TextButton(game.loc.get("back"), getSkin());

        playerInfoLbl = new Label(game.loc.get("anyplayer"), getSkin());
        playerInfoLbl.setAlignment(Align.center);

        playerField = new TextField("", getSkin());
        playerField.setMessageText(game.loc.get("username"));

        fillStage();
        Gdx.input.setInputProcessor(getStage());
        //Gdx.input.setCatchBackKey(true);
    }

    private void fillStage()
    {
        Table vg = new Table();
        vg.setFillParent(true);
        vg.row().fillY();
        vg.row(); vg.add(playerField).padBottom(BUTTON_PAD).width(getStage().getWidth()*.7f);
        vg.row(); vg.add(searchBtn).padBottom(3*BUTTON_PAD);
        vg.row(); vg.add(playerInfoLbl).padBottom(3*BUTTON_PAD).width(getStage().getWidth()*.9f);
        // Tu pewnie będzie wyświetlanie informacji o graczu
        vg.row(); vg.add(playBtn).padBottom(BUTTON_PAD).width(getStage().getWidth()*.4f);
        vg.row(); vg.add(backBtn).padBottom(BUTTON_PAD).width(getStage().getWidth()*.4f);
        vg.row().fillY();
        setButtonActions();
        getStage().addActor(vg);
    }

    private void hideKeyboard()
    {
        playerField.getOnscreenKeyboard().show(false);
    }

    private void setButtonActions() {
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                hideKeyboard();
                if (avaiting)
                {
                    System.out.println("Czekam na odpwoiedź!");
                    return;
                }
                if(getClient().isServerConnected())
                {
                    avaiting=true;
                    playBtn.setDisabled(true);
                    playBtn.setTouchable(Touchable.disabled);
                    getClient().startGame(playerName);
                }
                else System.out.println("Nie jesteś zalogowany!");
            }
        });

        searchBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                hideKeyboard();
                if (avaiting) return;
                if(getClient().isServerConnected())
                {
                    String name= playerField.getText().trim();
                    if (name.isEmpty()) return;
                    getClient().askForPlayer(name);
                    playerInfoLbl.setText("...");
                }
                else System.out.println("Nie jesteś zalogowany!");
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (avaiting) return;
                hideKeyboard();
                backToMainMenu();
            }
        });
    }

    private void enablePlay()
    {
        avaiting=false;
        playBtn.setDisabled(false);
        playBtn.setTouchable(Touchable.enabled);
    }

    @Override
    public void dispose()
    {
        hideKeyboard();
        super.dispose();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        if (command[0].equals(Client.ERROR))
        {
            Gdx.app.postRunnable(new Runnable()
            {
                @Override
                public void run()
                {
                    getClient().close();
                    backToMainMenu();
                }
            });
        }
        if (command[0].equals(Client.CMD_CLEAR))
        {
            //TODO komunikat o niepowodzeniue połączenia?
            Gdx.app.postRunnable(new Runnable()
            {
                @Override
                public void run()
                {
                    enablePlay();
                }
            });
            return true;
        }
        if (command[0].equals(Client.CMD_USERQUERY))
        {
            if (command.length==1)
            {
                System.out.println("Nie znaleziono takiego gracza!");
                playerName=null;
                Gdx.app.postRunnable(new Runnable(){
                    @Override
                    public void run()
                    {
                        playerInfoLbl.setText(getApp().loc.get("anyplayer"));
                    }
                });
            }
            else
            {
                final RatingEntry player = RatingEntry.decode(command);
                final boolean isOnline = new Boolean(command[5]);
                playerName = player.getName();
                Gdx.app.postRunnable(new Runnable(){
                    @Override
                    public void run()
                    {
                        playerInfoLbl.setText(player+(isOnline ? " online" : " offline"));
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public void connectionDown(boolean server)
    {
        if (server)
            Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run()
            {
                backToMainMenu();
            }});
    }

    @Override
    public void done(boolean success)
    {
        if (success && avaiting)
        {
            Gdx.app.postRunnable(new Runnable(){
                @Override
                public void run()
                {
                    getApp().setScreen(new PlayOnline(getApp()));
                    dispose();
                }});
        }
        else
            Gdx.app.postRunnable(new Runnable(){
                @Override
                public void run()
                {
                    //TODO komunikat o nieudanym połączeniu
                    enablePlay();
                }});
    }
}
