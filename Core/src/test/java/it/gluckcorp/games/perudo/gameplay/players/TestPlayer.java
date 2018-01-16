package it.gluckcorp.games.perudo.gameplay.players;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestPlayer {
    private PlayerImpl p;

    private void removeNDices(int n) {
        while (n-- > 0)
            p.removeDice();
    }

    @Before
    public void setUp() {
        p = new PlayerImpl(5);
    }

    @Test(expected = PlayerImpl.InvalidPlayer.class)
    public void creatingPlayerWithLessThanOneDice_ThrowsInvalidPlayer() {
        Players.CreatePlayerWithDiceSet(0);
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

    @Test(expected = PlayerImpl.DicesOverflow.class)
    public void addingMoreDicesThanSpecifiedWhileBuildingPlayer_ThrowsDicesOverflow() {
        p.addDice();
    }

    @Test(expected = PlayerImpl.DicesUnderflow.class)
    public void minDiceSetSizeIsZero_CallingRemoveThrowsDicesUnderflow() {
        removeNDices(6);
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
        removeNDices(4);
        assertFalse(p.isDiceSetEmpty());

        p.removeDice();
        assertTrue(p.isDiceSetEmpty());
    }

    @Test
    public void playerCanSayWhetherHisDiceSetContainsOnlyOneDice() {
        assertFalse(p.hasOneDice());
        removeNDices(4);
        assertTrue(p.hasOneDice());
    }

    @Test
    public void itIsPossibleToChangePalificOption() {
        assertFalse(p.hasBeenPalific());
        p.setPalificOption();
        assertTrue(p.hasBeenPalific());
    }
}
