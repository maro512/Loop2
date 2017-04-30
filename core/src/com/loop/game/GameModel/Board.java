package com.loop.game.GameModel;

import java.util.*;

/**
 * Klas opisująca planszę LOOP. Kontroluje dodawanie płytek i automatyczne uzupełnianie.
 * Ponadto sprawdza powstawanie linii wygrywających.
 * Created by Piotr on 2017-04-15.
 */
public class Board
{
    private Map<BasicPosition, Cell> graph, view;
    private Collection<Cell> bareCells;
    //private boolean whiteWin, blackWin; // Informacja o istnieniu pętli
    private List<Tile> whiteLine, blackLine; // Wygrywające linie;
    private int minX, minY, maxX, maxY; // Wymiary planszy

    public Board()
    {
        graph = new HashMap<BasicPosition, Cell>();
        view = Collections.unmodifiableMap(graph);
        bareCells = view.values();
        minimalWinningLength=8;
        Cell first = new EmptyCell( new Position(0,0) );
        graph.put(first.getPosition(),first );
        //whiteWin= blackWin= false; // Istnienie zwycięskich linii
        minX=minY=maxX=maxY=0;
    }

    /* Metody zwracające skrajne współrzędne płytek na planszy. */
    public int getMinX()
    {
        return minX;
    }
    public int getMinY()
    {
        return -maxY;
    }
    public int getMaxX()
    {
        return maxX;
    }
    public int getMaxY()
    {
        return -minY;
    }

    @Deprecated
    public Map<BasicPosition, Cell> getCrrPosition() { return view; }
    public Collection<Cell> getCells() { return bareCells; }

    // Stała opisująca długość linii wygrywającej niebędącej pętlą.
    private int minimalWinningLength;
    public int getMinimalWinningLength() { return minimalWinningLength; }
    public void setMinimalWinningLength(int minimalWinningLength)
    {
        if (minimalWinningLength<5) throw new IllegalArgumentException("Winning length cannot be less than 5.");
        this.minimalWinningLength = minimalWinningLength;
    }

    /** Zwraca, czy istnieje biała linia wygrywająca. */
    public boolean isWhiteWin()
    {
        return whiteLine!=null;
    }

    /** Zwraca, czy istnieje czarna linia wygrywająca. */
    public boolean isBlackWin()
    {
        return blackLine!=null;
    }

    /** Zwraca linię wygrywającą w podanym kolorze. */
    public List<Tile> getWinningLine(boolean color)
    {
        return color ? blackLine : whiteLine;
    }

    /** Zwraca płytkę (wolne pole) z danej pozycji na planszy. */
    public Cell getCell(int x, int y)
    {
        return graph.get(tempPos.set(x,y));
    }

    /** Zwraca wolne pole znajdujące się na pozycji (x,y).
     * Jeśli nie podanej pozycji nie ma wolnego pola (np. jest płytka), zwraca null.
     */
    public EmptyCell getAvailablePlace(int x, int y)
    {
        BasicPosition p = tempPos.set(x,y);
        Cell place = graph.get(p);
        if (place==null || place.getType()!=0) return null;
        return (EmptyCell) place;
    }

    /** Wstawia płytkę podanego typu na zadane wolne pole.
     * @param place wolne pole (MUSI być w mapie na dobrej pozycji) na które wstawiamy płytkę
     * @param type typ płytki, który musi pasować na podane wolne pole.
     */
    public void setTile(EmptyCell place, byte type)
    {
        if (graph.get(place.getPosition())!=place) throw new IllegalArgumentException("The given position is not on the map!");
        Tile tile = place.createTile(type); // Tworzy płytkę.
        addTile(tile);
    }

    /** Dodaje płytkę na planszy (płytka ma dobrze określonych sąsiadów itd.) i automatycznie
     * uzupełnia miejsca deterministyczne. Ponadto wykonuje sprawdzenie, czy nie powstała pętla (linia wygrywająca)
     * @param tile płytka z dobrze określonymi sąsiadami (najlepiej wygenerowana z EmptyCell)
     */
    private void addTile(Tile tile)
    {
        graph.put(tile.getPosition(),tile); // Dodaj płytkę do mapy (nadpisując poprzednią w tym miejscu)
        for(int dir =0; dir<4; dir++) // Sprawdź reakcje sąsiadów
        {
            Cell cell = tile.getNeighbour(dir);
            if (cell == null) // Brak sąsiada!
            {
                Position next = tile.getPosition().getNeighbour(dir);
                EmptyCell newplace = new EmptyCell(next); // Stwórz nowe wolne pole
                graph.put(next, newplace); // Dodaj pole do listy
                extendBoundary(next); // Uaktualnij "wymiary plaszy"
                for (int dir2 = 0; dir2 < 4; dir2++) // Dopnij sąsiadów do nowego pustego pola
                {
                    Position next2 = next.getNeighbour(dir2); // Pobierz pozycję
                    newplace.append(dir2, graph.get(next2)); // Dodaj sąsiada (pobierz z mapy)
                }
                continue;
            }
            if (cell.getType()>0) continue; // Sąsiad-płytka już się nie zmieni
            EmptyCell place = (EmptyCell) cell;
            if (place.isDetermined()) {
                if (place.isDead()) graph.remove(place.getPosition()); // Błąd! Usuń pole
                else addTile(place.createTile()); // Stwórz i wstaw deterministyczną płytkę.
            }
        }
        //if (debug) System.out.println("Wstawiono plytkę na "+tile.getPosition());
        if (whiteLine==null) whiteLine=getWinningLine(tile, WHITE);
        if (blackLine==null) blackLine=getWinningLine(tile, BLACK);
        //if (debug) System.out.println();
    }

    // Poszerza zakresy współrzędnych, aby podana pozycja się w nich mieściła
    private void extendBoundary(BasicPosition pos)
    {
        if (pos.getX()>maxX) maxX=pos.getX();
        else if (pos.getX()<minX) minX=pos.getX();
        if (pos.getY()>maxY) maxY=pos.getY();
        else if (pos.getY()<minY) minY=pos.getY();
    }

    // Metoda sprawdzająca, czy z płytki nie wychodzi linia wygrywająca w danym kolorze.
    private List<Tile> getWinningLine(Tile t, boolean color)
    {
        //if (debug) System.out.print("\n\tCheckWin("+color+")"+t.getPosition()+"->");
        Cell end1 = t.getColorNeighbour(color); // Pierwszy sąsiad
        if (end1.getType()<=0) return null; // Płytka nie jest połączona tym kolorem z innymi
        Tile end1prev= t;
        Tile end2prev= (Tile) end1;
        LinkedList<Tile> line = new LinkedList<Tile>();
        do // Szukaj pierwszego końca linii
        {
            //if (debug) System.out.print(end1.getPosition()+"->");
            Tile tile2= (Tile) end1;
            line.addLast(tile2);
            end1= tile2.nextOnLine(end1prev); // Kolejna płytka (lub puste pole)
            end1prev=tile2;
            if (end1==t)
            {
                line.addFirst(t);
                return line;
            }
            if (end1==null) return null; // Plansza jeszcze nie gotowa (będzie drugi test w przyszłości!)
        } while (end1.getType()>0);
        //if (debug) System.out.print(end1.getPosition()+" ");
        if (((EmptyCell) end1).isDead()) return null; // Linia dochodząca do błędu nie może wygrać.
        Cell end2 = t; //.getColorNeighbour(color,false);
        do  // Szukaj drugiego końca linii
        {
            //if (debug) System.out.print(end1.getPosition()+"->");
            Tile tile2= (Tile) end2;
            line.addFirst(tile2);
            end2= tile2.nextOnLine(end2prev); // Kolejna płytka (lub puste pole)
            end2prev=tile2;
            if (end2==null) return null; // Plansza jeszcze nie gotowa (będzie drugi test w przyszłości!)
        } while (end2.getType()>0);
        //if (debug) System.out.print(end1.getPosition()+" ");
        if (((EmptyCell) end2).isDead()) return null; // Linia dochodząca do błędu nie może wygrać.
        int end1dir = end1.getDirection(end1prev); // Wyznacz kierunki zakończeń linii
        int end2dir = end2.getDirection(end2prev);
        //if(debug) System.out.printf("Kierunki:%d|%d",end1dir,end2dir);
        if ((end1dir ^ end2dir)!=2) return null;  //Sprawdź przeciwstawność kierunków
        int length= end1.getPosition().distanceInDirection(end2.getPosition(),end1dir);
        //if(debug) System.out.printf("[%d]",length);
        if (length> minimalWinningLength) return line;
        else return null;
    }

    // Taki pomocniczy obiekt do maltretowania. Chyba lepiej go modyfikować niż ciągle tworzyć new Position...
    BasicPosition.Mutable tempPos = new BasicPosition.Mutable();

    public static final boolean BLACK= true;
    public static final boolean WHITE= false;

    //public void useDebugMode() {debug=true; }
    //private boolean debug =false;
}
