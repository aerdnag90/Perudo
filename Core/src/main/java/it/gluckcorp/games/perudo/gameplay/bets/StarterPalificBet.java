package it.gluckcorp.games.perudo.gameplay.bets;

public class StarterPalificBet extends BaseBet {

    StarterPalificBet(int amount, int face) throws InvalidBet {
        super(amount, face);
        faceProtected = true;
    }

}
