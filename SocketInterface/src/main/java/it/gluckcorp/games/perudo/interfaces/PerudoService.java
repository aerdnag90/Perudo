package it.gluckcorp.games.perudo.interfaces;

import it.gluckcorp.games.perudo.gameplay.Perudo;
import it.gluckcorp.games.perudo.socketserver.SocketService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerudoService implements SocketService {
    private Map<String, Socket> users = new ConcurrentHashMap<>();

    public void connectPlayer(Socket s) {
        try {
            OutputStream os = s.getOutputStream();
            os.write("Hello Player, Welcome to Perudo Game.\n".getBytes());

            String nickname = getPlayerName(s);
            if (nickname != null) {
                synchronized (this) {
                    users.put(nickname, s);
                }
                os.write(("Hi " + nickname + "! Waiting for other players to join the game...\n").getBytes());
                System.out.println(nickname + " joined the game! Actually " + users.size() + " users are ready to play.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPlayerName(Socket s) {
        BufferedReader reader;
        OutputStream os;

        try {
            final String retryMsg = "Choose your nickname:\n";
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = s.getOutputStream();

            while (true) {
                os.write(retryMsg.getBytes());
                String read = reader.readLine();

                if (!read.matches("[\\s\\t\\n]*")) {
                    String name = read.trim();
                    synchronized (this) {
                        if (!users.keySet().contains(name))
                            return name;
                        else
                            os.write("Sorry, this nickname is already taken\n".getBytes());
                    }
                } else {
                    os.write(("Invalid Input. " + retryMsg).getBytes());
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    public Perudo prepareTable() {
        waitForPlayers();

        Map<String, Socket> players;
        synchronized (this) {
            players = new HashMap<>(users);
            users.clear();
        }

        String msg = "\n\nStarting new Perudo Table with %d players\n";
        broadcast(String.format(msg, players.size()), players.values());

        Perudo game = new Perudo(new ArrayList<>(players.keySet()), 5);
        game.gameInterface = new SocketInterface(players);

        return game;
    }

    private void waitForPlayers() {
        while (users.size() < 2) {
            try {
                Thread.sleep(30000);
                if (users.size() > 0)
                    broadcast("\nWaiting for other players!", users.values());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized (this) {
            users.values().removeIf(s -> s.isClosed() || !s.isConnected());
        }

        if (users.size() < 2)
            waitForPlayers();
    }

    private void broadcast(String msg, Collection<Socket> sockets) {
        for (Socket s : sockets) {
            try {
                s.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                System.out.println("Cannot broadcast msg to socket: " + e.getMessage());
            }
        }
    }

}
