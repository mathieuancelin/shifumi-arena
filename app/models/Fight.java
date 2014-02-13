package models;

import com.google.gson.Gson;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class Fight extends Model {

    @Lob
    public String playerFight;
    @Lob
    public String counterPLayerFight;
    public Boolean playerWin;
    public Boolean counterPlayerWin;
    public String playerIp;
    public String counterPlayerIp;
    public String error;
    public Long at;

    public Fight(Game playerFight, Game counterPLayerFight, Boolean playerWin, Boolean counterPlayerWin, String playerIp, String cplayerIp, String error, Long at) {

        if (playerFight != null) this.playerFight = new Gson().toJson(playerFight);
        if (counterPLayerFight != null) this.counterPLayerFight = new Gson().toJson(counterPLayerFight);
        this.at = at;
        this.playerWin = playerWin;
        this.counterPlayerWin = counterPlayerWin;
        this.error = error;
        this.playerIp = playerIp;
        this.counterPlayerIp = cplayerIp;
    }
}
