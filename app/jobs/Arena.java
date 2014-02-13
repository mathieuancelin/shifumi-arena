package jobs;

import com.google.gson.Gson;
import models.Fight;
import models.Game;
import models.Round;
import play.jobs.Job;
import play.libs.WS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Arena extends Job<ArenaResult> {

    private static final int LIMIT = 11;

    private final Player player;
    private final Player counterPlayer;

    public Arena(Player player, Player counterPlayer) {
        this.player = player;
        this.counterPlayer = counterPlayer;
    }

    @Override
    public ArenaResult doJobWithResult() throws Exception {
        try {
            String id = UUID.randomUUID().toString();
            player.setupGame(id, LIMIT);
            counterPlayer.setupGame(id, LIMIT);
            Game playerGame = player.game(id);
            Game counterPlayerGame = counterPlayer.game(id);
            List<Round> playerRounds = new ArrayList<Round>();
            List<Round> counterPlayerRounds = new ArrayList<Round>();
            int counterPlayerScore = 0;
            int playerScore = 0;
            for (int i = 0; i < LIMIT; i++) {
                String counterHand = counterPlayer.hands(LIMIT).get(i);
                String playerHand = player.hands(LIMIT).get(i);
                if (counterHand.equals(playerHand)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, false);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, true);
                    counterPlayerRounds.add(cround);
                }
                if (counterHand.equals(Game.PAPER) && playerHand.equals(Game.ROCK)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, false);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, true);
                    counterPlayerRounds.add(cround);
                    counterPlayerScore++;
                }
                if (counterHand.equals(Game.SCISSORS) && playerHand.equals(Game.ROCK)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, true);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, false);
                    counterPlayerRounds.add(cround);
                    playerScore++;
                }
                if (counterHand.equals(Game.ROCK) && playerHand.equals(Game.PAPER)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, true);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, false);
                    counterPlayerRounds.add(cround);
                    playerScore++;
                }
                if (counterHand.equals(Game.SCISSORS) && playerHand.equals(Game.PAPER)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, false);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, true);
                    counterPlayerRounds.add(cround);
                    counterPlayerScore++;
                }
                if (counterHand.equals(Game.PAPER) && playerHand.equals(Game.SCISSORS)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, true);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, false);
                    counterPlayerRounds.add(cround);
                    playerScore++;
                }
                if (counterHand.equals(Game.ROCK) && playerHand.equals(Game.SCISSORS)) {
                    Round round = new Round(player.getIp(), counterPlayer.getIp(), playerHand, counterHand, false);
                    playerRounds.add(round);
                    Round cround = new Round(counterPlayer.getIp(), player.getIp(), counterHand, playerHand, true);
                    counterPlayerRounds.add(cround);
                    counterPlayerScore++;
                }
            }
            boolean playerWin = playerScore > counterPlayerScore;
            boolean counterPlayerWin = playerScore < counterPlayerScore;
            playerGame.win = playerWin;
            playerGame.rounds = playerRounds;
            counterPlayerGame.win = counterPlayerWin;
            counterPlayerGame.rounds = counterPlayerRounds;
            player.submit(playerGame);
            counterPlayer.submit(counterPlayerGame);
            new Fight(playerGame, counterPlayerGame, playerWin, counterPlayerWin, player.getIp(), counterPlayer.getIp(), null, System.currentTimeMillis()).save();
            return new ArenaResult("Everything went well :-)", false);
        } catch (Exception e) {
            new Fight(null, null, false, false, "", "", e.getMessage(), System.currentTimeMillis()).save();
            return new ArenaResult(e.getMessage(), true);
        }
    }

    public static List<String> generateHands(int rounds) {
        List<String> hands = new ArrayList<String>();
        Random random = new Random();
        for (int i = 0; i < rounds; i++) {
            int choice = random.nextInt(3) + 1;
            if (choice == 1) {
                hands.add(Game.PAPER);
            }
            if (choice == 2) {
                hands.add(Game.ROCK);
            }
            if (choice == 3) {
                hands.add(Game.SCISSORS);
            }
        }
        return hands;
    }

    public static interface Player {
        public void setupGame(String id, int rounds);
        public Game game(String id);
        public List<String> hands(int rounds);
        public void submit(Game game);
        public String getIp();
    }

    public static class IAPlayer implements Player {

        private List<String> hands;

        @Override
        public void setupGame(String id, int rounds) {
            hands = generateHands(rounds);
        }

        @Override
        public Game game(String id) {
            return new Game(id, null);
        }

        @Override
        public List<String> hands(int rounds) {
            return hands;
        }

        @Override
        public void submit(Game game) {
        }

        @Override
        public String getIp() {
            return "AI Player";
        }
    }

    public static class ActualPlayer implements Player {

        private String games;
        private String ip;
        private String id;

        public ActualPlayer(String ip) {
            this.games = "http://" + ip + "/games";
            this.ip = ip;
        }

        @Override
        public void setupGame(String id, int rounds) {
            WS.HttpResponse response = WS.url(games).setParameter("id", id).setParameter("rounds", LIMIT).post();
            if (!response.success()) {
               throw new RuntimeException("Setup game failed");
            }
        }

        @Override
        public Game game(String id) {
            this.id = id;
            String onegame = "http://" + ip + "/games/" + id;
            WS.HttpResponse response = WS.url(onegame).get();
            if (!response.success()) {
                throw new RuntimeException("Retrieve game failed");
            }
            Game game = new Gson().fromJson(response.getJson(), Game.class);
            if (game == null) {
                throw new RuntimeException("Game is null");
            }
            if (game.theHands() == null) {
                throw new RuntimeException("Hands is null");
            }
            if (game.theHands().size() != LIMIT) {
                throw new RuntimeException("Bad number of hands : " + game.theHands().size());
            }
            if (!game.id.equals(id)) {
                throw new RuntimeException("Game id doesn't match : " + id + " != " + game.id);
            }
            return game;
        }

        @Override
        public List<String> hands(int rounds) {
            String onegame = "http://" + ip + "/games/" + id;
            WS.HttpResponse response = WS.url(onegame).get();
            if (!response.success()) {
                throw new RuntimeException("Retrieve hands failed");
            }
            Game game = new Gson().fromJson(response.getJson(), Game.class);
            if (game == null) {
                throw new RuntimeException("Game is null");
            }
            if (game.theHands() == null) {
                throw new RuntimeException("Hands is null");
            }
            if (game.theHands().size() != LIMIT) {
                throw new RuntimeException("Bad number of hands : " + game.theHands().size());
            }
            if (!game.id.equals(id)) {
                throw new RuntimeException("Game id doesn't match : " + id + " != " + game.id);
            }
            for (String hand : game.theHands()) {
                if (!hand.equals(Game.PAPER) && !hand.equals(Game.ROCK) && !hand.equals(Game.SCISSORS)) throw new RuntimeException("Bad hand value" + hand);
            }
            return game.theHands();
        }

        @Override
        public void submit(Game game) {
            String onegame = "http://" + ip + "/games/" + id;
            WS.HttpResponse response = WS.url(onegame).body(new Gson().toJson(game)).post();
            if (!response.success()) {
                throw new RuntimeException("Finish game failed");
            }
        }

        @Override
        public String getIp() {
            return ip;
        }
    }
}
