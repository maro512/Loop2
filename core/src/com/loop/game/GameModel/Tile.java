package com.loop.game.GameModel;

/**
 * Created by marek on 11.04.17.
 * Beeing updated by Piotr since 14.04.17
 */

public class Tile extends Cell
{
    private byte type;

    public static final byte TYPE_NS =10; // Czarna pionowo
    public static final byte TYPE_WE =5;  // Czarna poziomo

    public static final byte TYPE_SE =9;  // Czarna z dołu w prawo
    public static final byte TYPE_NE =3;  // Czarna z prawej w górę
    public static final byte TYPE_NW =6;  // Czarna lewej w górę
    public static final byte TYPE_SW =12; // Czarna z dołu w lewo

    public static final byte[] ALL_TYPES=
            new byte[]{TYPE_SE, TYPE_NE, TYPE_NW, TYPE_SW, // Zakręty
                    TYPE_NS, TYPE_WE}; // Skrzyżowania

    // Konstruktor o zasięgu pakietowym, aby nikt nie tworzył płytek "od siebie".
    Tile(Position position, byte type)
    {
        super(position);
        this.type = type;
    }

    public byte getType()
    {
        return type;
    }

    public boolean getColor(int direction)
    {
        return (mask[direction] & type) != 0;
    }

    /** Wskazuje następną płytkę, idąc po linii przychodzącej z płytki "from".
     * Jeśli na wyniku isTile()==false, znaczy że linia się kończy na bieżącej płytce.
     * Zwrócona EmptyCell pozwala wyznaczyć kierunek zakończenia linii.
     */
    public Cell nextOnLine(Tile from)
    {
        int direction_in = getDirection(from);
        int direction_out= otherEnd(direction_in);
        return getNeighbour(direction_out);
    }

    /** Poniższa metoda zwraca (dowolnego) sąsiada w wybranym kolorze.
     * Zwrócony sasiad jest typu EmptyCell jedynie wtedy, gdy linia w wybranym kolorze
     * nie przełuża się poza płytkę.
     * @param color kolor linii, który nas interesuje (true="czarny", false="biały")
     * @return sąsiednią płytkę, lub wolne pole
     */
    public Cell getColorNeighbour(boolean color)
    {
        int direction= 0;
        while ( ((type & mask[direction]) == 0)==color ) direction++; // Znajdź odpowiedni bit
        Cell neighbour = getNeighbour(direction);
        if (neighbour==null) throw new IllegalStateException("Tile's neighbour is null!");// Do usunięcia.
        if (!neighbour.isTile()) // "Pierwszy" sąsiad powinien (jeśli to możliwe) być płytką
            return getNeighbour(otherEnd(direction));
        return neighbour;
    }

    @Override
    public boolean isTile()
    {
        return true;
    }

    // Sprawdza zgodnośc kolorów z sąsiadem. Służy wyrzucaniu wyjątków (na czas testowania).
    @Override
    protected void fireAppend(int direction, Cell cell)
    {
        if (cell.isTile())
        {
            Tile other = (Tile) cell;
            if (other.getColor(direction^2)!=getColor(direction))
                //throw new ColorMismatchException("Color mismatch near " + cell.getPosition()); // Może dodać taką klasę wyjątków?
            throw new IllegalStateException("Color mismatch near " + cell.getPosition());
        }
    }

    /** Ta metoda przelicza numer kierunku, na drugi kierunek,
     *  w których odchodzi linia danego koloru. (Wskazuje drugi koniec linii.)
     */
    private int otherEnd(int direction)
    {
        if (type%5==0) return (direction ^2); // Linie proste [+] przerzucają na drugą stronę
        if (type%9==3) return (direction ^1); // Zakręty "typu backslash" [\]
        return (direction ^3); // Zakręty "typu slash" [/]
    }

}
