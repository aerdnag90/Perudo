package it.gluckcorp.games.perudo.gameplay.bets;

import java.util.List;

public class StarterBet extends BaseBet {

    StarterBet(int amount, int face) throws InvalidBet {
        super(amount, face);
        faceProtected = false;

        if (face == 1)
            throw new InvalidBet("Cannot start new bet with face '1'");
    }

    @Override
    protected void checkBetValidity(Bet newBet) throws InvalidBet {
        if (isFaceProtected() && face != newBet.face) {
            throw new InvalidBet("You are not allowed to change bet's face!");
        } else if (face == 1 && newBet.face != 1) {
            if (2 * amount >= newBet.amount)
                throw new InvalidBet("To change bet from face '1' to any other face, the amount should be greater than the double of previous bet's amount!");
        } else if (face != 1 && newBet.face == 1) {
            if ((amount / 2 + (amount % 2 != 0 ? 1 : 0)) > newBet.amount)
                throw new InvalidBet("To change bet to face '1' from any other face, the amount should be greater than half of previous bet's amount!");
        } else {
            super.checkBetValidity(newBet);
        }
    }

    @Override
    protected int countDicesValidForThisBet(List<Integer> dices) {
        int counter = 0;
        for (Integer i : dices)
            if (i == face || i == 1)
                counter++;

        return counter;
    }
}
