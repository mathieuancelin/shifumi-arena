package models;

import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
public class Game extends GenericModel {

    public static String ROCK = "ROCK";
    public static String PAPER = "PAPER";
    public static String SCISSORS = "SCISSORS";

    @Id
    public String id;

    public String getId() {
        return id;
    }

    @Override
    public Object _key() {
        return getId();
    }

    public String hands;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Round> rounds;

    public Boolean win;

    public List<String> theHands() {
        return Arrays.asList(hands.split(":"));
    }

    public Game(String id, List<String> hands) {
        this.id = id;
        this.hands = StringUtils.join(hands, ":");
        this.win = false;
        this.rounds = new ArrayList<Round>();
    }
}
