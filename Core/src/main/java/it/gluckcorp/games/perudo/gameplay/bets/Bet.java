package it.gluckcorp.games.perudo.gameplay.bets;

public class Bet {
    int amount;
    int face;

    Bet(int amount, int face) {
        if (amount < 1)
            throw new InvalidBet("Bet amount must be greater than zero!");
        else if (face < 1 || face > 6)
            throw new InvalidBet("Dice face is a number between 1 and 6!");

        this.amount = amount;
        this.face = face;
    }

    public int getAmount() {
        return amount;
    }

    public int getFace() {
        return face;
    }

    public static class InvalidBet extends RuntimeException {
        public InvalidBet() {
            super();
        }

        public InvalidBet(String message) {
            super(message);
        }
    }
}
