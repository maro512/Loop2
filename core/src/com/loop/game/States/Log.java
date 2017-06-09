package com.loop.game.States;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.loop.game.LoopGame;
import com.loop.game.Net.Client;

import java.util.Arrays;

/**
 * Created by tobi on 4/28/17.
 */

public class Log extends BasicScreen
{
    private TextField userField, passField;
    private TextButton logBtn;
    private TextButton backBtn;

    public Log(final LoopGame game)
    {
        super(game);
        this.logBtn = new TextButton(getString("login"), game.skin);
        this.backBtn = new TextButton(getString("back"), game.skin);

        userField = new TextField("", game.skin);
        passField = new TextField("", game.skin);

        userField.setMessageText(getString("username"));
        passField.setMessageText(getString("password"));
        passField.setPasswordCharacter('*');
        passField.setPasswordMode(true);
        fillStage();
        Gdx.input.setInputProcessor(getStage());
    }

    private void fillStage()
    {
        Table vg = new Table();
        vg.setFillParent(true);
        vg.row().fillY();
        vg.row(); vg.add(userField).padBottom(BUTTON_PAD).width(getStage().getWidth()*.7f);
        vg.row(); vg.add(passField).padBottom(BUTTON_PAD).width(getStage().getWidth()*.7f);
        vg.row(); vg.add(logBtn).padBottom(BUTTON_PAD).width(getStage().getWidth()*.4f);
        vg.row(); vg.add(backBtn).padBottom(BUTTON_PAD).width(getStage().getWidth()*.4f);
        vg.row().fillY();
        setButtonActions();
        getStage().addActor(vg);
    }

    private void setButtonActions() {
        logBtn.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                //hideKeyboard();
                String name = userField.getText();
                String pass = passField.getText();
                if (name.isEmpty() || pass.isEmpty()) return;
                if(getClient().isServerConnected())
                {
                    //TODO: komunikat od LibGDX jakieś okienko czy coś ?
                    System.out.println("Już zalogowano");
                }
                else
                    getClient().logIn(name,pass);
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //hideKeyboard();
                backToMainMenu();
            }
        });
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("Komunikat: "+ Arrays.toString(command));
        if (command[0].equals(Client.ERROR))
        {
            Gdx.app.postRunnable(new Runnable()
            {
                @Override
                public void run()
                {
                    getClient().close();
                }
            });
        }
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
        if (success)
        {
            Gdx.app.postRunnable(new Runnable(){
                @Override
                public void run()
                {
                    backToMainMenu();
                }});
        }
        else
        {
            System.out.println("Nieudane logowanie!");
        }
    }
}
