package com.loop.game.GameModel;

import com.badlogic.gdx.math.Vector2;

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
    private Cell selected;

    public Game() {
        this.board = new Board();
        this.players = new Player[2];
        this.rnd = new Random();
        this.draw = false;
    }

    public Game(Player players[]) {
        this.board = new Board();
        this.players = players;
        this.rnd = new Random();
        this.draw = false;
    }

    public Cell getSelected() {
        return selected;
    }

    public void setSelected(Cell selected) {
        this.selected = selected;
    }

    public Cell getCell(Vector2 pos) {
        for (Cell cell : getBoardView()) {
            if (cell.getX() == pos.x && cell.getY() == pos.y) {
                return cell;
            }
        }
        return null;
    }

    public Player[] getPlayers () { return players; }

    public void setPlayers(Player p1, Player p2) {
        players[0] = p1;
        players[1] = p2;
    }

    private void changePlayer() { currentPlayer ^= 1; }

    public int getCurrentPlayerNumber() { return currentPlayer; }

    public Player pickFirstPlayer() {
        currentPlayer = rnd.nextInt(2);
        return players[currentPlayer];
    }

    public boolean isDraw() { return draw; }

    /**
     * Metoda zwraca mozliwe typy plytek, kt�re moga zostac polozone
     * na danym polu.
     * @param x - wspolrzedna x
     * @param y - wspolrzedna y
     * @return  - lista typow jako lista obiektow Byte,
     *            pusta lista, gdy nie da sie postawic plytki
     */
    public List<Byte> getPossibleMoves(int x, int y) {
        List<Byte> possibilities = new LinkedList<Byte>();
        if (!draw && !board.isWhiteWin() && !board.isBlackWin()) {
            EmptyCell cell = board.getAvailablePlace(x, y);

            if (cell != null) {
                for (int i=0; i<Tile.ALL_TYPES.length; ++i) {
                    if (cell.tileFits(Tile.ALL_TYPES[i])) {
                        possibilities.add(Tile.ALL_TYPES[i]);
                    }
                }
            }
        }

        return possibilities;
    }

    /**
     * Zakladam, ze dostaje od interfejsu TYLKO plytki z prawidlowym typem
     * i miejscem (getPossibleMoves zapobiega zlym typom)
     */
    public void makeMove(int x, int y, byte type) {
        board.setTile(board.getAvailablePlace(x, y), type);
        changePlayer();
    }

    /**
     * Zwraca zwyciezce, jesli gra jest rozstrzygnieta;
     * jesli jest remis, ustawia zmienna draw na true i zwraca null
     * jesli gra nie jest rozstrzygnieta, zwraca null.
     */
    public Player whoWon() {
        if (board.isWhiteWin() && board.isBlackWin()) {
            draw = true;
            return null;
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

    public Player getCrrPlayer() { return players[currentPlayer]; }
}