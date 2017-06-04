/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.Net;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa do testowania połączenia z serwerem LOOP i innymi graczami.
 * Created by Piotr on 2017-05-13
 */
public class ClientTest implements ConnectionListener
{
    String name = String.valueOf(new Date().hashCode()).substring(1);
    int step=0;
    Client client;
    private Boolean doneFlag = null;

    public static void main(String[] args) throws IOException
    {
        ClientTest obj = new ClientTest();
        System.out.println("Klient: "+obj.name);
        obj.client = new Client(obj);
        if ( args.length>0 )
            obj.client.setServerAddress(args[0]);
        obj.client.logIn("b","bb");
        System.in.read();
        if(obj.step<3) obj.nextStep();
        //System.in.read();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("Komunikat: "+Arrays.toString(command));
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
        else nextStep();
        return true;
    }

    @Override
    public void recieveRating(List<RatingEntry> rating)
    {
        for(RatingEntry player: rating)
            System.out.println("\t"+player);
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
        doneFlag= success;
        if (success)
        {
            System.out.print("OK ");
            nextStep();
        }
        else
        {
            System.out.println("\n\tNiepowodzenie operacji. Zamykam połączenie z serwerem.");
            client.close();
        }
    }

    // Tu ma miejsce cała zabawa w wysyłanie komunikatów.
    private void nextStep()
    {
       /* EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() { // */
            try {
                Thread.sleep(600);
                System.out.print("["+name+"]: "+step);
                switch(step)
                {
                    case 1: System.out.println(" próba rozpoczęcia gry.");
                        client.startGame(null);
                        break;
                    case 2: System.out.println(" Ruch.");
                        client.commitMove(1,1,(byte)1);
                        break;
                    case 3: System.out.println(" Ruch.");
                        client.commitMove(2,1,(byte)1);
                        break;
                    case 4: Thread.sleep(2000);
                            System.out.println(" Ruch.");
                        client.commitMove(3,1,(byte)1);
                        break;
                    case 5: System.out.println(" Ruch.");
                        client.commitMove(4,1,(byte)1);
                        break;
                    case 6: System.out.println(" Kończenie.");
                        client.commitGameEnd(true);
                        break;
                    default: System.out.println("z"); break;
                }
                step++;
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}
