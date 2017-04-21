package com.loop.game.GameModel;

/**
 * Created by marek on 11.04.17.
 * Beeing updated by Piotr since 13.04.17
 */
public abstract class Cell
{
    private Cell[] neighbours;
    private Position position;

    public Cell(Position position)
    {
        this.position = position;
        neighbours = new Cell[4];
    }

    public final Position getPosition()
    {
        return position;
    }

    public final Cell getNeighbour(int direction)
    {
        if (direction<0 || direction >3) throw new IllegalArgumentException("Invalid direction.");
        return neighbours[direction];
    }

    /** Funkcja odwrotna do getNeighbour(direction)
     * @param nighbour pewnien sąsiad pola/komórki
     * @return kierunek dir taki, że getNeighbour(dir)==nighbour
     */
    public final int getDirection(Cell nighbour)
    {
        if (nighbour==null) return -1;
        // Rozwiązanie obliczeniowe
        int dx = 1+position.getX()-nighbour.position.getX();
        int dy = 1-position.getY()+nighbour.position.getY();
        /* Jeśli mamy do czynienia z sąsiadami, (dx,dy) jest postaci:
            (1,2) - siąsiad południowy  (2,1) - sąsiad zachodni
            (1,0) - sąsiad północny     (0,1) - sąsiad wschodni
        */
        // iloczyn dx*dy jest 0 lub 2 i jedna z nich jest 1 - pełna charakteryzacja
        int c=dx*dy;
        if ( (c | 2)!=2 || (dx+dy-c)!=1 ) return -1; // To nie są sąsiedzi
        int direction= ( (dx & 1)+ c ); // Wygląda niejasno, ale działa.
        /*
        short direction=-1; // Rozwiązanie czytelne:
        if (nighbour.getX() == getX()-1) direction = Position.WEST;
        if (nighbour.getX() == getX()+1) direction = Position.EAST;
        if (nighbour.getY() == getY()-1) direction = Position.NORTH;
        if (nighbour.getY() == getY()+1) direction = Position.SOUTH;
        if (direction<0) return -1;
        // */
        if (neighbours[direction]!=nighbour) // Sąsiedzi wg. współrzędnych, ale coś się nie zgadza
            throw new IllegalStateException("Invalid cell graph configuration!");
        return direction;
    }

    /** Ta metoda reaguje na zmianę sąsiada. Będzie jej używała klasa EmptyCell aby poznać
     * swoje otoczenie, a klasa Tile będzie sprawdzała zgodność kolorów.
      */
    protected void fireAppend(int direction, Cell cell) {}

    // Ta metoda przypina sąsiada.
    public void append(int direction, Cell cell)
    {
        if (direction<0 || direction >3) throw new IllegalArgumentException("Invalid direction.");
        if (cell==null || neighbours[direction]==cell) return; // Nie przypinaj null
        if (!cell.position.equals(position.getNeighbour(direction)))
            throw new IllegalArgumentException("Appending neighbour from wrong side.");
        if (neighbours[direction]!=null)
            neighbours[direction].neighbours[direction ^2]=null; // Skasuj starego sąsiada
        fireAppend(direction,cell); // Zastanów się, czy zmiana sasiada coś znaczy
        cell.fireAppend(direction ^2,this); // Powiadom sąsiada o zmianie
        neighbours[direction] =cell; // Ustaw nowego sąsiada
        cell.neighbours[direction ^ 2]=this; // Ustaw sąsiada (this) nowemu sąsiadowi
    }

    public boolean isTile()
    {
        return false;
    }

    /** Metoda pomocnicza do zastępowania płytki inną płytką. Kopiuje i powiadamia sąsiadów.
     * @param other
     */
    void replaceMe(Cell other)
    {
        //if (!other.position.equals(position)) throw new IllegalArgumentException("Positions do not agree.");
        for(int dir =0; dir<4; dir++)
        {
            if (neighbours[dir]!=null)
            {
                //if (other==null) neighbours[dir].neighbours[dir^2]=null;
                //else
                other.append(dir, neighbours[dir]);
            }
            neighbours[dir]=null;
        }
    }

    // Maski bitowe kierunków na potrzeby klas konretnych
    protected static final byte[] mask= new byte[]{1,2,4,8};
}
