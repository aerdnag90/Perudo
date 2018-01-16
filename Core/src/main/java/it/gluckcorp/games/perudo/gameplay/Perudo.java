package it.gluckcorp.games.perudo.gameplay;

import it.gluckcorp.games.perudo.gameplay.bets.Bet;
import it.gluckcorp.games.perudo.gameplay.bets.BetFactory;
import it.gluckcorp.games.perudo.gameplay.bets.SimpleBet;
import it.gluckcorp.games.perudo.gameplay.players.Player;
import it.gluckcorp.games.perudo.gameplay.players.Players;

import java.util.ArrayList;
import java.util.List;

public class Perudo {
    private RoundController roundController;
    public Console gameInterface;

    Perudo() {
    }

    public Perudo(List<String> playerNames, int dicesPerPlayer) {
        if (playerNames == null || playerNames.size() < 2)
        throw new RoundController.InvalidAction("At least two players required!");

        if (dicesPerPlayer < 1)
            throw new RoundController.InvalidAction("At least one dice per player is required!");

        initGame(playerNames, dicesPerPlayer);
    }

    void initGame(List<String> playerNames, int dicesPerPlayer) {
        List<Player> players = initPlayers(playerNames, dicesPerPlayer);
        roundController = RoundController.SetupNewRound(players);
    }

    private List<Player> initPlayers(List<String> playerNames, int dicesPerPlayers) {
        List<Player> players = new ArrayList<>();

        for (String name : playerNames)
            players.add(Players.CreatePlayerWithDiceSet(name, dicesPerPlayers));

        return players;
    }

    // ---------------------------------------------------------------------------------------

    public void startGame() {
        while (roundController.getAllPlayerIds().length > 1) {
            final Player player = roundController.getPlayer(roundController.getNextPlayerId());

            int[] bet = getPlayerBet(player);

            if (bet != null) {
                putNewBet(player, bet);
            } else {
                List<Integer> allDices = roundController.getAllDices();
                doubtCurrentBet(roundController.getNextPlayerId());

                Player loosingPlayer = roundController.getLoosingPlayer();
                gameInterface.announceRoundSummary(allDices, roundController.getCurrentBet(), loosingPlayer.getName());
                if (roundController.isPlayerOut())
                    gameInterface.announcePlayerOut(loosingPlayer.getName());

                roundController.cleanUpRound();
                gameInterface.announceRoundOver();
            }
        }

        gameInterface.announceGameOver(roundController.getPlayer(roundController.getNextPlayerId()).getName());
    }

    private int[] getPlayerBet(Player player) {
        int[] bet;
        if (roundController.getCurrentBet() == null) {
            bet = gameInterface.getFirstBet(player.getName(), player.getDices(), roundController.shouldBePalific(player));
        } else {
            gameInterface.showCurrentBet(roundController.getCurrentBet());
            bet = gameInterface.getNextBet(player.getName(), player.getDices());
        }
        return bet;
    }

    // ---------------------------------------------------------------------------------------

    void putNewBet(Player p, int[] bet) {
        try {
            SimpleBet simpleBet = BetFactory.createSimpleBet(p.getId(), bet[0], bet[1]);
            roundController.putNewBet(simpleBet);
        } catch (Bet.InvalidBet invalidBet) {
            gameInterface.promptInvalidBet(p.getName(), "Invalid Bet: " + invalidBet.getMessage());
        }
    }

    void doubtCurrentBet(long playerId) {
        roundController.doubtCurrentBet(playerId);
    }

    void fitCurrentBet(long playerId) {
        roundController.fitCurrentBet(playerId);
    }

    // ------------------------------- TEST --------------------------------------------------------

    long getNextPlayerId() {
        return roundController.getNextPlayerId();
    }

    void setStartingPlayer(long playerId) {
        roundController.setNextPlayer(playerId);
    }

    long[] getPlayerIds() {
        return roundController.getAllPlayerIds();
    }

    Player getPlayerById(long playerId) {
        return roundController.getPlayer(playerId);
    }

}
