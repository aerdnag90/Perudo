package it.gluckcorp.games.perudo.gameplay.players;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private List<Integer> dices;
    private String name = "";
    private double maxDiceSet;
    private long id;
    private boolean hasBeenPalific;

    public static Player CreatePlayerWithDiceSet(int diceSetMaxSize) {
        if (diceSetMaxSize < 1)
            throw new InvalidPlayer();

        return new Player(diceSetMaxSize);
    }

    public static Player CreatePlayerWithDiceSet(String name, int dicesPerPlayers) {
        Player p = CreatePlayerWithDiceSet(dicesPerPlayers);
        p.name = name;

        return p;
    }

    private Player(int diceSetMaxSize) {
        dices = new ArrayList<>();
        maxDiceSet = diceSetMaxSize;

        final Random random = new Random();

        while (diceSetMaxSize-- > 0)
            dices.add(random.nextInt(6) + 1);

        id = random.nextLong();
    }

    public void addDice() {
        if (dices.size() == maxDiceSet)
            throw new DicesOverflow();

        dices.add(1);
    }

    public void removeDice() {
        if (dices.size() == 0)
            throw new DicesUnderflow();

        dices.remove(0);
    }

    public int getDiceCount() {
        return dices.size();
    }

    public List<Integer> getDices() {
        return dices;
    }

    public boolean isDiceSetFull() {
        return !(dices.size() < maxDiceSet);
    }

    public boolean isDiceSetEmpty() {
        return dices.isEmpty();
    }

    public boolean hasOneDice() {
        return dices.size() == 1;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasBeenPalific() {
        return hasBeenPalific;
    }

    public void setPalificOption() {
        hasBeenPalific = true;
    }

    public void shuffleDices() {
        int deckSize = dices.size();
        dices.clear();

        final Random random = new Random();
        while (deckSize-- > 0)
            dices.add(random.nextInt(6) + 1);
    }

    public static class DicesOverflow extends RuntimeException {
    }

    public static class DicesUnderflow extends RuntimeException {
    }

    public static class InvalidPlayer extends RuntimeException {
    }
}
