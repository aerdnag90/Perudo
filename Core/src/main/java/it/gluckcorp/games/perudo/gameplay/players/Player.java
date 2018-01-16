package it.gluckcorp.games.perudo.gameplay.players;

import java.util.List;

public interface Player {
    void addDice();

    void removeDice();

    List<Integer> getDices();

    boolean isDiceSetFull();

    boolean isDiceSetEmpty();

    boolean hasOneDice();

    long getId();

    String getName();

    void setName(String name);

    boolean hasBeenPalific();

    void setPalificOption();

    void shuffleDices();

    public static class DicesOverflow extends RuntimeException {
    }

    public static class DicesUnderflow extends RuntimeException {
    }

    public static class InvalidPlayer extends RuntimeException {
    }
}
