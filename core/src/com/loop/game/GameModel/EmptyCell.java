package com.loop.game.GameModel;

/**
 * Klasa reprezentująca wolne pole na planszy. Udostepnia informacje o pasujacych
 * w tym miejscu płytkach, oraz umożliwia utworzenie płytki i dowiązanie jej do grafu.
 * Created by Piotr on 29.04.17
 */

public class EmptyCell extends Cell
{
    public EmptyCell(Position pos)
    {
        super(pos);
        knownWhite=knownBlack=0;
        dead=false;
    }

    private byte knownWhite, knownBlack;
    private boolean dead;

    /** Tworzy płytkę zdeterminowaną. */
    Tile createTile()
    {
        int black = countBits(knownBlack);
        int white = countBits(knownWhite);
        if (black<2 && white<2) throw new IllegalArgumentException("Cell is not determined!");
        // if ((knownWhite & knownBlack)!=0) throw new IllegalStateException("Unbelievable overlap of black and white line!");
        if (black>2 || white>2) // Trzy linie tego samego koloru?
            return null; // Nie istnieje taka płytka!
        byte type = black==2 ? knownBlack : (byte) (~knownWhite & 15);
        return createTile(type);
    }

    /** Wstawia płytkę danego typu. */
    Tile createTile(byte type)
    {
        if (!tileFits(type)) throw new IllegalArgumentException("The given tile type does not fit.");
        Tile tile =  new Tile(getPosition(),type);
        replaceMe(tile);
        return tile;
    }

    public boolean tileFits(byte type)
    {
        // "type" to końce linii czarnej, a "~type" to końce linii białej
        return countBits(type)==2 && (type & knownWhite) == 0 && (~type & knownBlack)==0;
    }

    // Pole zdeterminowane to takie na które pasuje conajwyżej jedna płytka.
    public boolean isDetermined()
    {
        return dead || countBits(knownBlack)>1 || countBits(knownWhite) >1;
    }

    /*  MARTWE POLE to takie, na które nie pasuje żadna płytka. Board nie udostępnia martwych pól,
    ale ich też nie odpina od grafu, ponieważ płytki nie powinny mieć sąsiadów null. */
    public boolean isDead() { return dead; }

    //Tutaj reagujemy na zmianę sąsiada.
    @Override
    protected void fireAppend(int direction, Cell cell)
    {
        if(!dead && cell.getType()>0)
        {
            Tile tile = (Tile) cell;
            if(tile.getColor(direction ^2))
            {
                knownWhite &= 15^ Cell.mask[direction]; // Skasuj ewnetualny biały
                knownBlack |= Cell.mask[direction]; // Dopisz czarny
            }
            else
            {
                knownWhite |= Cell.mask[direction]; // Dopisz biały
                knownBlack &= 15^ Cell.mask[direction]; // Skasuj ewentualny czarny
            }
            dead = countBits(knownBlack)>2 || countBits(knownWhite) >2;
        }
    }

    @Override
    public byte getType()
    {
        return dead ? (byte) -1 : 0;
    }

    private static int countBits(byte val)
    {
        return (val & 1) + ((val & 2) >> 1) + ((val & 4) >> 2) + ((val & 8) >> 3);
    }
}