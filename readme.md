Shifumi
===============

Le but de cet exercice est de coder une application web permettant de jouer à shifumi.

http://fr.wikipedia.org/wiki/Pierre-feuille-ciseaux

Cependant, ce n'est pas vous qui jouerez mais un bot.

Afin que le bot puisse fonctionner il va falloir lui offrir des APIs REST respectant un certains contrat.

Le déroulement d'un match est le suivant :

une fois que vous pensez vos APIs prêtes,

* connectez vous sur l'arene et vous soumettez votre IP. Préférez le mode IA qui vous affichera les différentes erreurs
* le bot vas commencer par vous notifier qu'un nouveau doit être créé en base. Il vous fournira l'id de ce jeu afin qu'il puisse y accéder par la suite
  * vous devez alors créer un nouveau jeu et le sauvegarder en base
  * vous devez générer les différentes mains à jouer (le bot vous indique le nombre, a vous de déterminer les meilleures stratégies pour gagner au shifumi) au format MAIN:MAIN:MAIN. Les valeurs possibles sont
    * ROCK
    * PAPER
    * SCISSORS
  * n'oubliez pas de tout bien sauvegarder en base avec le bon id
* ensuite le bot va venir lire le jeu via son id vous voir quels mains vous avez joué
* enfin le bot finira le jeu en vous postant le résultat des différents rounds avec votre adversaire ainsi que le gagnant

Vous pouvez soumettre autant de fois que vous voulez pour la mise au point de votre shifumi (en mode IA)

Vous devez également fournir une vue permettant de lister les différents jeux ayant eu lieu ainsi que leur issue

Une fois votre jeu fonctionnel, vous pourrez ensuite tester le match contre un autre étudiant via l'interface de l'arene.

L'application Shifumi respecte le contrat suivant :


```

GET     /games                                  Application.getAllGames
POST    /games                                  Application.notifyNewGame
GET     /games/{id}                             Application.getGame
POST    /games/{id}                             Application.finishGame

GET     /ui/games                               Application.getAllGamesUI
GET     /ui/games/{id}                          Application.getGameUI


```

Le squelette du controleur principal est le suivant :

```java

package controllers;

import com.google.gson.Gson;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    /**
     * Appelé par le bot
     * Renvoi la liste des jeux enregistrés dans la base en JSON
     */
    public static void getAllGames() {
        ...
    }

    /**
     * Appelé par le bot
     * Créé un nouveau jeu dans la base. Ce jeu ne contient pas encore les rounds joués
     * il contient simplement l'id du jeu et les mains à jouer sous la forme
     * ROCK:PAPER:ROCK:PAPER:ROCK
     * Le nombre de mains == rounds
     * renvoi un ok()
     */
    public static void notifyNewGame(String id, int rounds) {
        ...
    }

    /**
     * Appelé par le bot
     * Récupère le jeu avec l'ID spécifié en JSON
     */
    public static void getGame(String id) {
        ...
    }

    /**
     * Appelé par le bot
     * Le bot envoit le jeu complet (avec les rounds) dans le body de la requete
     * Il faut ici le merger avec celui correspondant en base pour le finaliser
     * Renvoi un ok();
     */
    public static void finishGame(String id) {
        Game gameFromBot = new Gson().fromJson(request.params.get("body"), Game.class);
        ...
    }

    /**
     * Appelé depuis votre navigateur
     * Renvoi une vue de tous les jeux que vous avez joué ainsi que les stats
     */
    public static void getAllGamesUI() {
        ...
    }

    /**
     * Appelé depuis votre navigateur
     * Renvoi une vue d'un jeu en particulier
     */
     public static void getGameUI(String id) {
        ...
    }
}

```

Les jeux enregistrés en base se baseront sur le squelette suivant :

```java

package models;

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

    // Voici les seules valeures possibles pour les mains
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

    // Il va falloir ajouter les données suivantes :
    // 
    // * les mains jouées (type String)
    // * le jeu a-t-il été gagné (type boolean)
    // * la liste des rounds (one to many, type List<Round>)

    ...
}


```

Un Round est formé de la façon suivante :

```java

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

}

```

Voici un exemple de round au format JSON :

```json
{
    "yourIp":"127.0.0.1",
    "opponentIp":"192.168.86.2",
    "yourHand":"SCISSORS",
    "opponentHand":"PAPER",
    "win":true
}
```

Voici un exemple de jeu au format JSON :

```json
{
    "id":"fa668bb2-b9f8-4ca9-87df-07bd2762f6e8",
    "hands":"SCISSORS:SCISSORS:SCISSORS:PAPER:ROCK",
    "rounds":[
        {
            "yourIp":"127.0.0.1",
            "opponentIp":"AI Player",
            "yourHand":"SCISSORS",
            "opponentHand":"PAPER",
            "win":true
        },{
            "yourIp":"127.0.0.1",
            "opponentIp":"AI Player",
            "yourHand":"SCISSORS",
            "opponentHand":"SCISSORS",
            "win":false
        },{
            "yourIp":"127.0.0.1",
            "opponentIp":"AI Player",
            "yourHand":"SCISSORS",
            "opponentHand":"PAPER",
            "win":true
        },{
            "yourIp":"127.0.0.1",
            "opponentIp":"AI Player",
            "yourHand":"PAPER",
            "opponentHand":"PAPER",
            "win":false
        },{
            "yourIp":"127.0.0.1",
            "opponentIp":"AI Player",
            "yourHand":"ROCK",
            "opponentHand":"SCISSORS",
            "win":true
        }
    ],
    "win":true
}

```