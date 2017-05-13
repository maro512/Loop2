/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.net;

import static com.loop.game.net.LoopServer.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa reprezentująca połąaczenie z serwerem LOOP. Kontroluje także komunikację z drugim graczem w ramach
 * prowadzenia rozgrywki.
 * @author Piotr
 */
public class Client implements Runnable
{  
    private String name;
    private Socket server;
    private PrintWriter serverIn;
    private PrintWriter otherPlayerOut;
    private Socket otherPlayer;
    private ConnectionListener user;
    private BlockingQueue<String> queue;
    private byte state=-1;
    
    public Client(String name, ConnectionListener listener)
    {
        this.name=name;
        user = listener;
        queue=new LinkedBlockingQueue<String>();
    }
    
    /** Uruchamia połaczenie z serwerem. Komunikacja dzieje się w osobnym wątku. */
    public void logIn(/* Jakieś dane uwierzytalniania? */)
    {
        //if(state!= 0) throw new IllegalStateException("Net interface not ready!");
        state=1;
        Thread t = new Thread(this);
        t.start();
    }
       
    @Override
    public void run()
    {
        state=1;
        server=new Socket();
        System.out.print("Connecting to server...");
        try {
            server.connect(SERVER_ADDR, 10000);
            System.out.println("complete!");
            serverIn = new PrintWriter(server.getOutputStream(),true);
            serverIn.println(LoopServer.CMD_LOGIN+" "+name);
            Scanner serverOut = new Scanner(server.getInputStream());
            System.out.println("Waiting for confirmation...");
            if (serverOut.nextLine().startsWith(LoopServer.CMD_LOGIN)) state= 0;
            if(state==0) user.done(true);
            else
            {
                state=-1;
                server.close();
                user.done(false);
            }
            //System.out.println(server.isClosed()+" "+server.isConnected()+" "+serverOut.hasNextLine());
            while(isServerConnected() && serverOut.hasNextLine())
            {
                String[] command= serverOut.nextLine().split(" ");
                state=0;
                if(!dispath(command))
                    serverIn.println(ERROR);
            }
            
        }catch (IOException ex)
        {
            user.connectionDown(true);
        }
        finally
        {
            System.out.println("Połączenie z serwerem zamknięte.");
            try {
                if (server!=null) server.close();
            } catch (IOException ex)
            {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    public void commitMove(String description) // Tu parametr typu Tile, albo jakiś inny kod ruchu.
    {
        if(!isOtherPlayerConnected()) throw new IllegalStateException("Net interface not ready!");
        queue.offer(CMD_MOVE+" "+description);
    }
    
    public void commitGameEnd(String args)
    {
        if(!isOtherPlayerConnected()) throw new IllegalStateException("Net interface not ready!");
        if (args==null) queue.offer(CMD_GAMEEND);
        else queue.offer(CMD_GAMEEND+" "+args);
    }
    
    public boolean isServerConnected()
    {
        return server!=null && server.isConnected() && !server.isClosed();
    }
    
    public boolean isOtherPlayerConnected()
    {
        return otherPlayer!=null && otherPlayer.isConnected() && !otherPlayer.isClosed();
    }
    
    public void startGame(final String otherPlayerName)
    {
        if(!isServerConnected()) throw new IllegalStateException("Net interface not ready!");
        if (isOtherPlayerConnected()) return; // To powinien być wyjątek, ale psułby mi testy
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                doStartGame(otherPlayerName);
            }
        });
        t.start();
    }
    
    private void doStartGame(String otherPlayerName)
    {
        try {
            int port=SERVER_PORT+new Random().nextInt(8)+1; // Na tym samym urządzeniu musimy mieć różne porty!
            ServerSocket awaiting = new ServerSocket(port);
            awaiting.setSoTimeout(10000);
            
            System.out.println("Wysłano rządanie do serwera...");
            if (otherPlayerName==null) serverIn.println(LoopServer.CMD_PLAY+" "+port); // Wyślij rządanie do serwera
            else serverIn.println(LoopServer.CMD_PLAY+" "+port+" "+otherPlayerName);
            
            System.out.print("Waiting for other player...");
            otherPlayer= awaiting.accept(); // Czekaj na odpowiedź
            System.out.print("connected!\nWaiting for response...");
            
            Scanner in = new Scanner(otherPlayer.getInputStream());
            String[] answer = ERROR_COMMAND;
            if (in.hasNextLine())
            {
                answer = in.nextLine().split(" ");
                state=0;
                System.out.println(" got "+Arrays.toString(answer));
            }
            if (answer[0].equals(CMD_PLAY))
            {
                otherPlayerOut= new PrintWriter(otherPlayer.getOutputStream(),true);
                user.done(true);
                otherPlayerListener(); // Uruchom nasłuchiwanie ruchów
            }
            else
            {
                //user.connectionError(false);
                user.done(false);
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
            user.done(false);
        }
    }
    
    private void disconnectOther() 
    {
        if (otherPlayer==null) return;
        try{
                otherPlayer.close();
        } catch (IOException ex) {            
        }
        otherPlayerOut=null;
        otherPlayer=null;
        user.connectionDown(false);
    }
    
    private boolean dispath(String[] args)
    {
        if(args[0].equals(LoopServer.CMD_PLAY))
        {
            return acceptPlayRequest(args);
        }
        else
            return user.processCommand(args);
    }
    
    // Obsługuje pętlę nasłuchiwania 
    private void otherPlayerListener()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                otherPlayerSender();
            }
        }).start();
        try {
            Scanner in = new Scanner( otherPlayer.getInputStream() );
            while(in.hasNextLine())
            {
                String[] cmd = in.nextLine().split(" ");
                if (! user.processCommand(cmd)
                        || cmd[0].equals(CMD_CLEAR)) break;
            }
        } catch (IOException ex) {
            user.connectionDown(false);
        }
         try {
            queue.put(CMD_CLEAR);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void otherPlayerSender() // Obsługuje wysyłanie informacji do gracza
    {
        try {
            while(true)
            {
                String cmd=queue.take();
                otherPlayerOut.println(cmd);
                if (cmd.startsWith(CMD_GAMEEND))
                {
                    serverIn.println(cmd); break; // Poinformuj serwer o końcu gry
                }
                else if (cmd.startsWith(CMD_CLEAR)) break;
            }
            serverIn.println(CMD_CLEAR); // Zgłoś serwerowi koniec gry.
            queue.clear();
        } catch (Exception ex) { user.connectionDown(false); }
        finally
        {
            disconnectOther();
        }
        System.out.println("Zamknięcie wątku wychodzacego.");
    }
    
    private boolean acceptPlayRequest(String[] args)
    {
        InetSocketAddress address = new InetSocketAddress(args[1],Integer.parseInt(args[2]));
        otherPlayer=new Socket();
        try {
            otherPlayer.connect(address,5000);
            otherPlayerOut= new PrintWriter(otherPlayer.getOutputStream(),true);

            if (state==0 && user.processCommand(args))
            {
                otherPlayerOut.println(LoopServer.CMD_PLAY); // Tu może będą ustalane kolory graczy?
                //otherPlayerOut.flush();
                new Thread(new Runnable(){
                    @Override
                    public void run()
                    {
                        otherPlayerListener(); 
                    }
                }).start(); // Uruchom nasłuchiwanie ruchów
            }
            else
            {
                otherPlayerOut.println(LoopServer.CMD_CLEAR); // Odrzuć "zaproszenie"
                disconnectOther();
                user.done(false);
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    /** Kończy całą komunikację sieciową natychmiast. */
    public void close()
    {
        try {
            if (server!=null && !server.isClosed()) server.close();
            if (otherPlayer!=null && !otherPlayer.isClosed()) otherPlayer.close();
            
        } catch (IOException ex) {
        }
        server= otherPlayer =null;
        serverIn=null;
        otherPlayerOut=null;
        state=-1;
    }
    
    public static final String[] ERROR_COMMAND = { LoopServer.ERROR };
    public static final String CMD_MOVE     = "MovE";
        
    public static final String SERVER_ADDRESS = "localhost";
    private static final InetSocketAddress SERVER_ADDR = new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT);
}
