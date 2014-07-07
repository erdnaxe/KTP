package me.azenet.KTPPlugin;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class KTPTeam {

    private final Scoreboard board;
    private final Team team;
    private final String name;
    private String displayName = "Team sans nom";
    private ChatColor color = ChatColor.WHITE;
    private ArrayList<Player> players = new ArrayList<Player>();

    /**
     * Constructeur de la classe KTPTeam
     *
     * @param n Nom de la team
     * @param sb Scoreboard principal
     */
    public KTPTeam(String n, Scoreboard sb) {
        this.name = n;
        this.board = sb;

        // On supprime les anciennes teams
        try {
            board.getTeam(this.name).unregister();
        } catch (NullPointerException e) {
        }

        // On crée une nouvelle team
        board.registerNewTeam(this.name);
        this.team = board.getTeam(this.name);
        team.setCanSeeFriendlyInvisibles(true);
        this.setDisplayName(this.displayName);
        this.setChatColor(this.color);
    }

    /**
     * Récupérer le nom de la team
     *
     * @return Nom de la team
     */
    public String getName() {
        return name;
    }

    /**
     * Récupérer le nom d'affichage de la team
     *
     * @return Nom d'affichage de la team
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Paramétrer le nom d'affichage de la team
     *
     * @param dn Nouveau nom d'affichage
     */
    public void setDisplayName(String dn) {
        this.displayName = dn;

        Team t = board.getTeam(this.name);
        t.setDisplayName(this.displayName);
    }

    /**
     * Récupérer la liste des joueurs
     *
     * @return Liste des joueurs
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Modifier la liste des joueurs
     *
     * @param l Liste des joueurs
     */
    public void setPlayers(ArrayList<Player> l) {
        players = l;
    }

    /**
     * Ajouter un joueurs à cette team
     *
     * @param playerExact Joueur
     */
    public void addPlayer(Player playerExact) {
        players.add(playerExact);
        board.getTeam(this.name).addPlayer(playerExact);
    }

    /**
     * Téléporter tous les joueurs de cette team
     *
     * @param lo Endroit où téléporter
     */
    public void teleportTo(Location lo) {
        for (Player p : players) {
            p.teleport(lo);
        }
    }

    /**
     * Récupérer la couleur de la team
     *
     * @return Couleur de la team
     */
    public ChatColor getChatColor() {
        return color;
    }

    /**
     * Modifier la couleur de la team
     *
     * @param c Nouvelle couleur
     */
    public void setChatColor(ChatColor c) {
        this.color = c;
        Team t = board.getTeam(this.name);
        t.setPrefix(this.color + "");
    }
}
