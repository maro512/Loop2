/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.net;

import java.awt.EventQueue;
import java.io.FileInputStream;
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
        /* System.setProperty("javax.net.ssl.keyStore", "trusted.jks");// "C:\\Users\\Piotr\\Documents\\Informatyka UJ\\Loop2\\trusted.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","loop2017");
        //System.setProperty("javax.net.ssl.trustStoreType", "jks"); // */

        ClientTest obj = new ClientTest();
        System.out.println("Klient: "+obj.name);
        try
        {
            obj.client = new Client(obj.name,obj,
                LoopServer.getSSLContext(new FileInputStream("trusted.jks"), new char[]{'l', 'o', 'o', 'p', '2', '0', '1', '7'}));
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        if ( args.length>0 )
            obj.client.setServerAddress(args[0]);
        obj.client.logIn();
        System.in.read();
        if(obj.step<3) obj.nextStep();
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
                    Thread.sleep(600);
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
