package com.loop.game.GameModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by marek on 11.04.17.
 * Edited by ciepiela on 22.04.17.
 */

public class Game {
    private Board board;
    private final int BLACK = 1;
    private final int WHITE = 0;
    private Player[] players;
    private int currentPlayer; // liczba z {0, 1}, do wyboru z tablicy players

    public Game() {
        this.board = new Board();
        this.players = new Player[2];
    }

    private void changePlayer() { currentPlayer ^= 1; }

    public Player pickFirstPlayer() {
        Random rnd = new Random();
        currentPlayer = rnd.nextInt(2);
        return players[currentPlayer];
    }

    /**
     * Metoda zwraca mo�liwe typy p�ytek, kt�re mog� zosta� po�o�one
     * na danym polu.
     * @param x, @param y - wsp�rz�dne
     * @return lista typ�w jako lista obiekt�w Byte,
     *         pusta lista, gdy nie da si� postawi� p�ytki
     */
    public List<Byte> getPossibleMoves(int x, int y) {
      if (!board.isWhiteWin() && !board.isBlackWin()) {
        List<Byte> possibilities = new LinkedList<Byte>();
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
     * Zak�adam, �e dostaj� od interfejsu TYLKO p�ytki z prawid�owym typem
     * i miejscem (getPossibleMoves zapobiega z�ym typom)
     */
    public void makeMove(int x, int y, byte type) {
      board.setTile(board.getAvailablePlace(x, y), type);
      changePlayer();
    }

    /**
     * Zwraca zwyci�zc�, je�li gra jest rozstrzygni�ta;
     * je�eli nie jest, zwraca null.
     */
    public Player whoWon() {
      if (board.isWhiteWin() && board.isBlackWin()) {
        return new Player("DRAW");
      } else if (board.isWhiteWin()) {
        return players[WHITE];
      } else if (board.isBlackWin()) {
        return players[BLACK];
      } else {
        return null;
      }
    }

    public Player getCrrPlayer() { return players[currentPlayer]; }
}