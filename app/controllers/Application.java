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
}