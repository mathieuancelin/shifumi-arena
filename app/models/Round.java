package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

@Entity
public class Round extends Model {

    public String yourIp;
    public String opponentIp;
    public String yourHand;
    public String opponentHand;
    public Boolean win;

    public Round() {}

    public Round(String yourIp, String opponentIp, String yourHand, String opponentHand, Boolean win) {
        this.yourIp = yourIp;
        this.opponentIp = opponentIp;
        this.yourHand = yourHand;
        this.opponentHand = opponentHand;
        this.win = win;
    }
}
