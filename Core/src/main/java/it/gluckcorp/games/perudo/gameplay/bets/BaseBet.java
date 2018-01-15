package it.gluckcorp.games.perudo.gameplay.bets;

import java.util.List;

public class BaseBet extends Bet {
    boolean faceProtected;

    BaseBet(int amount, int face) throws InvalidBet {
        super(amount, face);
    }

    public void updateBet(Bet newBet) throws InvalidBet {
        checkBetValidity(newBet);

        this.amount = newBet.amount;
        this.face = newBet.face;
    }

    protected void checkBetValidity(Bet newBet) throws InvalidBet {
        if (newBet.amount < amount)
            throw new InvalidBet("Cannot bet lower amount!");

        else if (newBet.amount == amount) {
            if (newBet.face <= face)
                throw new InvalidBet("Cannot bet same amount with lower or equal face!");
            else if (isFaceProtected())
                throw new InvalidBet("You are not allowed to change bet's face!");
        }

        else {
            if (newBet.face < face)
                throw new InvalidBet("Cannot bet on lower face!");
            else if (newBet.face > face && isFaceProtected())
                throw new InvalidBet("You are not allowed to change bet's face!");
        }
    }

    public boolean isLessOrEqual(List<Integer> dices) {
        return amount <= countDicesValidForThisBet(dices);
    }

    public boolean isExact(List<Integer> dices) {
        return amount == countDicesValidForThisBet(dices);
    }

    protected int countDicesValidForThisBet(List<Integer> dices) {
        int counter = 0;
        for (Integer dice : dices)
            if (dice == face)
                counter++;

        return counter;
    }

    public boolean isFaceProtected() {
        return faceProtected;
    }

    public void setFaceProtected(boolean faceProtected) {
        this.faceProtected = faceProtected;
    }
}
