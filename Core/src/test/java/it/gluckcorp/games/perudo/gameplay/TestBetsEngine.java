package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.BaseBet;
import it.gluckcorp.games.perudo.gameplay.bets.Bet;
import it.gluckcorp.games.perudo.gameplay.bets.BetFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static it.gluckcorp.games.perudo.gameplay.bets.Bet.InvalidBet;
import static it.gluckcorp.games.perudo.gameplay.bets.BetFactory.*;
import static org.junit.Assert.*;


public class TestBetsEngine {
    private BaseBet starter;
    private BaseBet starterPalific;

    @Before
    public void setUp() throws InvalidBet {
        starter = createStarterBet(3, 3);
        starterPalific = createStarterPalificBet(2, 4);
    }

    @Test
    public void canCreateBetFactory_AlthoughItIsUseless() {
        BetFactory factory = new BetFactory();
        assertNotNull(factory);
    }

    @Test
    public void canCreateNewSimpleBet() {
        Bet simpleBet = createSimpleBet(2734518354L, 2, 4);
        assertNotNull(simpleBet);
    }

    @Test(expected = InvalidBet.class)
    public void creatingStarterBetWithFace1_ThrowsInvalidBet() throws InvalidBet {
        createStarterBet(5, 1);
    }

    @Test(expected = InvalidBet.class)
    public void creatingSimpleBetWithFaceGT7_ThrowsInvalidBet() throws InvalidBet {
        createSimpleBet(-231416L, 3, 7);
    }

    @Test(expected = InvalidBet.class)
    public void creatingStarterBetWithFaceGT7_ThrowsInvalidBet() throws InvalidBet {
        createStarterBet(3, 7);
    }

    @Test(expected = InvalidBet.class)
    public void creatingStarterPalificBetWithFaceGT7_ThrowsInvalidBet() throws InvalidBet {
        createStarterPalificBet(2, 7);
    }

    @Test(expected = InvalidBet.class)
    public void whenBetIsCreatedWithZeroOrNegativeAmount_ThrowsInvalidBet() throws InvalidBet {
        boolean exceptionThrown = false;

        try {
            createSimpleBet(0, 0, 4);
        } catch (InvalidBet invalidBet) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        exceptionThrown = false;

        try {
            createSimpleBet(0, -5, 4);
        } catch (InvalidBet invalidBet) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        throw new InvalidBet();
    }

    @Test
    public void betCanBeUpated() throws InvalidBet {
        starter.updateBet(createSimpleBet(-1L, 6, 4));
    }

    @Test(expected = InvalidBet.class)
    public void wheneverNewBetIsLower_ThrowsInvalidBet() throws InvalidBet {
        starter = createStarterBet(3, 3);
        try {
            starter.updateBet(createSimpleBet(-2346L, 2, 5));
        } catch (InvalidBet invalidBet) {
            assertEquals(3, starter.getAmount());
            assertEquals(3, starter.getFace());

            throw invalidBet;
        }
    }

    @Test
    public void betCanBeCheckedUponADiceSet_ValidBet() {
        Integer[] dices = {1, 4, 5, 2, 3, 3, 3, 6, 3, 1};
        assertTrue(starter.isLessOrEqual(Arrays.asList(dices)));
    }

    @Test
    public void betCanCheckUponADiceSet_InvalidBet() {
        Integer[] dices = {1, 4, 5, 2, 3, 5, 6};
        assertFalse(starter.isLessOrEqual(Arrays.asList(dices)));
    }

    @Test
    public void betCanCheckIfIsExactUponADisceSet_ValidBet() {
        starter = createStarterBet(3, 4);
        Integer[] dices = {2, 3, 6, 4, 1, 5, 4, 2};

        assertTrue(starter.isExact(Arrays.asList(dices)));
    }

    @Test
    public void betCanCheckIfIsExactUponADisceSet_InvalidBet() {
        starter = createStarterBet(2, 6);
        Integer[] dices = {2, 3, 6, 4, 1, 5, 1, 2};

        assertFalse(starter.isExact(Arrays.asList(dices)));
    }

    @Test
    public void palificBetCanCheckIfIsLessOrEqualUponADiceSet_ValidBet() {
        starterPalific = createStarterPalificBet(2, 3);
        Integer[] dices = {2, 3, 6, 3, 1, 5, 1, 2};

        assertTrue(starterPalific.isLessOrEqual(Arrays.asList(dices)));
    }

    @Test
    public void palificBetCanCheckIfIsLessOrEqualUponADiceSet_InvalidBet() {
        starterPalific = createStarterPalificBet(2, 5);
        Integer[] dices = {2, 3, 6, 3, 1, 5, 1, 2};

        assertFalse(starterPalific.isLessOrEqual(Arrays.asList(dices)));
    }

    @Test
    public void palificBetCanCheckIfIsExactUponADiceSet_ValidBet() {
        starterPalific = createStarterPalificBet(2, 2);
        Integer[] dices = {2, 3, 6, 3, 1, 5, 1, 2};

        assertTrue(starterPalific.isExact(Arrays.asList(dices)));
    }

    @Test
    public void palificBetCanCheckIfIsExactUponADiceSet_InvalidBet() {
        starterPalific = createStarterPalificBet(3, 5);
        Integer[] dices = {2, 3, 6, 3, 1, 5, 1, 2};

        assertFalse(starterPalific.isExact(Arrays.asList(dices)));
    }

    @Test
    public void betHavingAmountGreaterThenCurrentBetIsValid() {
        starter = createStarterBet(2, 3);
        starter.updateBet(createBet(3, 5));

        assertEquals(3, starter.getAmount());
        assertEquals(5, starter.getFace());
    }

    @Test
    public void betHavingSameAmountButGreaterFaceThenCurrentBetIsValid() {
        starter = createStarterBet(3, 3);
        starter.updateBet(createBet(3, 5));

        assertEquals(3, starter.getAmount());
        assertEquals(5, starter.getFace());
    }

    @Test
    public void whenBettingOnFaceOne_MinimumAmountIsTheExceedingHalfOfCurrentBet() {
        starter = createStarterBet(3, 3);
        starter.updateBet(createBet(2, 1));

        assertEquals(2, starter.getAmount());
        assertEquals(1, starter.getFace());
    }

    @Test
    public void whenBettingFromFaceOneToAnyOther_MinimumAmountIsDoublePlusOne() {
        starter = createStarterBet(7, 5);
        starter.updateBet(createBet(4, 1));
        starter.updateBet(createBet(9, 2));

        assertEquals(9, starter.getAmount());
        assertEquals(2, starter.getFace());
    }

    @Test
    public void bettingFromFaceOneToFaceOne_AmountMustBeGreater() {
        starter = createStarterBet(4, 6);
        starter.updateBet(createBet(3, 1));
        starter.updateBet(createBet(5, 1));

        assertEquals(5, starter.getAmount());
        assertEquals(1, starter.getFace());
    }

    @Test
    public void fromPalificBet_NewBetIsValidWhenAmountIsGreater() {
        starterPalific = createStarterPalificBet(2, 4);
        starterPalific.updateBet(createBet(3, 4));

        assertEquals(3, starterPalific.getAmount());
        assertEquals(4, starterPalific.getFace());
    }

    @Test
    public void fromPalificBet_NewBetIsValidWithSameAmountAndFaceGreater() {
        starterPalific = createStarterPalificBet(2, 1);
        starterPalific.updateBet(createBet(3, 1));

        assertEquals(3, starterPalific.getAmount());
        assertEquals(1, starterPalific.getFace());
    }

    @Test(expected = InvalidBet.class)
    public void fromPalificBet_BetWithLowerAmountIsNotValid() {
        starterPalific = createStarterPalificBet(3, 5);
        starterPalific.updateBet(createBet(2, 5));
    }

    @Test(expected = InvalidBet.class)
    public void fromPalificBet_BetWithLowerFaceIsNotValid() {
        starterPalific = createStarterPalificBet(3, 5);
        starterPalific.updateBet(createBet(5, 2));
    }

    @Test
    public void starterBetIsCreatedWithoutFaceProtection() {
        assertFalse(starter.isFaceProtected());
    }

    @Test
    public void starterPalificBetIsCreatedWithFaceProtection() {
        assertTrue(starterPalific.isFaceProtected());
    }

    @Test
    public void itIsPossibleToSwitchFaceProtectionOnOff() {
        starter.setFaceProtected(true);
        assertTrue(starter.isFaceProtected());

        starter.setFaceProtected(false);
        assertFalse(starter.isFaceProtected());
    }

    @Test( expected = InvalidBet.class)
    public void tryingToUpdateCurrentBetsFaceWhileFaceProtectionIsOn_ThrowsInvalidBet() {
        starterPalific.updateBet(createBet(4,6));
    }
}
