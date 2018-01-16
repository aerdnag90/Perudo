package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.*;
import it.gluckcorp.games.perudo.gameplay.players.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.gluckcorp.games.perudo.gameplay.bets.BetFactory.createBet;
import static it.gluckcorp.games.perudo.gameplay.bets.BetFactory.createSimpleBet;
import static org.junit.Assert.*;

public class TestRoundController {
    private RoundController controller;
    private PlayerSpy player1, player2, player3;

    @Before
    public void setUp() {
        player1 = new PlayerSpy(111L);
        player2 = new PlayerSpy(222L);
        player3 = new PlayerSpy(333L);

        List<Player>  players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        controller = RoundController.SetupNewRound(players);
    }

    @Test
    public void canCreateNewRoundControllerStartingFromAListOfPlayers() {
        assertNotNull(controller);
    }

    @Test
    public void canExposeOrderedPlayerIds() {
        long[] pIds = controller.getAllPlayerIds();

        assertNotNull(pIds);
        assertEquals(3, pIds.length);

        for (long pId : pIds) assertNotNull(controller.getPlayer(pId));
    }

    @Test
    public void canSetNextPlayer_OrderIsMaintained() {
        long[] pIds = controller.getAllPlayerIds();

        assertEquals(pIds[0], controller.getNextPlayerId());

        controller.setNextPlayer(pIds[1]);
        assertEquals(pIds[1], controller.getNextPlayerId());
    }

    @Test
    public void canSayWhichPlayerIsNextToBet() {
        assertEquals(player1.getId(), controller.getNextPlayerId());
    }

    @Test
    public void canExposeCurrentActiveBet() {
        assertNull(controller.getCurrentBet());
    }

    @Test
    public void canFetchPlayerById() {
        assertNotNull(controller.getPlayer(player1.getId()));
    }

    @Test
    public void ifPlayerIdDoesntExists_GetPlayerReturnsNull() {
        assertNull(controller.getPlayer(2387568576L));
    }

    @Test
    public void canPutNewBet() {
        controller.putNewBet(createSimpleBet(player1.getId(), 2, 5));

        assertNotNull(controller.getCurrentBet());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void firstBetMustComeFromPlayerAtTheTop_ElseThrowsInvalidAction() {
        controller.putNewBet(createSimpleBet(player2.getId(), 2, 5));
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void everyBetMustComeFromPlayerAtTheTop_ElseThrowsInvalidAction() {
        controller.putNewBet(createSimpleBet(player1.getId(), 2, 5));
        controller.putNewBet(createSimpleBet(player3.getId(), 3, 5));
    }

    @Test
    public void whenNewValidBetIsPlaced_CurrentBetIsUpdated() {
        controller.putNewBet(createSimpleBet(player1.getId(), 2, 5));

        assertEquals(2, controller.getCurrentBet().getAmount());
        assertEquals(5, controller.getCurrentBet().getFace());

        controller.putNewBet(createSimpleBet(player2.getId(), 3, 6));

        assertEquals(3, controller.getCurrentBet().getAmount());
        assertEquals(6, controller.getCurrentBet().getFace());
    }

    @Test
    public void whenNewInvalidBetIsPlaced_CurrentBetIsNotUpdated_ThrowsInvalidBet() {
        controller.putNewBet(createSimpleBet(player1.getId(), 2, 4));

        boolean exceptionThrown = false;
        try {
            controller.putNewBet(createSimpleBet(player2.getId(), 2, 3));
        } catch (Exception e) {
            exceptionThrown = true;
            assertTrue(e instanceof Bet.InvalidBet);
            assertEquals(2, controller.getCurrentBet().getAmount());
            assertEquals(4, controller.getCurrentBet().getFace());
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void whenNewValidBetIsPlaced_NextToBetIsSecondPlayer() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 5));
        checkNextPlayerThenPlaceBet(player2, createBet(3, 5));
        checkNextPlayerThenPlaceBet(player3, createBet(4, 5));
        checkNextPlayerThenPlaceBet(player1, createBet(5, 5));
    }

    private void checkNextPlayerThenPlaceBet(Player p, Bet bet) {
        assertEquals(p.getId(), controller.getNextPlayerId());
        controller.putNewBet(createSimpleBet(p.getId(), bet.getAmount(), bet.getFace()));
    }

    @Test
    public void whenNewInvalidBetIsPlaced_PlayerOrderIsUnchanged() {
        assertNull(controller.getCurrentBet());

        boolean exceptionThrown = false;
        try {
            checkNextPlayerThenPlaceBet(player1, createBet(2, 1));
        } catch (Exception e) {
            assertTrue(e instanceof Bet.InvalidBet);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        assertNull(controller.getCurrentBet());
        assertEquals(player1.getId(), controller.getNextPlayerId());
    }

    @Test
    public void whenDoubtIsCalled_TotalNumberOfDicesIsDecreasedByOne() {
        assertEquals(15, controller.getAllDices().size());

        checkNextPlayerThenPlaceBet(player1, createBet(2, 5));
        controller.doubtCurrentBet(player2.getId());

        assertEquals(14, controller.getAllDices().size());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void onlyNextPlayerCanDoubt_anyOtherPlayerIdThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(player1, createBet(1, 3));

        assertEquals(player2.getId(), controller.getNextPlayerId());
        controller.doubtCurrentBet(player3.getId());
    }

    @Test
    public void playerDoubtingValidBetLosesOneDiceAndIsUpToNext() {
        checkNextPlayerThenPlaceBet(player1, createBet(1, 5));

        long doubtingPlayerId = player2.getId();
        controller.doubtCurrentBet(doubtingPlayerId);

        assertEquals(14, controller.getAllDices().size());
        assertEquals(4, controller.getPlayer(doubtingPlayerId).getDices().size());
        assertEquals(doubtingPlayerId, controller.getNextPlayerId());
    }

    @Test
    public void whenBetIsDoubted_PlayerWhoPutsWrongBetLosesOneDiceAndIsUpToNext() {
        checkNextPlayerThenPlaceBet(player1, createBet(6, 4));
        controller.doubtCurrentBet(player2.getId());

        long loosingPID = player1.getId();

        assertEquals(14, controller.getAllDices().size());
        assertEquals(4, controller.getPlayer(loosingPID).getDices().size());
        assertEquals(loosingPID, controller.getNextPlayerId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void playerWhoPlacedCurrentBetCannotFit_ThrowsInvalidAction() {
        player1.removeDice();

        checkNextPlayerThenPlaceBet(player1, createBet(4, 5));
        controller.fitCurrentBet(player1.getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void playerWhoReceivesCurrentBetCannotFit_ThrowsInvalidAction() {
        player2.removeDice();

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.fitCurrentBet(player2.getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenFitCurrentBetIsCalledWithInvalidPlayerId_ThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 5));
        controller.fitCurrentBet(2873562986592639L);
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void onlyPlayersWithDiceSetNotFullCanFitCurrentBet_ThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.fitCurrentBet(player3.getId());
    }

    @Test
    public void playerFittingCurrentValidBet_ObtainsOneDiceAndIsUpToNext() {
        player1.removeDice();
        controller.setNextPlayer(player2.id);

        checkNextPlayerThenPlaceBet(player2, createBet(3,3));
        controller.fitCurrentBet(player1.getId());

        assertEquals(15, controller.getAllDices().size());
        assertEquals(5, player1.getDices().size());
        assertEquals(player1.getId(), controller.getNextPlayerId());
    }

    @Test
    public void playerFittingCurrentInvalidBet_LosesOneDiceAndIsUpToNext() {
        player1.removeDice();
        controller.setNextPlayer(player2.id);

        checkNextPlayerThenPlaceBet(player2, createBet(1,3));
        controller.fitCurrentBet(player1.getId());

        assertEquals(13, controller.getAllDices().size());
        assertEquals(3, player1.getDices().size());
        assertEquals(player1.getId(), controller.getNextPlayerId());
    }

    @Test
    public void afterDoubtIsCalled_RoundIsOver() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 4));
        controller.doubtCurrentBet(player2.getId());

        assertTrue(controller.isRoundOver());
    }

    @Test
    public void afterFitIsCalled_RoundIsOver() {
        player3.removeDice();

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.fitCurrentBet(player3.getId());

        assertTrue(controller.isRoundOver());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_PutNewBetThrowsInvalidAction() {
        player3.removeDice();

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.fitCurrentBet(player3.getId());

        controller.putNewBet(BetFactory.createSimpleBet(player3.getId(), 4, 5));
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_DoubtCurrentBetThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.doubtCurrentBet(player2.getId());

        controller.doubtCurrentBet(player2.getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_FitCurrentBetThrowsInvalidAction() {
        player3.removeDice();

        checkNextPlayerThenPlaceBet(player1, createBet(1, 4));
        controller.doubtCurrentBet(player2.getId());

        controller.fitCurrentBet(player3.getId());
    }

    @Test
    public void whenRoundIsOver_Doubt_CanSayIfSomePlayerIsOutOfGame() {
        player2.dices = buildNewListOfDices(4);

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.doubtCurrentBet(player2.getId());

        assertTrue(controller.isRoundOver());
        assertTrue(controller.isPlayerOut());
        assertEquals(player2, controller.getDeadPlayer());
    }

    private ArrayList<Integer> buildNewListOfDices(Integer... dices) {
        return new ArrayList<>(Arrays.asList(dices));
    }

    @Test
    public void whenRoundIsOver_Fit_CanSayIfSomePlayerIsOutOfGame() {
        player3.dices = buildNewListOfDices(3);

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.fitCurrentBet(player3.getId());

        assertTrue(controller.isRoundOver());
        assertTrue(controller.isPlayerOut());
        assertEquals(player3, controller.getDeadPlayer());
    }

    @Test
    public void whenRoundIsOver_CallingCleanUpRoundPermitsNextRound() {
        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        controller.doubtCurrentBet(player2.getId());

        assertTrue(controller.isRoundOver());
        controller.cleanUpRound();

        assertFalse(controller.isRoundOver());
        checkNextPlayerThenPlaceBet(player2, createBet(1, 4));
    }

    @Test
    public void whenRoundStartsWithPlayerHavingOneDiceAndNotYetPalific_StarterBetIsPalific() {
        player1.dices = buildNewListOfDices(4);
        checkNextPlayerThenPlaceBet(player1, createBet(1, 1));

        assertTrue(player1.hasBeenPalific());
        assertTrue(controller.getCurrentBet() instanceof StarterPalificBet);
    }

    @Test
    public void onePlayerCannotPutTwoStarterPalificInTheSameGame() {
        player2.dices = buildNewListOfDices(3);
        player2.beenPalific = true;
        controller.setNextPlayer(player2.id);

        checkNextPlayerThenPlaceBet(player2, createBet(2, 3));
        assertTrue(controller.getCurrentBet() instanceof StarterBet);
        assertEquals(1, player2.getDices().size());
        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test
    public void whenBetIsUpdatedFromPlayerHavingOneDice_CurrentBetBecomesFaceProtected() {
        player2.dices = buildNewListOfDices(4);
        player2.beenPalific = true;

        checkNextPlayerThenPlaceBet(player1, createBet(2, 3));
        assertFalse(((BaseBet) controller.getCurrentBet()).isFaceProtected());

        checkNextPlayerThenPlaceBet(player2, createBet(3, 5));
        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test
    public void whenBetIsFaceProtected_PlayerHavingOneDiceCanChangeTheFace() {
        player1.dices = buildNewListOfDices(4);
        player2.dices = buildNewListOfDices(2);

        checkNextPlayerThenPlaceBet(player1, createBet(2, 4));
        assertTrue(controller.getCurrentBet() instanceof StarterPalificBet);

        checkNextPlayerThenPlaceBet(player2, createBet(2, 5));
        assertEquals(2, controller.getCurrentBet().getAmount());
        assertEquals(5, controller.getCurrentBet().getFace());

        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test(expected = Bet.InvalidBet.class)
    public void whenBetIsFaceProtected_PlayerHavingMoreThanOneDiceCannotChangeFace_ThrowsInvalidBet() {
        player2.dices = buildNewListOfDices(4);
        boolean exceptionThrown = false;

        checkNextPlayerThenPlaceBet(player1, createBet(2, 4));
        checkNextPlayerThenPlaceBet(player2, createBet(1, 1));
        try {
            checkNextPlayerThenPlaceBet(player3, createBet(3, 4));
        } catch (Bet.InvalidBet invalidBet) {
            exceptionThrown = true;
            assertEquals(1, controller.getCurrentBet().getAmount());
            assertEquals(1, controller.getCurrentBet().getFace());

            assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
        }

        assertTrue(exceptionThrown);
        throw new Bet.InvalidBet();
    }

    private class PlayerSpy implements Player {
        long id;
        boolean beenPalific = false;
        List<Integer> dices = buildNewListOfDices(2, 3, 4, 5, 6);

        PlayerSpy(long id) {
            this.id = id;
        }

        public void addDice() {
            dices.add(5);
        }

        public void removeDice() {
            dices.remove(0);
        }

        public List<Integer> getDices() {
            return dices;
        }

        public boolean isDiceSetFull() {
            return dices.size() == 5;
        }

        public boolean isDiceSetEmpty() {
            return dices.size() == 0;
        }

        public boolean hasOneDice() {
            return dices.size() == 1;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return "";
        }

        public void setName(String name) {

        }

        public boolean hasBeenPalific() {
            return beenPalific;
        }

        public void setPalificOption() {
            beenPalific = true;
        }

        public void shuffleDices() {

        }
    }
}
