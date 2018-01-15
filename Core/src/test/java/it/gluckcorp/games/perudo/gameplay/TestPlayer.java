package it.gluckcorp.games.perudo.gameplay;

import org.junit.Before;
import org.junit.Test;
import it.gluckcorp.games.perudo.gameplay.players.Player;

import java.util.List;

import static org.junit.Assert.*;

public class TestPlayer {
    private Player p;

    private void removeNTime(int n) {
        while (n-- > 0)
            p.removeDice();
    }

    @Before
    public void setUp() throws Exception {
        p = Player.CreatePlayerWithDiceSet(5);
    }

    @Test(expected = Player.InvalidPlayer.class)
    public void creatingPlayerWithLessThanOneDice_ThrowsInvalidPlayer() {
        p = Player.CreatePlayerWithDiceSet(0);
    }

    @Test
    public void playerCanShowHisPlayerId() {
        assertNotNull(p.getId());
    }

    @Test
    public void playerCanRemoveOrAddDicesToHisSet() {
        assertEquals(5, p.getDiceCount());
        p.removeDice();
        assertEquals(4, p.getDiceCount());
        p.addDice();
        assertEquals(5, p.getDiceCount());
    }

    @Test
    public void playerCanExposeHisDiceSet() {
        List<Integer> dices = p.getDices();
        assertEquals(5, dices.size());
    }

    @Test(expected = Player.DicesOverflow.class)
    public void addingMoreDicesThanSpecifiedWhileBuildingPlayer_ThrowsDicesOverflow() {
        p.addDice();
    }

    @Test(expected = Player.DicesUnderflow.class)
    public void minDiceSetSizeIsZero_CallingRemoveThrowsDicesUnderflow() {
        removeNTime(6);
    }

    @Test
    public void playerCanSayWhetherHisDiceSetIsFull() {
        assertTrue(p.isDiceSetFull());

        p.removeDice();
        assertFalse(p.isDiceSetFull());

        p.addDice();
        assertTrue(p.isDiceSetFull());
    }

    @Test
    public void playerCanSayWhetherHisDiceSetIsEmpty() {
        removeNTime(4);
        assertFalse(p.isDiceSetEmpty());

        p.removeDice();
        assertTrue(p.isDiceSetEmpty());
    }

    @Test
    public void playerCanSayWhetherHisDiceSetContainsOnlyOneDice() {
        assertFalse(p.hasOneDice());
        removeNTime(4);
        assertTrue(p.hasOneDice());
    }

    @Test
    public void itIsPossibleToChangePalificOption() {
        assertFalse(p.hasBeenPalific());
        p.setPalificOption();
        assertTrue(p.hasBeenPalific());
    }
}
