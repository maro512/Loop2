/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.Net;


import com.badlogic.gdx.utils.async.AsyncTask;
import com.loop.game.GameModel.Player;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa reprezentująca połączenie z serwerem LOOP. Kontroluje także komunikację
 * z drugim graczem w ramach prowadzenia rozgrywki.
 * Created by Piotr on 2017-05-13
 * Edited by Kamil on 2017-05-21
 */
public class Client
{  
    public static final String CMD_MOVE             ="MovE";
    public static final String CMD_WIN              ="wIn";
    public static final String CMD_USERQUERY        ="fINd";
    public static final String CMD_LOGIN            ="loGIN";   // Logowanie
    public static final String CMD_REGISTER         ="NewOnE";  // Rejestracja ?
    public static final String CMD_LOGOUT           ="loGOuT";
    public static final String CMD_PLAY             ="pLAYnow"; // Żądanie rozpoczęcia gry online
    public static final String CMD_ENDDATA          ="nOmoRE";
    public static final String CMD_DATABASEQUERY    ="getDATA";
    public static final String CMD_CLEAR            ="eNdALl";  // Gwałtowny koniec gry
    public static final String CMD_GAMEEND          ="KonIEc";  // Oficjalny koniec gry
    public static final String ERROR                ="erROr";   // Coś na wypadek błędu.
    public static final String[] ERROR_COMMAND = { ERROR };
    public static final int SERVER_PORT= 7457;

    public static final String SERVER_ADDRESS = "localhost"; // Domyślny adres serwera.

    private String name;
    private ConnectionListener user;
    private Socket server;
    private PrintWriter serverIn;
    private Socket otherPlayer;
    private BlockingQueue<String> messagesToOtherPlayer;
    private PrintWriter otherPlayerOut;
    private byte state=-1;
    private Boolean colour;
    private Boolean myMove;
    private String opponentName;
    private InetSocketAddress serverAddress;
    /* Czasy oczekiwania na połączenie w milisekundach. */
    private int serverConnectionTimeout = 6000;
    private int otherPlayerConnectionTimeout = 11000;

    public Client(ConnectionListener listener)
    {
        name = "default";
        user = listener;
        messagesToOtherPlayer =new LinkedBlockingQueue<String>();
        colour = null;
        myMove = null;
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                serverAddress= new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT);
            }
        }).start();
    }

    public void setConnectionListener(ConnectionListener listener)
    {
        if(listener==null) throw new NullPointerException("Connection listener is null!");
        user=listener;
    }

    /** Uruchamia połączenie z serwerem. Komunikacja dzieje się w osobnym wątku. */
    public void logIn(final String login, final String pass)
    {
        if(state!= -1) throw new IllegalStateException("Connection is already open?");
        state=1;
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                serverReadingLoop(login, pass);
            }
        }).start();
    }
    
    /** Gettery i Settery : **/
    public String  getName(){
        return name;
    }

    public Boolean isMyMove(){
        return myMove;
    }

    public Boolean getColour(){
        return colour;
    }

    public Player[] getPlayerTable()
    {
        Player[] players=new Player[2];
        players[colour ? 1 : 0 ]=new Player(name, colour ? 1 : 0);
        players[colour ? 0 : 1 ]=new Player(opponentName, colour ? 0 : 1);
        return players;
    }
    
    public String  getOpponentName()
    {
        if (!isOtherPlayerConnected()) throw new IllegalStateException("No connection with other player!");
        return opponentName;
    }

    /** Pętla komunikacji z serwerem (głównie odbiera dane) */
    private void serverReadingLoop(String login, String pass)
    {
        try {
            Scanner serverOut = connectToServer(login, pass);
            if(state==0) user.done(true);
            else
            {
                state=-1;
                server.close();
                user.done(false);
            }
            //System.out.println(server.isClosed()+" "+server.isConnected()+" "+serverOut.hasNextLine());
            while( isServerConnected() && serverOut.hasNextLine() )
            {
                String[] command= serverOut.nextLine().split(" ");
                state=0;
                if (command[0].equals(CMD_DATABASEQUERY))
                {
                    readRatingAndSendToUser(command, serverOut);
                    continue;
                }
                if(!dispath(command))
                    serverIn.println(ERROR+" "+command[0]);
            }
        }
        catch (IOException ex)
        {
            user.connectionDown(true);
            ex.printStackTrace();
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
            state=-1;
        }
    }

    private void readRatingAndSendToUser(String[] firstLine, final Scanner input)
    {
        List<RatingEntry> list = new ArrayList<RatingEntry>();
        while(firstLine[0].equals(CMD_DATABASEQUERY))
        {
            list.add(RatingEntry.decode(firstLine));
            firstLine=input.nextLine().split(" ");
        }
        user.recieveRating(list);
    }

    private Scanner connectToServer(String name, String pass) throws IOException
    {
        server=new Socket();
        System.out.print("Connecting to server...");
        server.connect(serverAddress, serverConnectionTimeout);
        System.out.println("complete!");
        serverIn = new PrintWriter(server.getOutputStream(), true);
        serverIn.println(CMD_LOGIN+" "+name+"//"+pass);
        Scanner serverOut = new Scanner(server.getInputStream());

        System.out.print("Waiting for confirmation... "); //Może tu też jakiś szybki czas na odpowiedź?
        if (serverOut.hasNext() && serverOut.nextLine().startsWith(CMD_LOGIN))
        {
            this.name=name;
            state= 0;
        }
        return serverOut;
    }

    /** Wysyła do drugiego gracza informację o ruchu. */
    public void commitMove(int x, int y, byte type) // Tu parametr typu Tile, albo jakiś inny kod ruchu.
    {
        if(!isOtherPlayerConnected()) throw new IllegalStateException("No connection with other player!");
        myMove=false;
        messagesToOtherPlayer.offer(CMD_MOVE+" "+x+" "+y+" "+type);
    }

    /** Wysyła do drugiego gracza informację o zakończeniu gry. */
    public void commitGameEnd(boolean IWon)
    {
        if(!isOtherPlayerConnected()) throw new IllegalStateException("Not connected to other player!");
        messagesToOtherPlayer.offer(IWon ? CMD_GAMEEND+" "+CMD_WIN : CMD_GAMEEND);
    }

    /** Wysyła zapytanie o najlepszych count graczy i własną pozycję w rankingu. */
    public void askForRating(int count)
    {
        serverIn.println(CMD_DATABASEQUERY+" "+name+" "+count);
    }

    /** Wysyła zapytanie o dostępność gracza (i pozcyję w rankingu). */
    public void askForPlayer(String username)
    {
        serverIn.println(CMD_USERQUERY+" "+username);
    }

    /** Zwraca, czy jest aktywne połączenie z serwerem. */
    public boolean isServerConnected()
    {
        return server!=null && server.isConnected() && !server.isClosed();
    }

    /** Zwraca, czy jest aktywne połączenie z innym graczem. */
    public boolean isOtherPlayerConnected()
    {
        return otherPlayer!=null && otherPlayer.isConnected() && !otherPlayer.isClosed();
    }

    /** Łączy się z serwerem aby zainicjować połączenie z innym graczem. */
    public void startGame(final String otherPlayerName)
    {
        if(!isServerConnected()) throw new IllegalStateException("Server connection not established!");
        if (state!=0 || isOtherPlayerConnected())
        {
            return;// throw new IllegalStateException("No other players on server"); // To powinien być wyjątek, ale psułby mi testy
        }
        state=1;
        colour = true;
        myMove = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                doStartGame(otherPlayerName);
            }
        });
        t.start();
    }

    /** Inicjowanie połączenia z innym graczem.
     * Obsługuje pętlę odbierającą informacje od tego gracza. */
    private void doStartGame(String otherPlayerName)
    {
        try {
            int port= SERVER_PORT + new Random().nextInt(8)+1; // Na tym samym urządzeniu musimy mieć różne porty!
            ServerSocket awaiting = new ServerSocket(port);
            awaiting.setSoTimeout(otherPlayerConnectionTimeout);

            System.out.println("Wysłano rządanie do serwera...");
            if (otherPlayerName==null) serverIn.println(CMD_PLAY+" "+port); // Wyślij żądanie do serwera
            else serverIn.println(CMD_PLAY+" "+port+" "+otherPlayerName);

            System.out.print("Waiting for other player...");
            otherPlayer= awaiting.accept(); // Czekaj na odpowiedź
            System.out.print("connected!\nWaiting for response... ");
            Scanner in = new Scanner(otherPlayer.getInputStream());
            String[] answer = ERROR_COMMAND;
            if (in.hasNextLine())
            {
                answer = in.nextLine().split(" ");
            }

            if (answer[0].equals(CMD_PLAY) && answer.length>1)
            {
                System.out.println("Accepted :)");
                otherPlayerOut= new PrintWriter(otherPlayer.getOutputStream(),true);
                opponentName= answer[1];
                user.processCommand(answer);
                user.done(true);
                otherPlayerListener(); // Uruchom nasłuchiwanie ruchów
            }
            else
            {
                System.out.println("Aborted! :(");
                serverIn.println(CMD_CLEAR);
                state = 0;
                user.done(false); // Zgłoś niepowodzenie operacji rozpoczęcia gry
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            user.done(false); // Błąd
        } finally {
            state = 0;
        }
    }

    //Zamyka połączenie z drugim graczem.
    private void disconnectOther()
    {
        if (otherPlayer==null || otherPlayer.isClosed()) return;
        try{
                otherPlayer.close();
        } catch (IOException ex) {
        }
        otherPlayerOut=null;
        otherPlayer=null;
        state = 0;
        //user.connectionDown(false);
    }

    public void clearConnectionState()
    {
        if (isOtherPlayerConnected())
            messagesToOtherPlayer.offer(CMD_CLEAR);
        else
            serverIn.println(CMD_CLEAR);
    }

    private boolean dispath(String[] args)
    {
        if(args[0].equals(CMD_PLAY))
        {
            return acceptPlayRequest(args);
        }
        else if (args[0].equals(CMD_GAMEEND))
        {
            //serverIn.println(CMD_CLEAR);
            messagesToOtherPlayer.offer(CMD_CLEAR);
        }
        return user.processCommand(args);
    }

    /** Nasłuchiwanie w pętli odpowiedzi drugiego gracza. */
    private void otherPlayerListener()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                otherPlayerSender();
            }
        }).start(); // Uruchom wątek wysyłania
        try {
            System.out.println("Listener has started");
            Scanner in = new Scanner( otherPlayer.getInputStream() );
            while(in.hasNextLine())
            {
                String cmd0=in.nextLine();
                String[] cmd = cmd0.split(" ");
                myMove=false;
                if (! user.processCommand(cmd)
                    || cmd[0].equals(CMD_CLEAR)) break;
                if (cmd[0].equals(CMD_MOVE)) myMove=true;
                if (cmd[0].equals(CMD_GAMEEND))
                {
                    serverIn.println(cmd0); // Przekaż informację o zakończeniu gry do serwera.
                    //break;
                }
            }
        } catch (IOException ex)
        {
        }
        myMove=false;
        if (!messagesToOtherPlayer.offer(CMD_CLEAR))
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, "Nie wstawiło się.");
        System.out.println("Połączenie z drugim graczem zamknięte.");
        state = 0;
    }

    /** Wysyłanie kolejnych informacji do drugiego gracza. */
    private void otherPlayerSender()
    {
        try {
            while(true)
            {
                String cmd = messagesToOtherPlayer.take();
                otherPlayerOut.println(cmd);
                if (cmd.startsWith(CMD_CLEAR)) break;
            }
            serverIn.println(CMD_CLEAR); // Zgłoś serwerowi koniec połączenia.
            messagesToOtherPlayer.clear();
        } catch (Exception ex) {  }
        finally
        {
            disconnectOther();
            user.connectionDown(false);
            state=0;
        }
        System.out.println("Zamknięcie wątku wychodzącego.");
    }

    /** Nawiązywanie połączenia z drugim graczem w odpowiedzi na jego żądanie. */
    private boolean acceptPlayRequest(String[] args)
    {
        InetSocketAddress address = new InetSocketAddress(args[1],Integer.parseInt(args[2]));
        otherPlayer=new Socket();
        try {
            otherPlayer.connect(address,5000);
            otherPlayerOut= new PrintWriter(otherPlayer.getOutputStream(),true);
            if (state==0 && args.length>3 && user.processCommand(args))
            {
                otherPlayerOut.println(CMD_PLAY+" "+name);
                colour = false;
                myMove = false;
                opponentName=args[3];
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
                otherPlayerOut.println(CMD_CLEAR); // Odrzuć "zaproszenie"
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
        } catch (IOException ex)
        {
        }
        server = otherPlayer = null;
        serverIn = null;
        otherPlayerOut = null;
        state = -1;
    }

    public void setServerAddress(String address)
    {
        if(state!= -1) throw new IllegalStateException("Connection is already open?");
        serverAddress = new InetSocketAddress(address, SERVER_PORT);
    }

    public void setServerConnectionTimeout(int timeout)
    {
        this.serverConnectionTimeout = timeout;
    }

    public void setOtherPlayerConnectionTimeout(int timeout)
    {
        this.otherPlayerConnectionTimeout = timeout;
    }
}
