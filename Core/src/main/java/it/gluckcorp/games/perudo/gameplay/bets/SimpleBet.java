package it.gluckcorp.games.perudo.gameplay.bets;

public class SimpleBet extends Bet {
    private final long playerId;

    SimpleBet(long playerId, int amount, int face) {
        super(amount, face);
        this.playerId = playerId;
    }

    public long getPlayerId() {
        return playerId;
    }
}
