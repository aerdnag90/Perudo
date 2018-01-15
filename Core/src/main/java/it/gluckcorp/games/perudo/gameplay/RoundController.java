package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.BaseBet;
import it.gluckcorp.games.perudo.gameplay.bets.Bet;
import it.gluckcorp.games.perudo.gameplay.bets.BetFactory;
import it.gluckcorp.games.perudo.gameplay.bets.SimpleBet;
import it.gluckcorp.games.perudo.gameplay.players.Player;

import java.util.ArrayList;
import java.util.List;

public class RoundController {
    private List<Player> players;
    private BaseBet currentBet;
    private boolean roundOver;
    private Player deadPlayer;
    private Player loosingPlayer;

    public static RoundController SetupNewRound(List<Player> players) {
        return new RoundController(players);
    }

    private RoundController(List<Player> players) {
        this.players = new ArrayList<>();
        this.players.addAll(players);
        roundOver = false;
    }

    public void cleanUpRound() {
        roundOver = false;
        currentBet = null;

        if(deadPlayer != null){
            players.remove(deadPlayer);
            deadPlayer = null;
        }

        loosingPlayer = null;

        shuffleDices();
    }

    private void shuffleDices() {
        for (Player player : players) {
            player.shuffleDices();
        }
    }

    public boolean isRoundOver() {
        return roundOver;
    }

    public boolean isPlayerOut() {
        return deadPlayer != null;
    }

    // ---------------------------------------------------------------------------------------

    public void putNewBet(SimpleBet bet) {
        if (roundOver || !isPlayerAtTheTop(bet.getPlayerId()))
            throw new InvalidAction();

        if (currentBet == null) {
            createNewStarterBet(bet.getAmount(), bet.getFace());
        } else
            updateCurrentBet(bet);

        shiftPlayers();
    }

        private void createNewStarterBet(int amount, int face) {
            Player p = players.get(0);

            if (shouldBePalific(p)) {
                currentBet = BetFactory.createStarterPalificBet(amount, face);
                p.setPalificOption();
            } else {
                currentBet = BetFactory.createStarterBet(amount, face);
                if(p.hasOneDice())
                    currentBet.setFaceProtected(true);
            }
        }

            public boolean shouldBePalific(Player player) {
            return player.hasOneDice() && !player.hasBeenPalific();
        }

        private void updateCurrentBet(SimpleBet bet) {
            long playerId = bet.getPlayerId();

            if (canPlayerBetNormally(playerId))
                updateCurrentBetRemovingFaceProtection(bet);
            else
                currentBet.updateBet(bet);


            if (getPlayer(bet.getPlayerId()).hasOneDice())
                currentBet.setFaceProtected(true);
        }

            private boolean canPlayerBetNormally(long playerId) {
                return !currentBet.isFaceProtected() || getPlayer(playerId).hasOneDice();
            }

            private void updateCurrentBetRemovingFaceProtection(SimpleBet bet) {
                boolean wasFaceProtected = currentBet.isFaceProtected();

                currentBet.setFaceProtected(false);
                try {
                    currentBet.updateBet(bet);
                } catch (Bet.InvalidBet invalidBet) {
                    currentBet.setFaceProtected(wasFaceProtected);
                    throw invalidBet;
                }

                currentBet.setFaceProtected(wasFaceProtected);
            }

    public void doubtCurrentBet(long playerId) {
        if (currentBet == null || roundOver || !playerCanDoubt(playerId))
            throw new InvalidAction();

        if (currentBet.isLessOrEqual(getAllDices()))
            loosingPlayer = players.get(0);
        else
            loosingPlayer = players.get(players.size() - 1);

        loosingPlayer.removeDice();

        if (loosingPlayer.isDiceSetEmpty())
            deadPlayer = loosingPlayer;

        if(deadPlayer != null)
            shiftToPlayerAfter();
        else
            shiftToPlayer(loosingPlayer.getId());

        roundOver = true;
    }

    protected List<Integer> getAllDices() {
        List<Integer> dices = new ArrayList<>();
        for (Player player : players)
            dices.addAll(player.getDices());

        return dices;
    }

    private boolean playerCanDoubt(long playerId) {
        return getPlayer(playerId) != null && isPlayerAtTheTop(playerId);
    }

    public void fitCurrentBet(long playerId) {
        if (currentBet == null || roundOver || !playerCanFit(playerId))
            throw new InvalidAction();

        Player loosingPlayer = getPlayer(playerId);
        if (currentBet.isExact(getAllDices()))
            loosingPlayer.addDice();
        else
            loosingPlayer.removeDice();


        if (loosingPlayer.isDiceSetEmpty())
            deadPlayer = loosingPlayer;

        if(deadPlayer != null)
            shiftToPlayerAfter();
        else
            shiftToPlayer(loosingPlayer.getId());

        roundOver = true;
    }

        private boolean playerCanFit(long playerId) {
        Player p = getPlayer(playerId);

        return p != null &&
                !p.isDiceSetFull() &&
                !isPlayerAtTheTop(playerId) &&
                !isPlayerAtTheBottom(playerId);
    }

    public Bet getCurrentBet() {
        return currentBet;
    }

    // ---------------------------------------------------------------------------------------

    public long getNextPlayerId() {
        return players.get(0).getId();
    }

    private void shiftToPlayer(long playerId) {
        List<Player> shiftingPlayers = new ArrayList<>();

        while (players.get(0).getId() != playerId) {
            Player p = players.remove(0);
            shiftingPlayers.add(p);
        }

        players.addAll(shiftingPlayers);
    }

    private void shiftPlayers() {
        Player tmp = players.remove(0);
        players.add(tmp);
    }

    private void shiftToPlayerAfter() {
        int index = players.indexOf(deadPlayer);

        if (index != players.size() - 1) {
            shiftToPlayer(players.get(index + 1).getId());
        }
    }

    private boolean isPlayerAtTheTop(long playerId) {
        return players.get(0).getId() == playerId;
    }

    private boolean isPlayerAtTheBottom(long playerId) {
        return players.get(players.size() - 1).getId() == playerId;
    }

    public Player getPlayer(long playerId) {
        for (Player p : players)
            if (p.getId() == playerId)
                return p;

        return null;
    }

    public long[] getAllPlayerIds() {
        long[] pIds = new long[players.size()];

        for (int i = 0; i < players.size(); i++)
            pIds[i] = players.get(i).getId();

        return pIds;
    }

    public void setNextPlayer(long playerId) {
        shiftToPlayer(playerId);
    }

    public Player getDeadPlayer() {
        return deadPlayer;
    }

    public Player getLoosingPlayer() {
        return loosingPlayer;
    }

    public static class InvalidAction extends RuntimeException {
        public InvalidAction() {
        }

        public InvalidAction(String message) {
            super(message);
        }
    }
}
