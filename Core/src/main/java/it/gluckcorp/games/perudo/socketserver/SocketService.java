package it.gluckcorp.games.perudo.socketserver;

import it.gluckcorp.games.perudo.gameplay.Perudo;

import java.net.Socket;

public interface SocketService {
    void connectPlayer(Socket s);
    Perudo prepareTable();
}
