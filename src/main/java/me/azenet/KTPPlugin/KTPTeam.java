package me.azenet.KTPPlugin;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class KTPTeam {

    private String name;
    private String displayName;
    private ChatColor color;
    private final KTPPlugin plugin;
    private ArrayList<Player> players = new ArrayList<Player>();

    public KTPTeam(String name, String displayName, ChatColor color, KTPPlugin plugin) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.plugin = plugin;

        Scoreboard sb = this.plugin.getScoreboard();
        sb.registerNewTeam(this.name);

        Team t = sb.getTeam(this.name);
        t.setDisplayName(this.displayName);
        t.setCanSeeFriendlyInvisibles(true);
        t.setPrefix(this.color + "");
    }

    public String getName() {
        return name;
    }

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
        plugin.getScoreboard().getTeam(this.name).addPlayer(playerExact);
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
