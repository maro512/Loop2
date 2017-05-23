package com.loop.game.GameModel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by marek on 11.04.17.
 * Edited by ciepiela on 25.04.17.
 */

public class Game {
    private Board board;
    private final int BLACK = 1;
    private final int WHITE = 0;
    private Player[] players;
    private int currentPlayer; // liczba z {0, 1}, do wyboru z tablicy players
    private Random rnd;
    private boolean draw;

    public Game(Player[] players)
    {
        this.board = new Board();
        this.players = players;
        this.rnd = new Random();
        this.draw = false;
    }

    public Game() {
        this.board = new Board();
        this.players = new Player[2];
        this.rnd = new Random();
        this.draw = false;
    }

    public void setPlayers(Player p1, Player p2) {
        players[0] = p1;
        players[1] = p2;
    }

    private void changePlayer() // Nie można zmienić gracza, po tym jak gra się zakończyła!
    {
        currentPlayer ^= 1;
    }

    public Player pickFirstPlayer() // Po co to komu?
    {
        currentPlayer = rnd.nextInt(2);
        return players[currentPlayer];
    }

    public boolean isDraw() { return board.isWhiteWin() && board.isBlackWin(); } // False?

    /**
     * Metoda zwraca mozliwe typy plytek, które moga zostac położone
     * na danym polu.
     * @param x - współrzędna x
     * @param y - współrzędna y
     * @return  - lista typów jako lista obiektów Byte,
     *            pusta lista, gdy nie da sie postawić płytki
     */
    public List<Byte> getPossibleMoves(int x, int y) {
        List<Byte> possibilities = new LinkedList<Byte>();
        if (!board.isWhiteWin() && !board.isBlackWin())
        {
            EmptyCell cell = board.getAvailablePlace(x, y);

            if (cell != null)
            {
                for (byte type : Tile.ALL_TYPES)
                {
                    if (cell.tileFits(type))
                        possibilities.add(type);
                }
            }
        }
        return possibilities;
    }

    /**
     * Zakładam, że dostaję od interfejsu TYLKO płytki z prawidłowym typem
     * i miejscem (getPossibleMoves zapobiega złym typom)
     */
    public void makeMove(int x, int y, byte type)
    {
        if (board.isWhiteWin() || board.isBlackWin()) return;

        board.setTile(board.getAvailablePlace(x, y), type);
        changePlayer();
    }

    /**
     * Zwraca zwyciezce, jesli gra jest rozstrzygnieta;
     * jesli jest remis, ustawia zmienna draw na true i zwraca null
     * jesli gra nie jest rozstrzygnieta, zwraca null.
     */
    public Player whoWon() {
        if (board.isWhiteWin() && board.isBlackWin())
        {
            return //null;
                getCrrPlayer(); // Wersja bezremisowa.
        } else if (board.isWhiteWin()) {
            return players[WHITE];
        } else if (board.isBlackWin()) {
            return players[BLACK];
        } else {
            return null;
        }
    }

    public Collection<Cell> getBoardView(){
        return board.getCells();
    }

    public boolean isTerminated() {
        return board.isWhiteWin() || board.isBlackWin();
    }

    public List<Tile> getWinLine(){
        if (board.isWhiteWin() && currentPlayer==0)
            return board.getWinningLine(false);
        else
            return board.getWinningLine(true );
    }

    public Player getCrrPlayer() { return players[currentPlayer]; }

    public int getCurrentPlayerNumber() {return currentPlayer; }
}