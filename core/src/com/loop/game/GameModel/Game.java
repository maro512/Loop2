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
    private Cell selected;
    private boolean terminated;

    public Game() {
        this.board = new Board();
        this.players = new Player[2];
        this.rnd = new Random();
        this.terminated = false;
    }

    public Game(Player players[]) {
        this.board = new Board();
        this.players = players;
        this.rnd = new Random();
        this.terminated = false;
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

    public Player pickFirstPlayer() {
        currentPlayer = rnd.nextInt(2);
        return players[currentPlayer];
    }

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
        if (!board.isWhiteWin() && !board.isBlackWin()) {
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

    private boolean checkEnd() {
        if (board.isBlackWin() || board.isWhiteWin()) {
            terminated = true;
        }

        return terminated;
    }

    public void makeMove(byte type) {
        board.setTile(board.getAvailablePlace(selected.getX(), selected.getY()), type);

        if (!checkEnd()) {
            changePlayer();
        }
    }

    public boolean isTerminated() { return terminated; }

    public Collection<Cell> getBoardView(){
        return board.getCells();
    }

    public List<Tile> getWinningLine() {
        return board.getWinningLine(currentPlayer == BLACK);
    }

    public Player getCrrPlayer() { return players[currentPlayer]; }
}