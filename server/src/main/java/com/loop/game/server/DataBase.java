package com.loop.game.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;

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
            ok = stmt.executeUpdate()==1;
        } finally // Pozamykaj wszystko
        {
            if (stmt!=null) try { stmt.close(); } catch (SQLException e) {}
            try { dbc.close(); } catch (SQLException e) {}
        }
        return ok;
    }

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

    public static void main(String[] args) throws Exception
    {
        DataBase db = new DataBase();
        String user = "Piotr";
        char[] pass = "Katowice".toCharArray();
        System.out.println("Dodawanie... "+ db.addUser(user,pass));
        System.out.println("Logowanie... "+ db.checkUser(user,pass));
    }
}
