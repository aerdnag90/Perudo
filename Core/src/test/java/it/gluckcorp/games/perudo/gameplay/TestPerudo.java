package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.Bet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPerudo {
    private Perudo p;
    private InterfaceSpy gameInterface;

    private void initializeGame(int howManyPlayers, int howManyDices) {
        List<String> players = new ArrayList<>();
        for(int i=0; i<howManyPlayers; i++)
            players.add("Player"+(i+1));

        p.initGame(players, howManyDices);
    }

    @Before
    public void setUp() {
        p = new Perudo();
        gameInterface = new InterfaceSpy();
        p.gameInterface = gameInterface;
    }

    @Test
    public void canRetrieveOrderedPlayerIds() {
        initializeGame(3, 6);
        long[] pIds = p.getPlayerIds();
        assertEquals(3, pIds.length);
    }

    @Test
    public void canSetStartingPlayer() {
        initializeGame(4, 5);
        long pId = p.getPlayerIds()[3];

        p.setStartingPlayer(pId);

        assertEquals(pId, p.getNextPlayerId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenNoBetIsPlaced_callingDoubtWouldThrowInvalidAction() {
        initializeGame(3, 5);
        p.doubtCurrentBet(p.getNextPlayerId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenNoBetIsPlaced_callingFitWouldThrowInvalidAction() {
        initializeGame(4, 5);
        p.fitCurrentBet(p.getNextPlayerId());
    }

    @Test
    public void whenInvalidBetIsReceived_ThrowsInvalidBet() {
        initializeGame(2, 5);
        p.putNewBet(p.getPlayerById(p.getNextPlayerId()), new int[]{3, 2});
        p.putNewBet(p.getPlayerById(p.getNextPlayerId()), new int[]{2, 5});

        assertTrue(gameInterface.calledPromptInvalidBet);
    }

    private class InterfaceSpy implements Console {
        boolean calledPromptInvalidBet = false;

        @Override
        public int[] getFirstBet(String player, List<Integer> dices, boolean palificRound) {
            return new int[0];
        }

        @Override
        public int[] getNextBet(String player, List<Integer> dices) {
            return new int[0];
        }

        @Override
        public void showCurrentBet(Bet currentBet) {

        }

        @Override
        public void announceGameOver(String winningPlayer) {

        }

        @Override
        public void announcePlayerOut(String deadPlayer) {

        }

        @Override
        public void announceRoundOver() {

        }

        @Override
        public void promptInvalidBet(String player, String cause) {
            calledPromptInvalidBet = true;
        }

        @Override
        public void announceRoundSummary(List<Integer> roundDices, Bet bet, String loosingPlayer) {

        }
    }
}
