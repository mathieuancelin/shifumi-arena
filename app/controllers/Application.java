package controllers;

import com.google.gson.Gson;
import jobs.Arena;
import jobs.ArenaResult;
import play.mvc.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void logs(Long from) {
        List<Fight> fights = Fight.find("at > ? order by at desc", from).fetch();
        renderJSON(fights);
    }

    public static final ConcurrentLinkedQueue<Arena.Player> players = new ConcurrentLinkedQueue<Arena.Player>();

    public static void submit(String ip, boolean ia) {
        if (players.isEmpty() && !ia) {
            players.offer(new Arena.ActualPlayer(ip));
            renderJSON("{\"waiting\":\"Waiting for another player ...\"}");
        } else if (!players.isEmpty() && !ia) {
            ArenaResult result = await(new Arena(new Arena.ActualPlayer(ip), players.poll()).now());
            renderJSON(result.toJson());
        } else {
            ArenaResult result = await(new Arena(new Arena.ActualPlayer(ip), new Arena.IAPlayer()).now());
            renderJSON(result.toJson());
        }
        ok();
    }

    public static void getAllGames() {
        List<Game> games = Game.findAll();
        renderJSON(games);
    }

    public static void notifyNewGame(String id, int rounds) {
        List<String> hands = Arena.generateHands(rounds);
        Game game = new Game(id, hands);
        game = game.save();
        ok();
    }

    public static void getGame(String id) {
        renderJSON(Game.findById(id));
    }

    public static void finishGame(String id) {
        Game game = new Gson().fromJson(request.params.get("body"), Game.class);
        Game fromDb = Game.findById(id);
        fromDb.rounds = game.rounds;

        fromDb.win = game.win;
        fromDb = fromDb.save();
        ok();
    }

    public static void getAllGamesUI() {
        List<Game> games = Game.findAll();
        int nbrWon = 0;
        int nbrLost = 0;
        for (Game game : games) {
            if (game.win) {
                nbrWon++;
            } else {
                nbrLost++;
            }
        }
        render(games, nbrLost, nbrWon);
    }

    public static void getGameUI(String id) {
        Game game = Game.findById(id);
        if (game != null) {
            render(game);
        } else {
            notFound();
        }
    }
}