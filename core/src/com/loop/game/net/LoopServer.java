/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.net;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.logging.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Klasa serwera gry LOOP. Nasłuchuje na porcie o numerze 7457.
 * Created by Piotr on 2017-05-13
 */
public class LoopServer implements Runnable
{
    public static void main(String args[])
    {
        /*
        System.setProperty("javax.net.ssl.keyStore", "trusted.jks");//"C:\\Users\\Piotr\\Documents\\Informatyka UJ\\Loop2\\trusted.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","loop2017");
        System.setProperty("javax.net.ssl.trustStoreType", "jks"); */
        new LoopServer().run();
    }
    
    private SSLServerSocket main;
    private final ExecutorService executor= Executors.newCachedThreadPool();;
    
    public LoopServer()
    {
        clients = new ConcurrentHashMap<String,ClientInteraction>();
    }
    
    @Override
    public void run()
    {
        try{SSLServerSocketFactory factory = //(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
              getSSLContext(new FileInputStream("trusted.jks"), new char[]{'l', 'o', 'o', 'p', '2', '0', '1', '7'})
                      .getServerSocketFactory(); //*/
            main = (SSLServerSocket) factory.createServerSocket(SERVER_PORT);
            boolean go = true;
            main.setNeedClientAuth(false);
            main.setWantClientAuth(false); main.setEnableSessionCreation(true);
            main.setEnabledCipherSuites(main.getSupportedCipherSuites());
            System.out.println(InetAddress.getLocalHost().getHostAddress());
            System.out.print("\tSerwer waits for new connection... ");
            //executor
            while(go)
            {
                SSLSocket incomming= (SSLSocket) main.accept();
                System.out.print("Connected? ");
                executor.execute(new ClientInteraction(incomming));
            }
            main.close();
        } catch (Exception ex)
        {
            Logger.getLogger(LoopServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static final int SERVER_PORT= 7457;
    //public static final int CLIENT_PORT= 7459;
    
    private Map<String,ClientInteraction> clients; 
    
    enum State { NULL, ASK, GAME, TERMINATE, LOCKED }

    /** Wewnętrzna klasa reprezentująca połączenie z pojedyńczym klientem. */
    class ClientInteraction implements Runnable
    {
        public ClientInteraction(SSLSocket socket)
        {
            this.socket=socket;
        }
        
        private String name;
        private SSLSocket socket;
        private Scanner in; // Obsługa danych przychodzących
        private PrintWriter out; // Obsługa danych wychodzących
        private BlockingQueue<String> queue;
        private ClientInteraction otherPlayer; // Ten drugi gracz, z kórym się połączyliśmy
        private State state = State.LOCKED;
        private int clientPort;
        private Thread sendingThread; // To jest wątek, który trzeba potem zabić.

        @Override
        public void run() // Pętla komunikacji z pojedynczym klientem
        {
            try {
                if ( introduceClient() ) return; // Błąd logowania.
                while(in.hasNextLine() ) // Przetwarzanie w pętli zapytań klienta.
                {
                    String[] command = in.nextLine().split(" ");
                    System.out.println("("+name+"): "+Arrays.toString(command));

                    if (command[0].equals(CMD_PLAY)) cmdPlay(command);
                    else if (command[0].equals(CMD_CLEAR)) cmdClear();
                    else if (command[0].equals(CMD_GAMEEND)) cmdGameEnd(command);
                    
                    // Tu można podododawać inne komendy, które serwer może obsługiwać.
                    else if(command[0].equals(CMD_LISTAVAILABLE)) cmdListAvailable(command);
                    else
                    {
                        if(!command[0].equals(CMD_LOGOUT))
                        {
                            System.err.println(
                                "Communication protocol error. Connection terminated.");
                            queue.offer(ERROR);
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
        private static final String CLOSE_POINTER= "\n";
        
        /** Czyści stan komunikacji z graczem. */
        private void cmdClear()
        {
            state=State.NULL;
            if(otherPlayer!=null)
            {
                queue.offer(CMD_CLEAR); // Potwierdź graczowi, że został odłączony
                otherPlayer.queue.offer(CMD_CLEAR); // Potwierdź drugiemu graczowi, że został odłączony
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
            String request = CMD_PLAY+" "+socket.getInetAddress().getHostAddress()+" "+clientPort+" "+name;
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
            queue.offer(CMD_GAMEEND+" "+otherPlayer.name);
            otherPlayer.queue.offer(CMD_GAMEEND+" "+name);
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
            if (name!=null)
            {
                clients.remove(name);
                detach();
                System.out.println("Wylogowanie " + toString());
                queue.add(CLOSE_POINTER);
            }
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
                new PrintWriter(returnSocket.getOutputStream(),true).println(CMD_CLEAR);
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

        //TODO Nie wiem jak to bedzie ostatecznie działać. 
        private void authenticateUser(String input)
        {
            name = input.trim();
            if (clients.containsKey(name))
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
            socket.startHandshake();
            System.out.println(" YEAH!");
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
            if (hello.startsWith(CMD_LOGIN))
                authenticateUser(hello.substring(CMD_LOGIN.length()));
            if (name != null)
            {
                clients.put(name,this);
                queue=new LinkedBlockingQueue<String>();
                queue.add(CMD_LOGIN+" "+name); //out.flush();
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
    
    public static final String CMD_LOGIN            ="loGIN";   // Logowanie
    //public static final String CMD_REGISTER            ="NewOnE";   // Rejestracja ?
    public static final String CMD_LOGOUT           ="loGOuT";
    public static final String CMD_PLAY             ="pLAYnow"; // Rządanie rozpoczęcia gry online
    public static final String CMD_LISTAVAILABLE    ="avaILabLe";
    public static final String CMD_DATABASEQUERY    ="getDATA";
    public static final String CMD_CLEAR            ="eNdALl"; // Gwałtowny koniec gry
    public static final String CMD_GAMEEND          ="KonIEc"; // Oficjalny koniec gry
    public static final String ERROR                ="erROr";  // Coś na wypadek błędu.

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
            e.printStackTrace();
            return null;
        }
    }

    private long loginTimeout= 1000;
    private int playRequestConnectionTimeout= 10000;

    public void setPlayRequestConnectionTimeout(int timeout)
    {
        playRequestConnectionTimeout = timeout;
    }

    public void setLoginTimeout(long timeout)
    {
        loginTimeout=timeout;
    }

    /** Tworzy "kontekst SSL", ktory pozwala generować fabryki gniazd itd.
     * Wymaga podania źródła certyfikatu i */
    static SSLContext getSSLContext(InputStream certSource, char[] pass) throws Exception
    {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(certSource, pass); certSource.close();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//"X509");
        kmf.init(ks, pass);
        Arrays.fill(pass,'\0'); // Zamaż hasło.
        TrustManagerFactory trustmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());//getInstance("X509");
        trustmf.init(ks);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), trustmf.getTrustManagers(), null);
        return sslContext;
    }
}

