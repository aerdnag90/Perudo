package it.gluckcorp.games.perudo.interfaces;

import it.gluckcorp.games.perudo.gameplay.Perudo;
import it.gluckcorp.games.perudo.socketserver.SocketServer;

public class Game {

    public static void main(String arg) {
        final ConsoleInterface gameInterface = new ConsoleInterface();

        Perudo game = new Perudo(gameInterface.getPlayerNames(), gameInterface.getDicesPerPlayer());
        game.gameInterface = gameInterface;
        game.startGame();
    }

    public static void main(String[] args) throws Exception {
        new SocketServer(8011, new PerudoService()).start();
    }

}
