package me.azenet.KTPPlugin;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class KTPTeam {

    private final Scoreboard board;
    private String name = "Team";
    private String displayName = "Team";
    private ChatColor color = ChatColor.WHITE;
    private ArrayList<Player> players = new ArrayList<Player>();

    /**
     * Constructeur de la classe KTPTeam
     * @param sb Main scoreboard
     */
    public KTPTeam(Scoreboard sb) {
        board = sb; // On récupère le scoreboard

        // On supprime les anciennes teams
        try {
            board.getTeam(this.name).unregister();
        } catch (IllegalArgumentException e) {
        }

        // On crée une nouvelle team
        board.registerNewTeam(this.name);

        // On paramètre la team
        Team t = board.getTeam(this.name);
        t.setDisplayName(this.displayName);
        t.setCanSeeFriendlyInvisibles(true);
        t.setPrefix(this.color + "");
    }

    /**
     * Récupérer le nom de la team.
     * @return Nom de la team
     */
    public String getName() {
        return name;
    }

    /**
     * Modifie le nom de la team.
     * @param n Nom de la team.
     */
    public void setName(String n) {
        name = n;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String n) {
        displayName = n;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> l) {
        players = l;
    }

    public ArrayList<Player> getPlayer() {
        return players;
    }

    public void addPlayer(Player playerExact) {
        players.add(playerExact);
        board.getTeam(this.name).addPlayer(playerExact);
    }

    public void teleportTo(Location lo) {
        for (Player p : players) {
            p.teleport(lo);
        }
    }

    public ChatColor getChatColor() {
        return color;
    }

    public void setChatColor(ChatColor c) {
        color = c;
    }
}
