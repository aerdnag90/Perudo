package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.Bet;

import java.util.List;

public interface Console {

    int[] getFirstBet(String player, List<Integer> dices, boolean palificRound);

    int[] getNextBet(String player, List<Integer> dices);

    void showCurrentBet(Bet currentBet);    // WRONG

    void announceGameOver(String winningPlayer);

    void announcePlayerOut(String deadPlayer);

    void announceRoundOver();

    void promptInvalidBet(String player, String cause);

    void announceRoundSummary(List<Integer> roundDices, Bet bet, String loosingPlayer);
}
