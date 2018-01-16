package it.gluckcorp.games.perudo.interfaces;

import it.gluckcorp.games.perudo.gameplay.Console;
import it.gluckcorp.games.perudo.gameplay.bets.Bet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class ConsoleInterface implements Console {
    private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    ConsoleInterface() {
        printWelcome();
    }

    public void showCurrentBet(Bet currentBet) {
        System.out.println("Current active bet: \n" + extractFormattedBet(currentBet) + "\n");
    }

    private String extractFormattedBet(Bet bet) {
        return String.format("Amount: %d, Face: %d",
                bet.getAmount(), bet.getFace());
    }

    public int[] getFirstBet(String player, List<Integer> dices, boolean palificRound) {
        String msg = "%s place your bet. %s\nYour dices are: %s";
        System.out.println(String.format(msg,
                player,
                palificRound ? "This is a palifico Round" : "",
                dices)
        );
        int amount = readInt("Amount: ");
        int face = readInt("Face: ");

        return new int[]{amount, face};
    }

    public int[] getNextBet(String player, List<Integer> dices) {
        System.out.println(player + " Place Your Bet - [0 to doubt currentBet]. Your dices are: " + dices);

        int amount = readInt("Amount: ");
        if (amount == 0)
            return null;

        int face = readInt("Face: ");
        if (face == 0)
            return null;

        return new int[]{amount, face};
    }

    public void announceRoundSummary(List<Integer> roundDices, Bet bet, String loosingPlayer) {
        String summary = "\nRound Summary\nAll Dices: %s\nFinal Bet: %s\nLoosing Player: %s\n";
        System.out.println(String.format(summary,
                roundDices, extractFormattedBet(bet), loosingPlayer)
        );
    }

    public void announceRoundOver() {
        System.out.println("---- Round Over ----\n--------------------\n");
    }

    public void announcePlayerOut(String player) {
        System.out.println(player + ": Sorry Man you're out");
    }

    public void announceGameOver(String winningPlayer) {
        System.out.println("------ GAME OVER ------");
        System.out.println(winningPlayer + " Wins the game!!!");
    }

    public void promptInvalidBet(String player, String cause) {
        System.out.println(cause);
    }

    private void printWelcome() {
        System.out.println(  "################# GLUCKCORP PRESENTS #################");
        System.out.println("\n####################### PERUDO #######################");
    }

    public List<String> getPlayerNames() {
        int amount = readInt("How Many Players?");

        List<String> names = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String name = readString("Player" + (i+1) + " nickname?");

            if (!names.contains(name))
                names.add(name);
            else {
                System.out.println("Sorry, this nickname is already taken");
                i--;
            }
        }

        return names;
    }

    public int getDicesPerPlayer() {
        Integer value = null;

        while (value == null) {
            int read = readInt("How many dices per players?");
            if (read > 0)
                value = read;
            else
                System.out.println("At least One dice is required to play the game!");
        }

        return value;
    }

    private int readInt(String retryMsg) {
        Integer value = null;

        System.out.println(retryMsg + ":");
        while (value == null) {
            try {
                String read = br.readLine();
                value = Integer.parseInt(read);
            } catch (Exception e) {
                System.out.println("Invalid Number Format.\n" + retryMsg + ":");
            }
        }

        return value;
    }

    private String readString(String retryMsg) {
        String value = null;

        System.out.println(retryMsg + ":");
        while (value == null) {
            try {
                String read = br.readLine();
                if (!read.matches("[\\s\\t\\n]*"))
                    value = read.trim();
                else
                    System.out.println("Invalid Input.\n" + retryMsg + ":");
            } catch (IOException e) {
                System.out.println("Invalid Input.\n" + retryMsg + ":");
            }
        }

        return value;
    }
}
