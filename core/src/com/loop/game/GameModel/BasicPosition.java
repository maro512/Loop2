package com.loop.game.GameModel;

/** To jest uniwersalna klasa opisująca współrzędne na planszy LOOP. Służy do indeksowania
 * pól na planszy.
 * Created by Poitr on 2017-04-18
 */
public abstract class BasicPosition
{
    // Dostęp do współrzędnych
    public abstract int getX();
    public abstract int getY();

    @Override
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || !(o instanceof BasicPosition)) return false;
        BasicPosition position = (BasicPosition) o;
        return getY() == position.getY() && getX() == position.getX();
    }

    @Override
    public final int hashCode()
    { // Duża liczba pierwsza zapewnia większa niepowtarzalność, skoro x i y są "bliskie" 0.
        return 65537 * getX() + getY(); // W praktyce będzie to kodowanie 1 do 1 :)
    }

    @Override
    public String toString() // To może się przydać do debugowania
    {
        return String.format("(%3d,%3d)",getX(),getY());
    }

    /** To mała klasa pomocnicza, która ma na celu zmniejszać narzut z niepotrzebnym częstym tworzeniem
     * obiektów Position. Klasa Position musi być niemodyfikowalna, bay zapobiec błędom związanym
     * z przypadkową modyfikacją współrzędnych obiektów klasy Tile i EmptyCell.
     */
    public static class Mutable extends BasicPosition
    {
        private int x=0, y=0;

        public Mutable set(int _x, int _y)
        {
            x=_x; y=_y;
            return this;
        }

        @Override
        public int getX() { return x; }
        @Override
        public int getY() { return y; }
    }
}

/**
 * Created by marek on 11.04.17.
 * Beeing updated by Piotr since 13.04.17.
 */
class Position extends BasicPosition
{
    private int x,y;

    public Position(int xx, int yy) { x=xx; y=yy; }

    /** Metoda dekodująca - tworzy Position na podstawie hashCode
     * Powinna działać, ale trzeba przetestować
     */
    public static Position fromHashCode(int hash)
    {
        hash+= 65537/2;
        int x= hash >0 ? hash/65537 : hash/65537-1;
        int y= hash- x*65537- 65537/2; // poprawiona reszta z dzielenia
        return new Position(x,y);
    }

    // Dostęp do współrzędnych
    public int getX() { return x; }
    public int getY() { return y; }

    public Position getNeighbour (int direction)
    {
        if (direction<0 || direction >3) throw new IllegalArgumentException("Invalid direction.");
        return new Position(
                x+ (1-(direction & 1))*(1-direction),
                y+ (direction & 1)*(direction-2) );
    }

    // Oblicza różnicę współrzędnych w zadanym kierunku.
    public int distanceInDirection(BasicPosition other, int direction)
    {
        switch (direction)
        {
            case NORTH: return other.getY() - y; // other jest (powinien być) powyżej
            case SOUTH: return y - other.getY(); // other jest (powinien być) poniżej
            case EAST : return other.getX() - x; // other jest (powinien być) po prawej
            case WEST : return x - other.getX(); // other jest (powinien być) po lewej
        }
        throw new IllegalArgumentException("Invalid direction.");
    }

    // Stałe opisujące kierunki
    public static final int EAST =0;
    public static final int NORTH=1;
    public static final int WEST =2;
    public static final int SOUTH=3;
}
