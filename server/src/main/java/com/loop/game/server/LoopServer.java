/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.server;

import com.loop.game.Net.*;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Klasa serwera gry LOOP. Nasłuchuje na porcie o numerze 7457.
 * Created by Piotr on 2017-05-13
 * Edited by Kamil on 2017-05-21
 */
//TODO: gdy serwer jest wyłączony i próbujemy łączyć się do niego 2 razy to socket nie jst zamkniety
public class LoopServer //implements Runnable
{
    private final ExecutorService executor= Executors.newCachedThreadPool();
    private List<Player_Server> database;
    private ServerSocket main;
    private Map<String,ClientInteraction> clients;
    private final DataBase dataBase;

    //public static final int CLIENT_PORT= 7459;
    private long loginTimeout= 10000;
    private int playRequestConnectionTimeout= 10000;

    public LoopServer() throws Exception
    {
        database = new LinkedList<Player_Server>();
        dataBase= new DataBase(false); // Daj true, aby ominąć bazę danych.
        clients = new ConcurrentHashMap<String,ClientInteraction>();
    }

    public static void main(String args[]) throws Exception
    {
        LoopServer server = new LoopServer();
        if (args.length>1)
        {
            server.setLoginTimeout(Long.parseLong(args[0]));
            server.setPlayRequestConnectionTimeout(Integer.parseInt(args[1]));
        }
        server.run();
    }

    private String timeLimitedReadLine(final Scanner in, long milis)
    {
        String res=null;
        FutureTask<String> task = new FutureTask<String>(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return in.nextLine();
            }
        });
        executor.execute(task);
        //new Thread(task).start();
        try
        {
            return task.get(milis,TimeUnit.MILLISECONDS);
        } catch (Exception e)
        {
            return null;
        }
    }

    //@Override
    public void run()
    {
        try {
            main = new ServerSocket(Client.SERVER_PORT);
            boolean go = true;
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            System.out.print("\tSerwer waits for new connection... ");
            //executor
            while(go)
            {
                Socket incomming= main.accept();
                System.out.println(" Connected!");
                executor.execute(new ClientInteraction(incomming));
            }
            main.close();
        } catch (IOException ex)
        {
            Logger.getLogger(LoopServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPlayRequestConnectionTimeout(int timeout)
    {
        playRequestConnectionTimeout = timeout;
    }

    public void setLoginTimeout(long timeout)
    {
        loginTimeout=timeout;
    }

    public enum State { NULL, ASK, GAME, TERMINATE, LOCKED }

    /** Wewnętrzna klasa reprezentująca połączenie z pojedyńczym klientem. */
    class ClientInteraction implements Runnable
    {
        private static final String CLOSE_POINTER= "\n";
        private String name;
        private Socket socket;
        private Scanner in; // Obsługa danych przychodzących
        private PrintWriter out; // Obsługa danych wychodzących
        private BlockingQueue<String> queue;
        private ClientInteraction otherPlayer; // Ten drugi gracz, z kórym się połączyliśmy
        private State state = State.LOCKED;
        private int clientPort;
        private boolean notWorthy = false;
        private Thread sendingThread; // To jest wątek, który trzeba potem zabić.

        public ClientInteraction(Socket socket)
        {
            this.socket=socket;
            queue=new LinkedBlockingQueue<String>();
        }

        @Override
        public void run() // Pętla komunikacji z pojedynczym klientem
        {
            try {
                if ( introduceClient() ) return; // Błąd logowania.
                while(in.hasNextLine() ) // Przetwarzanie w pętli zapytań klienta.
                {
                    String[] command = in.nextLine().split(" ");
                    System.out.println("("+name+"): "+Arrays.toString(command));

                    if (command[0].equals(Client.CMD_PLAY)) cmdPlay(command);
                    else if (command[0].equals(Client.CMD_CLEAR)) cmdClear();
                    else if (command[0].equals(Client.CMD_GAMEEND)) cmdGameEnd(command);
                    // Tu można podododawać inne komendy, które serwer może obsługiwać.
                    else if(command[0].equals(Client.CMD_DATABASEQUERY)) cmdRatingQuery(command);
                    else if(command[0].equals(Client.CMD_USERQUERY)) cmdUserQuery(command);
                    else if(command[0].equals(Client.ERROR)) continue;
                    else
                    {
                        if(!command[0].equals(Client.CMD_LOGOUT))
                            throw new IOException("Not supported command.");
                        break;
                    }
                    notWorthy=false;
                    System.out.println("Listening to \""+name+"\"...");
                }
            } catch (Exception ex)
            {
                System.out.println("FATAL ERROR! Connection terminated.");
                queue.offer(Client.ERROR);
                ex.printStackTrace();
            }
            logout();
            System.out.println("Podłączeni: "+Arrays.toString(clients.keySet().toArray()));
        }

        /** Pętla wysyłająca zakolejkowane odpowiedzi serwera do klienta. */
        private void sendingLoop() // Wątek obsługujący wysyłane komunikaty.
        {
            try {
                String cmd=queue.take();
                while(socket!=null && !socket.isClosed() && cmd!=CLOSE_POINTER)
                {
                    out.println(cmd);
                    System.out.println("Wysłano \""+cmd+"\" do "+name);
                    cmd=queue.take();
                }
            } catch (Exception ex)
            {
                System.err.println("Błąd w wątku wysyłającym do gracza "+name);
                ex.printStackTrace();
            }
            queue.clear();
        }

        /** Czyści stan komunikacji z graczem. */
        private synchronized void cmdClear()
        {
            state=State.NULL;
            if(otherPlayer!=null)
            {
                queue.offer(Client.CMD_CLEAR); // Potwierdź graczowi, że został odłączony
                otherPlayer.queue.offer(Client.CMD_CLEAR); // Potwierdź drugiemu graczowi, że został odłączony
            }
            detach();
        }

        /** Pośredniczy w utworzeniu połączenia z dostepnym graczem. */
        private synchronized void cmdPlay(String[] args)
        {
            ClientInteraction player2=null;
            if (args.length==2) player2=chooseAnyPlayer();
            else
            {
                player2=clients.get(args[2]);
                if (player2!=null && !player2.isAvailable()) player2=null;
            }
            clientPort= Integer.parseInt(args[1]);
            if (player2==null || state!=State.NULL)
            {
                ignorePlayRequest(this);
                return;
            }
            String request = Client.CMD_PLAY+" "+socket.getInetAddress().getHostAddress()+" "+clientPort+" "+name;
            state=State.ASK;
            if (player2.queue.offer(request))
            {
                player2.attach(this);
                state = player2.state= State.GAME;
            }
        }

        /** Odbiera informację o zakończeniu gry.
         * Docelowo może aktualizować rankingi w bazie.
         * W obecnej wersji "protokołu" GAMEEND nie zamyka połączenia - to robi komenda CLEAR */
        private synchronized void cmdGameEnd(String[] args)
        {
            if (otherPlayer==null || state!=State.GAME) return; // throw new IllegalStateException("There is no game played!");
            System.out.println("Gra zakończona przez gracza \""+name+"\".");
            try {
                String players[] = new String[]{otherPlayer.name, name};
                dataBase.commitGameResult(players, 2-args.length); // Gdy jest argument => gracz przegrał.
            } catch (SQLException ex)
            {
                System.out.println("Błąd! Nie udało się zapisać wyniku gry w bazie danych!");
            }
            queue.offer(Client.CMD_GAMEEND+" "+otherPlayer.name); // Informacja dla graczy o końcu gry
            otherPlayer.queue.offer(Client.CMD_GAMEEND+" "+name);
            state= otherPlayer.state = State.TERMINATE; // Nie wiem co ten stan oznacza, tak naprawdę...
        }

        /** Odsyła do klienta listę najlepszych graczy oraz jego miejsce w rankingu */
        private void cmdRatingQuery(String[] command) throws IOException
        {
            try {
                int count = Integer.parseInt(command[2]);
                String playerName = command[1];
                List<RatingEntry> rating = dataBase.askForRating(count);
                RatingEntry player = dataBase.getPlayerEntry(playerName);
                for(RatingEntry entry: rating)
                {
                    queue.offer(Client.CMD_DATABASEQUERY+entry.encode());
                }
                if (player.getPosition()>count)
                    queue.offer(Client.CMD_DATABASEQUERY+player.encode()); // Dopisz gracza do rankingu
                queue.offer(Client.CMD_ENDDATA); // Koniec danych.
            } catch(SQLException ex)
            {
                queue.offer(Client.ERROR); // Błąd połączenia z bazą.
            } catch(NumberFormatException e)
            {
                throw new IOException(e);
            }
        }

        /** Wysyła do gracza informację o innym graczu. */
        private void cmdUserQuery(String[] command) throws IOException
        {
            try {
                String playerName = command[1];
                RatingEntry playerInfo = dataBase.getPlayerEntry(playerName);
                if (playerInfo!=null)
                {
                    boolean online= clients.containsKey(playerName);
                    queue.offer(Client.CMD_USERQUERY + playerInfo.encode()+" "+online);
                }
                else
                    queue.offer(Client.CMD_USERQUERY); // Posty komunikat oznacza brak gracza.
            } catch(SQLException ex)
            {
                queue.offer(Client.ERROR); // Błąd połączenia z bazą.
            } catch(NumberFormatException e)
            {
                throw new IOException(e);
            }
        }

        // Potencjalne, inne metody do obsługi zapytań...

        /** Zamykanie połaczenia z klientem. */
        private synchronized void logout()
        {
            clients.remove(name);
            detach();
            System.out.println("Wylogowanie "+toString());
            queue.add(CLOSE_POINTER);
            try
            {
                socket.close(); // Zamknij połączenie
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            if (notWorthy) // Usuń gracza, który tylko stworzył konto i nic nie zrobił.
                try{
                    dataBase.removeUser(name);
                } catch(SQLException ex)
                {
                    ex.printStackTrace();
                }
        }

        //boolean locked=true;

        private void attach(ClientInteraction other)
        {
            other.otherPlayer=this;
            this.otherPlayer=other;
        }

        private void detach()
        {
            if (otherPlayer!=null)
            {
                otherPlayer.otherPlayer=null;
                otherPlayer=null;
            }
        }

        private void ignorePlayRequest(ClientInteraction other)
        {
            other.state=State.NULL;
            try{
                System.out.print("Wysyłanie graczowi "+other.name+" odmowy... ");
                InetSocketAddress address = new InetSocketAddress(
                        other.socket.getInetAddress().getHostAddress(), other.clientPort);
                Socket returnSocket = new Socket();
                returnSocket.connect(address, playRequestConnectionTimeout);
                new PrintWriter(returnSocket.getOutputStream(),true).println(Client.CMD_CLEAR);
                System.out.print("zrobione! ");
                returnSocket.close();
                System.out.println("Połączenie zamknięte.");
                //returnSocket.shutdownOutput();
            }catch(IOException ex)
            {

            }
        }

        // Wyszukuje losowego, wolnego gracza.
        private ClientInteraction chooseAnyPlayer()
        {
            for(ClientInteraction client: clients.values())
                if(client!=this && client.isAvailable())
                {
                    return client;
                }
            return null;
        }

        public boolean isAttached()
        {
            return otherPlayer!=null;
        }

        public boolean isAvailable()
        {
            return state==State.NULL && otherPlayer==null;
        }

        @Override
        public String toString()
        {
            InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();
            return "\""+name+"\" ("+ addr.toString()+")";
        }

        /** Sprawdza prawdziwość danych logowania.
         * Jesli operacja się nie powiedzie, w zmiennej name jest zapisywany null.
         * @param input dane uwierzytelniające dostarczone przez uzytkownika.
         */
        private void authenticateUser(String input)
        {
            String []data = input.trim().split("//");
            name=data[0];
            if (clients.containsKey(name))
            {
                System.out.println("Gracz o nazwie: " + name + " jest już zalogowany!");
                name=null;
            }
            try {
                if ( dataBase.checkUser(name, data[1].toCharArray()) ) return;
                else
                {
                    System.out.println("Gracz " + name + " nie istnieje w bazie danych!");
                    name=null;
                }
            } catch (SQLException ex)
            {
                System.err.println("Błąd połączenia z bazą danych!");
                ex.printStackTrace();
            }
        }

        /** Metoda opisująca inicjalizację połaczenia.
         * Ustala zmienne-strumienie, wczytuje nazwę gracza i dodaje go do listy zalogowanych.
         * Wyświetla komunikaty diagnostyczne.
         */
        private boolean introduceClient() throws IOException
        {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(),true);
            String hello= timeLimitedReadLine(in,loginTimeout); // Serwer czeka tylko chwilę na logowanie
            if (hello==null)
            {
                socket.close();
                System.out.println("\tNieudane połączenie! "+socket.getInetAddress().toString());
                return true;
            }
            name = null;
            if (hello.startsWith(Client.CMD_LOGIN))
                authenticateUser(hello.substring(Client.CMD_LOGIN.length()));
            else if (hello.startsWith(Client.CMD_REGISTER))
                registerUser(hello.substring(Client.CMD_LOGIN.length()));
            if (name != null)
            {
                clients.put(name,this);
                queue.add(Client.CMD_LOGIN+" "+name); //out.flush();
                //sendingThread = new Thread(
                executor.execute(new Runnable() { // Odpalanie wątku wysyłającego
                    @Override
                    public void run() {
                        sendingLoop();
                    }
                } );
                //sendingThread.start();
                System.out.println("Zalogowano: "+toString());
            }
            else
            {
                out.println(Client.ERROR);
                System.out.println("NIE ZALOGOWANO: "+socket.getInetAddress().getHostAddress());
                socket.close();
                return true;
            }
            state = State.NULL;
            return false;
        }

        private void registerUser(String args)
        {
            String[] data = args.trim().split("//");
            name = data[0];
            try {
                if (dataBase.addUser(name, data[1].toCharArray()))
                {
                    notWorthy=true;
                    return;
                }
                else {
                    System.out.println("Dodanie nowego gracza nie powiodło się!");
                    name = null;
                }
            } catch (SQLException ex) {
                System.err.println("Błąd połączenia z bazą danych!");
                ex.printStackTrace();
            }
        }
    }
}

