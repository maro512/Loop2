package com.loop.game.server;

import com.loop.game.Net.*;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kamil on 2017-05-25.
 */

public class OnlineLoopGameTest implements ConnectionListener
{

    Boolean gameEnded;
    private Client client;

    private OnlineLoopGameTest()
    {
        client=new Client(this);
        gameEnded = null;
    }

    public static void main(String[] args) throws IOException {
        Scanner scr = new Scanner(System.in);
        OnlineLoopGameTest currSession = new OnlineLoopGameTest();
        if ( args.length>0 )
            currSession.client.setServerAddress(args[0]);
        while(!currSession.client.isServerConnected())
        {
            System.out.print("Podaj login: ");//TODO: w tym miejscu wyświetlam okno z logowaniem i pobieram l i h
            String login = scr.next();
            System.out.print("Podaj hasło: ");//TODO: "gwiazdkowanie" hasła
            String pass = scr.next();
            currSession.client.logIn(login, pass);
            try{
                for (int i =0; i < 3; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.print(".");
                }
                System.out.println();
            }catch(Exception e){
                System.out.println(e);
            }
        }
        System.in.read();
        currSession.playGame();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("Komunikat: "+ Arrays.toString(command));
        if(command[0].equals(Client.CMD_GAMEEND))
        {
            gameEnded=true;
            System.out.println("\tKONIEC GRY!");
        }
        if (command[0].equals(Client.CMD_CLEAR))
            {
                gameEnded=true;
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
    public void recieveRating(List<RatingEntry> rating)
    {
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
            System.out.println("\n\tNiepowodzenie operacji. Zamykam połączenie z serwerem.");
            client.close();
        }
    }

    private void playGame(){
        try{
            //client.askForOnePlayer("testuser");
            Thread.sleep(600);
            Scanner scr = new Scanner(System.in);
            System.out.println("Gracz "+client.getName()+" próbuje rozpocząć grę...");
            System.out.println(client.isServerConnected());
            client.startGame(null);
            gameEnded = false;
            while(!gameEnded){
                if(client.isMyMove()) {
                    System.out.println("Podaj x, y, type:");
                    int x = scr.nextInt();
                    int y = scr.nextInt();
                    byte type = scr.nextByte();
                    if (type<0) client.commitGameEnd(true);
                    else client.commitMove(x, y, type);//TODO:getX,Y&&type??
                }
                else{
                    System.out.print("I'm waiting for "+client.getOpponentName()+"'s move");
                    for(int i = 0; i<5; i++){
                        Thread.sleep(1000);
                        System.out.print(".");
                    }
                    System.out.println();
                }
            }
        }catch (InterruptedException e){
            System.out.println(e);
        }
    }
}
