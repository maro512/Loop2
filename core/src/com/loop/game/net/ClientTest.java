/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.net;

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
    private Boolean doneFlag = null;
    String name = String.valueOf(new Date().hashCode()).substring(1);
    int step=0;
    Client client;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        ClientTest obj = new ClientTest();
        System.out.println("Klient: "+obj.name);
        obj.client = new Client(obj.name,obj);
        obj.client.logIn();
        System.in.read();
        if(obj.step==0) obj.nextStep();
        //System.in.read();
    }

    @Override
    public boolean processCommand(String[] command)
    {
        System.out.println("Komunikat: "+Arrays.toString(command));
        if (command[0].equals(LoopServer.CMD_CLEAR))
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
            System.out.println("\n\tNiepowodzenie operacji. Zamykam połaczenie z serwerem.");
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
                    System.out.print("["+name+"]: "+step);
                    switch(step)
                    {
                        case 1: System.out.println(" próba rozpoczęcia gry.");
                            client.startGame(null);
                            break;
                        case 2: System.out.println(" Ruch.");
                            client.commitMove(name+">"+step);
                            break;
                        case 3: System.out.println(" Ruch.");
                            client.commitMove(name+">"+step);
                            break;
                        case 4: Thread.sleep(2000);
                                System.out.println(" Ruch.");
                                client.commitMove(name+">"+step);
                            break;
                        case 5: System.out.println(" Ruch.");
                            client.commitMove(name+">"+step);
                            break;
                        case 6: System.out.println(" Kończenie.");
                            client.commitGameEnd("Goodbye!");
                            break;
                        default: System.out.println(); break;
                    }
                    step++;
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
                }
         /*   }
        }); // */
    }
    
}
