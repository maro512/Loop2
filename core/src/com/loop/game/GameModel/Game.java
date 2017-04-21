package com.loop.game.GameModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by marek on 11.04.17.
 */

public class Game {
    private Board board;
    private final int BLACK = 1;
    private final int WHITE = 0;
    private Player[] players;
    private int currentPlayer; // liczba z {0, 1}, do wyboru z tablicy players

    public Game(Board board) {
        this.board = board;
        this.players = new Player[2];
    }

    public Player pickFirstPlayer() {
        Random rnd = new Random();
        currentPlayer = rnd.nextInt(2);
        return players[currentPlayer];
    }

    /**
     * Metoda zwraca możliwe typy płytek, które mogą zostać położone
     * na danym polu.
     * @param x, @param y - współrzędne
     * @return lista typów jako lista obiektów Byte,
     *         null, gdy nie da się postawić płytki
     */
    public List<Byte> getPossibleMoves(int x, int y) {
      List<Byte> possibilities = new LinkedList<Byte>();
      EmptyCell cell = board.getAvailablePlace(x, y);

      if (cell != null && !cell.isDead()) {
        for (int i=0; i<Tile.ALL_TYPES.length; ++i) {
          if (cell.tileFits(Tile.ALL_TYPES[i])) {
            //add(Tile.ALL_TYPES[i]);
          }
        }
      } else {
        return null;
      }

      return possibilities;
    }

    /**
     * Zakładam, że dostaję od interfejsu TYLKO płytki z prawidłowym typem
     * i miejscem (getPossibleMoves zapobiega złym typom)
     */
    public void makeMove(int x, int y, byte type) {
      board.setTile(board.getAvailablePlace(x, y), type);
    }

    public Player changePlayer() {
      ++currentPlayer;
      currentPlayer %= 2;
      return players[currentPlayer];
    }

    /**
     * Zwraca zwycięzcę, jeśli gra jest rozstrzygnięta;
     * jeżeli nie jest, zwraca null.
     */
    public Player whoWon() {
      if (board.isWhiteWin()) {
        return players[WHITE];
      } else if (board.isBlackWin()) {
        return players[BLACK];
      } else {
        return null;
      }
    }

    public Player getCrrPlayer() { return players[currentPlayer]; }
}
