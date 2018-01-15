package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.Bet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestPerudo {
    private Perudo p;

    private void initializeGame(int howManyPlayers, int howManyDices) {
        List<String> players = new ArrayList<>();
        for(int i=0; i<howManyPlayers; i++)
            players.add("Player"+(i+1));

        p.initGame(players, howManyDices);
    }

    @Before
    public void setUp() {
        p = new Perudo();
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

    @Test(expected = Bet.InvalidBet.class)
    public void whenInvalidBetIsReceived_ThrowsInvalidBet() {
        initializeGame(2, 5);
        p.putNewBet(p.getPlayerById(p.getNextPlayerId()), new int[]{3, 2});
        p.putNewBet(p.getPlayerById(p.getNextPlayerId()), new int[]{2, 5});
    }


}
