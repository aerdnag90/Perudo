package it.gluckcorp.games.perudo.gameplay.bets;

public class BetFactory {

    public static SimpleBet createSimpleBet(long playerId, int amount, int face) {
        return new SimpleBet(playerId, amount, face);
    }

    public static BaseBet createStarterBet(int amount, int face) throws BaseBet.InvalidBet {
        return new StarterBet(amount, face);
    }

    public static BaseBet createStarterPalificBet(int amount, int face) throws BaseBet.InvalidBet {
        return new StarterPalificBet(amount, face);
    }

    public static Bet createBet(int amount, int face) {
        return new Bet(amount, face);
    }
}
