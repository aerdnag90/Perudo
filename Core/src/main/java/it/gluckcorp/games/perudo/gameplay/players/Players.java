package it.gluckcorp.games.perudo.gameplay.players;

public class Players {
    public static Player CreatePlayerWithDiceSet(int diceSetMaxSize) {
        if (diceSetMaxSize < 1)
            throw new Player.InvalidPlayer();

        return new PlayerImpl(diceSetMaxSize);
    }

    public static Player CreatePlayerWithDiceSet(String name, int dicesPerPlayers) {
        Player p = CreatePlayerWithDiceSet(dicesPerPlayers);
        p.setName(name);

        return p;
    }

    public static Player CreatePlayerWithDices(Integer[] dices) {
        return new PlayerImpl(dices);
    }
}
