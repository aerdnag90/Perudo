package it.gluckcorp.games.perudo.interfaces;

import it.gluckcorp.games.perudo.gameplay.Console;
import it.gluckcorp.games.perudo.gameplay.bets.Bet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class SocketInterface implements Console {
    private Map<String, Socket> players;

    public SocketInterface(Map<String, Socket> players) {
        this.players = players;
    }

    @Override
    public int[] getFirstBet(String player, List<Integer> dices, boolean palificRound) {
        String msg = "%s place your bet. %s\nYour dices are: %s\n";
        writeToPlayer(player, String.format(msg, player, palificRound ? "This is a palifico Round" : "", dices));

        Socket s = players.get(player);

        int amount;
        int face;
        try {
            amount = readInt("Amount: ", s);
            face = readInt("Face: ", s);
        } catch (IOException e) {
            e.printStackTrace();
            return null;    // Is This a Doubt ???
        }

        return new int[]{amount, face};
    }

    @Override
    public int[] getNextBet(String player, List<Integer> dices) {
        String msg = "%s Place Your Bet - [0 to doubt currentBet].\nYour dices are: %s\n";
        writeToPlayer(player, String.format(msg, player, dices));

        Socket s = players.get(player);

        int amount = 0;
        int face = 0;
        try {
            amount = readInt("Amount: ", s);
            if (amount == 0)
                return null;
            face = readInt("Face: ", s);
            if (face == 0)
                return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new int[]{amount, face};
    }

    @Override
    public void showCurrentBet(Bet currentBet) {
        writeToAllOtherPlayers("", "Current Bet: "+extractFormattedBet(currentBet)+"\n");
    }

    @Override
    public void announceGameOver(String winningPlayer) {
        writeToPlayer(winningPlayer, "°°*** You are the Winner ***°°");
        removePlayer(winningPlayer);
    }

    @Override
    public void announcePlayerOut(String deadPlayer) {
        writeToPlayer(deadPlayer, "You Lost!\n");
        writeToAllOtherPlayers(deadPlayer, deadPlayer + " lost all his dices!\n");
        removePlayer(deadPlayer);
    }

    private void removePlayer(String player) {
        try {
            final Socket socket = players.get(player);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        players.remove(player);
    }

    @Override
    public void announceRoundOver() {
        writeToAllOtherPlayers("", "---- Round Over ----\n--------------------\n\n");
    }

    @Override
    public void announceRoundSummary(List<Integer> roundDices, Bet bet, String loosingPlayer) {
        String summary = "\nRound Summary\nAll Dices: %s\nFinal Bet: %s\nLoosing Player: %s\n";
        writeToAllOtherPlayers("", String.format(summary,
                roundDices, extractFormattedBet(bet), loosingPlayer)
        );
    }

    private String extractFormattedBet(Bet bet) {
        return String.format("Amount: %d, Face: %d",
                bet.getAmount(), bet.getFace());
    }

    @Override
    public void promptInvalidBet(String player, String cause) {
        writeToPlayer(player, cause+"\n");
    }

    private void writeToAllOtherPlayers(String excludedPlayer, String message) {
        for (String player : players.keySet()) {
            if (!player.equals(excludedPlayer))
                writeToPlayer(player, message);
        }
    }

    private void writeToPlayer(String player, String message) {
        OutputStream os;
        try {
            os = players.get(player).getOutputStream();
            os.write(message.getBytes());
        } catch (IOException e) {
            System.out.println("Cannot contact player " + player);
            e.printStackTrace();
        }
    }

    private int readInt(String retryMsg, Socket socket) throws IOException {
        Integer value = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream os = socket.getOutputStream();

        os.write((retryMsg).getBytes());
        while (value == null) {
            try {
                String read = br.readLine();
                value = Integer.parseInt(read);
            } catch (Exception e) {
                os.write(("Invalid Number Format.\n" + retryMsg).getBytes());
            }
        }

        return value;
    }
}
