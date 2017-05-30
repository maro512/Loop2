package com.loop.game.server;

import com.loop.game.Net.RatingEntry;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import static java.nio.charset.Charset.*;

/**
 * Created by Piotr on 2017-05-29.
 */

public class DataBase
{
    private static final String url =
            "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=LoopDb;integratedSecurity=true";
                    // user=LoopServer;password=najlepszyProjekt";

    public DataBase() throws NoSuchAlgorithmException, ClassNotFoundException
    {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        sha= MessageDigest.getInstance("SHA1");
        reader = null;
    }

    private MessageDigest sha;
    private ResultSet reader;
    private Statement statement;
    private Connection connection;

    /* _____________ METODY MANIPULUJĄCE UŻYTKOWNIKAMI _____________________________
        zwracają true, jesli operacja się powiedzie i flase, jeśli nie udało się
        zmodyfikować bazy. Jeśli nie uda się nawiązać połączenia, lub wystapi jakiś
        inny błąd, zostaje wyrzucony wyjątek SQLException.
     */

    /** Sprawdzenie nazwy użytkownika i hasła w bazie */
    public boolean checkUser(String name, char[] pass) throws SQLException
    {
        boolean ok = false;
        String hash = getHash(pass);
        Connection dbc= connect();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = dbc.prepareStatement(
                    "SELECT Name FROM Ranking WHERE Name = ? AND Pass = ?",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, name);
            stmt.setString(2, hash);
            rs = stmt.executeQuery();
            ok = rs.next();
        } finally // Pozamykaj wszystko
        {
            if (rs!=null) try { rs.close(); } catch (SQLException e) {}
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok;
    }

    /** Dodanie nowego użtykownika do bazy. */
    public boolean addUser(String name, char[] pass) throws SQLException
    {
        boolean ok = false;
        String hash = getHash(pass);
        Connection dbc= connect();
        System.out.println("Connection to data base: successfull!");
        PreparedStatement stmt = null;
        try {
            stmt = dbc.prepareStatement("INSERT INTO Ranking (Name, Pass) VALUES(? ,?)");
            stmt.setString(1, name);
            stmt.setString(2, hash);
            try {
                ok = stmt.executeUpdate() == 1;
            } catch (SQLServerException ex)
            {
                ok = false;
            }
        } finally // Pozamykaj wszystko
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok;
    }

    /** Zmiana hasła. */
    public boolean changePassword(String name, char[] newPass) throws SQLException
    {
        boolean ok = false;
        String hash = getHash(newPass);
        Connection dbc= connect();
        System.out.println("Connection to data base: successfull!");
        PreparedStatement stmt = null;
        try {
            stmt = dbc.prepareStatement("UPDATE Ranking SET Pass=? WHERE Name=?");
            stmt.setString(1, hash);
            stmt.setString(2, name);
            try {
                ok = stmt.executeUpdate() == 1;
            } catch (SQLServerException ex)
            {
                ok = false;
            }
        } finally // Pozamykaj wszystko
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok;
    }

    //________________________________________________________________

    //public

    /** Zapisuje hasło w bazie, w nieczytelnej formie. */
    private String getHash(char[] pass)
    {
        sha.reset();
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.wrap(pass);
        byte[] hash = sha.digest( utf8.encode(cb).array() );
        hash = Arrays.copyOfRange(hash,0,128);
        //cb.reset();
        String hashString = utf8.decode(ByteBuffer.wrap(hash)).toString();
        return hashString.replace('\'','_');
    }

    private static Connection connect() throws SQLException
    {
        return DriverManager.getConnection(url);
    }

    public static void main(String[] args) throws Exception // Test operacji na bazie
    {
        DataBase db = new DataBase();
        String user = "Piotr";
        char[] pass = "Katowice".toCharArray();
        char[] pass2="katowice".toCharArray();
            System.out.println(" Dodawanie... "+ db.addUser(user,pass));
            System.out.println(" Dodawanie... "+ db.addUser("testuser","loop".toCharArray()));
        System.out.println("Logowanie1... "+ db.checkUser(user,pass));
        System.out.println("Logowanie2... "+ db.checkUser(user,pass2));
        System.out.println("Zmiana hasła... "+ db.checkUser(user,pass2));
        System.out.println("Logowanie1... "+ db.checkUser(user,pass));
        System.out.println("Logowanie2... "+ db.checkUser(user,pass2));
        System.out.println("Powrót hasła... "+ db.checkUser(user,pass));
    }

    public void askForRating(int from, int count)
    {
        if (connection!=null) throw new IllegalStateException("Previous connection not closed!");
        try{
            connection=connect();
            statement= connection.prepareStatement("SELECT Name, GamesPlayed, Points FROM Ranking");
        } catch(SQLException ex) {

        }
    }

    /** Konwersja zbioru wyników na
    private List<RatingEntry> extractRatingEntries(ResultSet result)
    {
        List<RatingEntry> list = new ArrayList<RatingEntry>();
        try{
            while(result.next())
            {
                list.add(new RatingEntry(result.getString("Name"),
                                         result.getInt("TotalGames"),
                                         result.getInt("Points")));
            }
        } catch (SQLException ex)
        {
            System.err.println(ex);
        }
        finally
        {
            try{
                result.close(); // Zamknij zbiór wyników
            } catch(SQLException ex) {}
        }
        return list;
    } */
}
