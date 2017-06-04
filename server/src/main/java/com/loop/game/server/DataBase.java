package com.loop.game.server;

import com.loop.game.GameModel.Player;
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
    }

    private MessageDigest sha;
    private boolean strictAmountOfRatingEntries = false;

    public boolean isStrictAmountOfRatingEntries()
    {
        return strictAmountOfRatingEntries;
    }

    public void setStrictAmountOfRatingEntries(boolean strict)
    {
        strictAmountOfRatingEntries=strict;
    }

    /* _____________ METODY MANIPULUJĄCE UŻYTKOWNIKAMI _____________________________
        zwracają true, jesli operacja się powiedzie i flase, jeśli nie udało się
        zmodyfikować bazy. Jeśli nie uda się nawiązać połączenia, lub wystąpi jakiś
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
            stmt = dbc.prepareStatement( SQL_VERIFY, ResultSet.CONCUR_READ_ONLY);
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

    public boolean addUser(String name, char[] pass) throws SQLException
    {
        boolean ok = false;
        String hash = getHash(pass);
        Connection dbc= connect();
        //System.out.println("Connection to data base: successfull!");
        PreparedStatement stmt = null;
        try {
            stmt = dbc.prepareStatement( SQL_ADDUSER );
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

    public boolean changePassword(String name, char[] newPass) throws SQLException
    {
        boolean ok = false;
        String hash = getHash(newPass);
        Connection dbc= connect();
        //System.out.println("Connection to data base: successfull!");
        PreparedStatement stmt = null;
        try {
            stmt = dbc.prepareStatement( SQL_SETPASSWORD );
            stmt.setString(1, hash);
            stmt.setString(2, name);
            try {
                ok = stmt.executeUpdate() == 1;
            } catch (SQLServerException ex)
            {
                ok = false;
            }
        } finally
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok;
    }

    //________________________________________________________________

    /* public boolean commitGameResult(Player[] players, int winner) throws SQLException
    {
        String[] names = new String[]{players[0].getName(), players[1].getName()};
        return commitGameResult(names,winner);
    } */

    public boolean commitGameResult(String[] playerName, int winner) throws SQLException
    {
        boolean ok = false;
        if (playerName.length!=2 || (winner & -2) != 0) throw new IllegalArgumentException();
        if (playerName[0].equals(playerName[1])) throw new IllegalArgumentException();
        Connection dbc= connect();
        //System.out.println("Connection to data base: successfull!");
        PreparedStatement stmt = null;
        try {
            dbc.setAutoCommit(false);
            dbc.setSavepoint();
            stmt = dbc.prepareStatement( SQL_UPDATEPOINTS, ResultSet.CONCUR_UPDATABLE);
            for(int i=0; i<2; i++)
            {
                stmt.setInt(1, i==winner ? 1 : 0);
                stmt.setString(2, playerName[i]);
                try {
                    ok = stmt.executeUpdate()==1;
                } catch (SQLServerException ex) {
                    ok = false;
                }
                if (!ok) {
                    dbc.rollback(); // Odwołaj oprzednie zmiany w bazie!
                    break;
                }
            }
            dbc.commit();
        } finally // Pozamykaj wszystko
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok; // Zwróć, czy się udało
    }

    /** Koduje podane hasło do dziwnej postaci. */
    private String getHash(char[] pass)
    {
        sha.reset();
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.wrap(pass);
        byte[] hash = sha.digest( utf8.encode(cb).array() );
        String hashString = utf8.decode(ByteBuffer.wrap(hash)).toString();
        if (hashString.length()>128) hashString=hashString.substring(0,128);
        return hashString.replace('\'','_');
    }

    private static Connection connect() throws SQLException
    {
        return DriverManager.getConnection(url);
    }

    public List<RatingEntry> askForRating(int count) throws SQLException
    {
        Connection dbc=connect();
        PreparedStatement stmt=null;
        ResultSet result = null;
        List<RatingEntry> topPlayers=null;
        try{
            stmt= dbc.prepareStatement( strictAmountOfRatingEntries ? SQL_RATING : SQL_RATINGPLUS );
            stmt.setInt(1,count);
            result = stmt.executeQuery();
            topPlayers = extractRatingEntries(result);
        }
        finally
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            if (result!=null) try { result.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return topPlayers;
    }

    /** Zwraca rekord z bazy odpowiadający pozycji konkretnego gracza w rankingu. */
    public RatingEntry getPlayerEntry(String name) throws SQLException
    {
        Connection dbc=connect();
        PreparedStatement stmt=null;
        ResultSet result = null;
        RatingEntry player=null;
        try{
            stmt= dbc.prepareStatement( SQL_RATINGPLACES+" WHERE Name=?" );
            stmt.setString(1,name);
            result = stmt.executeQuery();
            if (result.next())
            {
                player= new RatingEntry(result.getInt("Place"), result.getString("Name"),
                                        result.getInt("TotalGames"), result.getInt("Points"));
            }
        }
        finally
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            if (result!=null) try { result.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return player;
    }

    /** Konwersja zbioru wyników na zbiór rekordów. */
    private List<RatingEntry> extractRatingEntries(ResultSet result)
    {
        List<RatingEntry> list = new ArrayList<RatingEntry>();
        int prev_total=0, prev_pts=-1;
        int lp=0;
        try{
            while(result.next())
            {
                int total=result.getInt("TotalGames");
                int points=result.getInt("Points");
                if( total!=prev_total || points!=prev_pts) lp++;
                prev_pts=points; prev_total=total;
                list.add( new RatingEntry(lp,result.getString("Name"),total,points) );
            }
        } catch (SQLException ex)
        {
            System.err.println(ex);
        }
        finally
        {
            try{
                result.close();
            } catch(SQLException ex) {}
        }
        return list;
    } //*/

    private static final String SQL_RATING=
            "SELECT TOP(?) Name, TotalGames, Points FROM Ranking ORDER BY Points DESC, TotalGames DESC";
    private static final String SQL_ADDUSER=
            "INSERT INTO Ranking (Name, Pass) VALUES(? ,?)";
    private static final String SQL_SETPASSWORD=
            "UPDATE Ranking SET Pass=? WHERE Name=?";
    private static final String SQL_UPDATEPOINTS=
            "UPDATE Ranking SET TotalGames=TotalGames+1, Points=Points+? WHERE Name=?";
    private static final String SQL_VERIFY=
        "SELECT Name FROM Ranking WHERE Name = ? AND Pass = ?";
    private static final String SQL_RATINGPLACES=
        "SELECT P.Place, R.Name, R.TotalGames, R.Points FROM Ranking AS R " +
          "JOIN (SELECT MIN(Lp) AS Place, TotalGames, Points FROM " +
          "(SELECT ROW_NUMBER() OVER(ORDER BY Points DESC, TotalGames DESC) AS Lp, TotalGames, Points FROM Ranking) AS Sorted "+
          "GROUP BY TotalGames, Points) AS P ON P.TotalGames=R.TotalGames AND P.Points=R.Points";
    private static final String SQL_RATINGPLUS=
        "SELECT * FROM ("+SQL_RATINGPLACES+") AS R WHERE Place<=? ORDER BY Place";

    //==============================================================================================
    /** Test operacji na bazie */
    public static void main(String[] args) throws Exception
    {
        DataBase db = new DataBase();
        String user = "Piotr";
        char[] pass = "Katowice".toCharArray();
        char[] pass2= "katowice".toCharArray();
        System.out.println(" Dodawanie... "+ db.addUser(user,pass));
        System.out.println(" Dodawanie... "+ db.addUser("testuser","loop".toCharArray()));
        System.out.println("Logowanie1... "+ db.checkUser(user,pass));
        System.out.println("Logowanie2... "+ db.checkUser(user,pass2));

        System.out.println("Zmiana hasła... "+ db.changePassword(user,pass2));
        System.out.println("Logowanie1... "+ db.checkUser(user,pass));
        System.out.println("Logowanie2... "+ db.checkUser(user,pass2));
        System.out.println("Powrót hasła... "+ db.changePassword(user,pass));
        System.out.println("Gra1... "+ db.commitGameResult(new String[]{"Piotr","testuser"},0));
        System.out.println("Gra2... "+ db.commitGameResult(new String[]{"JFGauss","testuser"},1)); // To się nie uda
        System.out.println("Gra4... "+ db.commitGameResult(new String[]{"Player02","testuser"},1));
        System.out.println("Gra3... "+ db.commitGameResult(new String[]{"testuser","Newton"},1)); // To też się nie uda
        System.out.println( db.getPlayerEntry("Piotr"));
        List<RatingEntry> top10 = db.askForRating(10);
        for(RatingEntry player: top10)
            System.out.println("\t"+player);
    }
}
