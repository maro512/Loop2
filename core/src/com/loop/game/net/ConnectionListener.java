/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.loop.game.net;

/**
 * Interfejs nasłuchujący odpowiedzi serwera/drugiego gracza w ramach komunikacji sieciowej.
 * Pytania do serwera wysyłamy przez obiekt <code>Client</code>.
 * UWAGA. Zdarzenia są, co do zasady, wywoływane przez osobne wątki. Należy na to uważać.
 * Created by Piotr on 2017-05-13
 */
public interface ConnectionListener
{
    /** Przetwarza komunikat z zewnątrz, podzielony na argumenty. Zwraca, czy nie wystąpił błąd. */
    boolean processCommand(String[] command);
    
    /** Zdarzenie przerwania połączenia.
     * Parametr oznacza, czy przerwano połączenie z drugim graczem (false), czy z serwerem (true). */
    void connectionDown(boolean server);
    
    /** Wywoływana by powiadomić o powodzeniu/klęsce ostatnij akcji zainicjowanej przez użytkownika. */
    void done(boolean success);
}
