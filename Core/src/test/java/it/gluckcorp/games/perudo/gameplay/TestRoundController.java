package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.*;
import it.gluckcorp.games.perudo.gameplay.players.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static it.gluckcorp.games.perudo.gameplay.bets.BetFactory.createBet;
import static it.gluckcorp.games.perudo.gameplay.bets.BetFactory.createSimpleBet;
import static org.junit.Assert.*;

public class TestRoundController {

    private List<Player> players;
    private RoundController controller;

    @Before
    public void setUp() throws Exception {
        players = new ArrayList<>();
        players.add(Player.CreatePlayerWithDiceSet(5));
        players.add(Player.CreatePlayerWithDiceSet(5));
        players.add(Player.CreatePlayerWithDiceSet(5));
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
        assertEquals(players.get(0).getId(), controller.getNextPlayerId());
    }

    @Test
    public void canExposeCurrentActiveBet() {
        assertNull(controller.getCurrentBet());
    }

    @Test
    public void canFetchPlayerById() {
        assertNotNull(controller.getPlayer(players.get(0).getId()));
    }

    @Test
    public void ifPlayerIdDoesntExists_GetPlayerReturnsNull() {
        assertNull(controller.getPlayer(2387568576L));
    }

    @Test
    public void canPutNewBet() {
        controller.putNewBet(createSimpleBet(players.get(0).getId(), 2, 5));

        assertNotNull(controller.getCurrentBet());
        assertTrue(controller.getCurrentBet() instanceof StarterBet);
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void firstBetMustComeFromPlayerAtTheTop_ElseThrowsInvalidAction() {
        controller.putNewBet(createSimpleBet(players.get(1).getId(), 2, 5));
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void everyBetMustComeFromPlayerAtTheTop_ElseThrowsInvalidAction() {
        controller.putNewBet(createSimpleBet(players.get(0).getId(), 2, 5));
        controller.putNewBet(createSimpleBet(players.get(2).getId(), 3, 5));
    }

    @Test
    public void whenNewValidBetIsPlaced_CurrentBetIsUpdated() {
        controller.putNewBet(createSimpleBet(players.get(0).getId(), 2, 5));

        assertEquals(2, controller.getCurrentBet().getAmount());
        assertEquals(5, controller.getCurrentBet().getFace());

        controller.putNewBet(createSimpleBet(players.get(1).getId(), 3, 6));

        assertEquals(3, controller.getCurrentBet().getAmount());
        assertEquals(6, controller.getCurrentBet().getFace());
    }

    @Test
    public void whenNewInvalidBetIsPlaced_CurrentBetIsNotUpdated_ThrowsInvalidBet() {
        controller.putNewBet(createSimpleBet(players.get(0).getId(), 2, 4));

        boolean exceptionThrown = false;
        try {
            controller.putNewBet(createSimpleBet(players.get(1).getId(), 2, 3));
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
        checkNextPlayerThenPlaceBet(0, createBet(2, 5));
        checkNextPlayerThenPlaceBet(1, createBet(3, 5));
        checkNextPlayerThenPlaceBet(2, createBet(4, 5));
        checkNextPlayerThenPlaceBet(0, createBet(5, 5));
    }

    private void checkNextPlayerThenPlaceBet(int index, Bet bet) {
        long pid = players.get(index).getId();
        assertEquals(pid, controller.getNextPlayerId());
        controller.putNewBet(createSimpleBet(pid, bet.getAmount(), bet.getFace()));
    }

    @Test
    public void whenNewInvalidBetIsPlaced_PlayerOrderIsUnchanged() {
        assertNull(controller.getCurrentBet());

        boolean exceptionThrown = false;
        try {
            checkNextPlayerThenPlaceBet(0, createBet(2, 1));
        } catch (Exception e) {
            assertTrue(e instanceof Bet.InvalidBet);
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        assertNull(controller.getCurrentBet());
        assertEquals(players.get(0).getId(), controller.getNextPlayerId());
    }

    @Test
    public void whenDoubtIsCalled_TotalNumberOfDicesIsDecreasedByOne() {
        assertEquals(15, controller.getAllDices().size());

        checkNextPlayerThenPlaceBet(0, createBet(2, 5));
        controller.doubtCurrentBet(players.get(1).getId());

        assertEquals(14, controller.getAllDices().size());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void onlyNextPlayerCanDoubt_anyOtherPlayerIdThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(0, createBet(1, 3));

        assertEquals(players.get(1).getId(), controller.getNextPlayerId());
        controller.doubtCurrentBet(players.get(2).getId());
    }

    @Test
    public void playerDoubtingValidBetLosesOneDiceAndIsUpToNext() {
        checkNextPlayerThenPlaceBet(0, createBet(1, 5));

        long doubtingPlayerId = players.get(1).getId();
        controller.doubtCurrentBet(doubtingPlayerId);

        assertEquals(14, controller.getAllDices().size());
        assertEquals(4, controller.getPlayer(doubtingPlayerId).getDiceCount());
        assertEquals(doubtingPlayerId, controller.getNextPlayerId());
    }

    @Test
    public void whenBetIsDoubted_PlayerWhoPutsWrongBetLosesOneDiceAndIsUpToNext() {
        checkNextPlayerThenPlaceBet(0, createBet(6, 4));
        controller.doubtCurrentBet(players.get(1).getId());

        long loosingPID = players.get(0).getId();

        assertEquals(14, controller.getAllDices().size());
        assertEquals(4, controller.getPlayer(loosingPID).getDiceCount());
        assertEquals(loosingPID, controller.getNextPlayerId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void playerWhoPlacedCurrentBetCannotFit_ThrowsInvalidAction() {
        players.get(0).removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(4, 5));
        controller.fitCurrentBet(players.get(0).getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void playerWhoReceivesCurrentBetCannotFit_ThrowsInvalidAction() {
        players.get(1).removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.fitCurrentBet(players.get(1).getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenFitCurrentBetIsCalledWithInvalidPlayerId_ThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(0, createBet(2, 5));
        controller.fitCurrentBet(2873562986592639L);
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void onlyPlayersWithDiceSetNotFullCanFitCurrentBet_ThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.fitCurrentBet(players.get(2).getId());
    }

    @Test
    public void playerFittingCurrentValidBet_ObtainsOneDiceAndIsUpToNext() {
        Player fittingPlayer = players.get(1);
        fittingPlayer.removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(3, 5));
        checkNextPlayerThenPlaceBet(1, createBet(4, 5));
        checkNextPlayerThenPlaceBet(2, createBet(6, 5));
        controller.fitCurrentBet(fittingPlayer.getId());

        assertEquals(15, controller.getAllDices().size());
        assertEquals(5, fittingPlayer.getDiceCount());
        assertEquals(fittingPlayer.getId(), controller.getNextPlayerId());
    }

    @Test
    public void playerFittingCurrentInvalidBet_LosesOneDiceAndIsUpToNext() {
        Player fittingPlayer = players.get(1);
        fittingPlayer.removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(3, 3));
        checkNextPlayerThenPlaceBet(1, createBet(4, 3));
        checkNextPlayerThenPlaceBet(2, createBet(5, 3));
        controller.fitCurrentBet(fittingPlayer.getId());

        assertEquals(13, controller.getAllDices().size());
        assertEquals(3, fittingPlayer.getDiceCount());
    }

    @Test
    public void afterDoubtIsCalled_RoundIsOver() {
        checkNextPlayerThenPlaceBet(0, createBet(2, 4));
        controller.doubtCurrentBet(players.get(1).getId());

        assertTrue(controller.isRoundOver());
    }

    @Test
    public void afterFitIsCalled_RoundIsOver() {
        players.get(2).removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.fitCurrentBet(players.get(2).getId());

        assertTrue(controller.isRoundOver());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_PutNewBetThrowsInvalidAction() {
        players.get(2).removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.fitCurrentBet(players.get(2).getId());

        controller.putNewBet(BetFactory.createSimpleBet(players.get(2).getId(), 4, 5));
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_DoubtCurrentBetThrowsInvalidAction() {
        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.doubtCurrentBet(players.get(1).getId());

        controller.doubtCurrentBet(players.get(1).getId());
    }

    @Test(expected = RoundController.InvalidAction.class)
    public void whenRoundIsOver_FitCurrentBetThrowsInvalidAction() {
        players.get(2).removeDice();

        checkNextPlayerThenPlaceBet(0, createBet(1, 4));
        controller.doubtCurrentBet(players.get(1).getId());

        controller.fitCurrentBet(players.get(2).getId());
    }

    @Test
    public void whenRoundIsOver_Doubt_CanSayIfSomePlayerIsOutOfGame() {
        removeNDicesToPlayer(4, 1);

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.doubtCurrentBet(players.get(1).getId());

        assertTrue(controller.isRoundOver());
        assertTrue(controller.isPlayerOut());
    }

    @Test
    public void whenRoundIsOver_Fit_CanSayIfSomePlayerIsOutOfGame() {
        removeNDicesToPlayer(4, 2);

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.fitCurrentBet(players.get(2).getId());

        assertTrue(controller.isRoundOver());
        assertTrue(controller.isPlayerOut());
    }

    private void removeNDicesToPlayer(int dicesToRemove, int playerIndex) {
        while (dicesToRemove-- > 0)
            players.get(playerIndex).removeDice();
    }

    @Test
    public void whenRoundIsOverIfPlayerLosesAllHisDicesIsExposedBothAsDeadFlagAndPlayer() {
        removeNDicesToPlayer(4, 1);

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        assertNull(controller.getDeadPlayer());
        assertFalse(controller.isPlayerOut());

        controller.doubtCurrentBet(players.get(1).getId());
        assertTrue(controller.isPlayerOut());
        assertEquals(players.get(1), controller.getDeadPlayer());

        controller.cleanUpRound();
        assertNull(controller.getDeadPlayer());
        assertFalse(controller.isPlayerOut());
        assertEquals(2, controller.getAllPlayerIds().length);
    }

    @Test
    public void whenRoundIsOver_CallingCleanUpRoundPermitsNextRound() {
        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        controller.doubtCurrentBet(players.get(1).getId());

        assertTrue(controller.isRoundOver());
        controller.cleanUpRound();

        assertFalse(controller.isRoundOver());
        checkNextPlayerThenPlaceBet(1, createBet(1, 4));
    }

    @Test
    public void initializingNewRoundWithPlayerHavingOneDiceAndHasNotAlreadyBeenPalific_StarterBetIsPalific() {
        removeNDicesToPlayer(4, 0);

        Player p = controller.getPlayer(controller.getNextPlayerId());
        assertFalse(p.hasBeenPalific());
        checkNextPlayerThenPlaceBet(0, createBet(1, 1));

        assertTrue(p.hasBeenPalific());
        assertTrue(controller.getCurrentBet() instanceof StarterPalificBet);
    }

    @Test
    public void onePlayerCannotPutTwoStarterPalificInTheSameGame() {
        removeNDicesToPlayer(3, 1);
        checkNextPlayerThenPlaceBet(0, createBet(3, 2));
        controller.doubtCurrentBet(players.get(1).getId());
        controller.cleanUpRound();

        checkNextPlayerThenPlaceBet(1, createBet(2, 3));
        assertTrue(controller.getCurrentBet() instanceof StarterPalificBet);

        checkNextPlayerThenPlaceBet(2, createBet(3, 3));
        controller.fitCurrentBet(players.get(1).getId());
        assertEquals(2, players.get(1).getDiceCount());
        controller.cleanUpRound();

        checkNextPlayerThenPlaceBet(1, createBet(5, 3));
        checkNextPlayerThenPlaceBet(2, createBet(6, 4));
        controller.fitCurrentBet(players.get(1).getId());
        controller.cleanUpRound();

        checkNextPlayerThenPlaceBet(1, createBet(2, 4));
        assertTrue(controller.getCurrentBet() instanceof StarterBet);
        assertEquals(1, players.get(1).getDiceCount());
        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test
    public void whenBetIsUpdatedFromPlayerHavingOneDice_CurrentBetBecomesFaceProtected() {
        removeNDicesToPlayer(4, 1);

        checkNextPlayerThenPlaceBet(0, createBet(2, 3));
        assertFalse(((BaseBet) controller.getCurrentBet()).isFaceProtected());

        checkNextPlayerThenPlaceBet(1, createBet(3, 5));
        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test
    public void whenBetIsFaceProtected_PlayerHavingOneDiceCanChangeTheFace() {
        removeNDicesToPlayer(4, 0);
        removeNDicesToPlayer(4, 1);

        checkNextPlayerThenPlaceBet(0, createBet(2, 4));
        assertTrue(controller.getCurrentBet() instanceof StarterPalificBet);

        checkNextPlayerThenPlaceBet(1, createBet(2, 5));
        assertEquals(2, controller.getCurrentBet().getAmount());
        assertEquals(5, controller.getCurrentBet().getFace());

        assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
    }

    @Test(expected = Bet.InvalidBet.class)
    public void whenBetIsFaceProtected_PlayerHavingMoreThanOneDiceCannotChangeFace_ThrowsInvalidBet() {
        removeNDicesToPlayer(4, 1);
        boolean exceptionThrown = false;

        checkNextPlayerThenPlaceBet(0, createBet(2, 4));
        checkNextPlayerThenPlaceBet(1, createBet(1, 1));
        try {
            checkNextPlayerThenPlaceBet(2, createBet(3, 4));
        } catch (Bet.InvalidBet invalidBet) {
            exceptionThrown = true;
            assertEquals(1, controller.getCurrentBet().getAmount());
            assertEquals(1, controller.getCurrentBet().getFace());

            assertTrue(((BaseBet) controller.getCurrentBet()).isFaceProtected());
        }

        assertTrue(exceptionThrown);
        throw new Bet.InvalidBet();
    }

}
