/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.server;

import com.loop.game.Net.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Klasa serwera gry LOOP. Nasłuchuje na porcie o numerze 7457.
 * Created by Piotr on 2017-05-13
 * Edited by Kamil on 2017-05-21
 */
//TODO: gdy serwer jest wyłączony i próbujemy łączyć się do niego 2 razy to socket nie jst zamkniety
public class LoopServer implements Runnable
{
private final ExecutorService executor= Executors.newCachedThreadPool();
ivate String
    ate List;
    private ServerSocket main;
    private Map<String,ClientInteraction> clients;
    

    //public static final int CLIENT_PORT= 7459;
    private long loginTimeout= 10000;
    private int playRequestConnectionTimeout= 10000;     public LoopServer()
    {
        database = new LinkedList<Player_Server>();
        clients = new ConcurrentHashMap<String,ClientInteraction>();
    }

timeLimitedReadLine(final Scanner in, long milis)
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

    public static void main(String args[])
    {
        new LoopServer().run();
    }

    @Override
    public void run()
    {
        try {
            main = new ServerSocket(Client.SERVER_PORT);
            boolean go = true;
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            System.out.print("\tSerwer waits for new connection... ");
            buildTestDatabase(); // TODO: podłączenie bazy danych
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

    priv

    public void setLoginTimeout(long timeout)
    {
        loginTimeout=timeout;
    }void buildTestDatabase(){
        database.add(new Player_Server("a","aa"));
        database.add(new Player_Server("b", "bb"));
        database.add(new Player_Server("adam", "pass123"));
        database.add(new Player_Server("admin", "admin1"));
        database.add(new Player_Server("Projekt", "na5"));
    }<Player_Server> database;

    publ
enum State { NULL, ASK, GAME, TERMINATE, LOCKED }

    pric
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
        private Thread sendingThread; // To jest wątek, który trzeba potem zabić.

        public ClientInteraction(Socket socket)
        {
            this.socket=socket;
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
                    else if(command[0].equals(Client.CMD_LISTAVAILABLE)) cmdListAvailable(command);
                    else
                    {
                        if(!command[0].equals(Client.CMD_LOGOUT))
                        {
                            System.err.println(
                                "Communication protocol error. Connection terminated.");
                            queue.offer(Client.ERROR);
                        }
                        break;
                    }
                }
            } catch (IOException ex)
            {
                System.out.println("ERROR! ");
                Logger.getLogger(ClientInteraction.class.getName()).log(Level.SEVERE, null, ex);
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
                    cmd=queue.take();
                }
            } catch (Exception ex)
            {
            }
        }

        /** Czyści stan komunikacji z graczem. */
        private void cmdClear()
        {
            state=State.NULL;
            if(otherPlayer!=null)
            {
                queue.offer(Client.CMD_CLEAR); // Potwierdź graczowi, że został odłączony
                otherPlayer.queue.offer(Client.CMD_CLEAR); // Potwierdź drugiemu graczowi, że został odłączony
            }
            detach();
        }

        /** Pośredniczy w utworzeniu połączenia z dostepnym graczem.
         * Na razie tylko z losowym. */
        private void cmdPlay(String[] args)
        {
            ClientInteraction player2=chooseAnyPlayer(); // Wystarczy to uzleżnić od argumentów...
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
        private void cmdGameEnd(String[] args)
        {
            if (otherPlayer==null || state!=State.GAME) return; // throw new IllegalStateException("There is no game played!");
            System.out.println("Gra zakończona przez gracza \""+name+"\".");
            queue.offer(Client.CMD_GAMEEND+" "+otherPlayer.name);
            otherPlayer.queue.offer(Client.CMD_GAMEEND+" "+name);
            state=otherPlayer.state= State.TERMINATE; // Nie wiem co ten stan oznacza, tak naprawdę...
        }


        /** Odsyła do klienta listę zalogowanych użytkowników. */
        private void cmdListAvailable(String[] command)
        {
            // if (state!=State.NULL) return;
            //TODO
        }

        // inne metody do obsługi zapytań

        /** Zamykanie połaczenia z klientem. */
        private void logout()
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
                InetSocketAddress address = new InetSocketAddress(other.socket.getInetAddress().getHostAddress(),other.clientPort);
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

        private boolean checkDatabase(String []data){
            if(data.length>2) return false;
            for (Player_Server p: database) {
                if(p.check(data[0],data[1])) return true;
            }
            return false;
        }

        private void authenticateUser(String input)
        {
            String []data = input.trim().split("//");
            name = data[0];
            if(!checkDatabase(data)){
                System.out.println("Gracz "+name+" nie istnieje w bazie danych!");
                name=null;
            }
            else if (clients.containsKey(name))
            {
                System.out.println("Gracz o nazwie: "+input+" już jest zalogowany!");
                name=null;
            }
        }

        /** Mewtoda opisująca inicjalizację połaczenia.
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
            if (name != null)
            {
                clients.put(name,this);
                queue=new LinkedBlockingQueue<String>();
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
                System.out.println("NIE ZALOGOWANO: "+socket.getInetAddress().getHostAddress());
                socket.close();
                return true;
            }
            state = State.NULL;
            return false;
        }
    }
}
