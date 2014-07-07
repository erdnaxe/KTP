package me.azenet.KTPPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Classe pour créer un objectif avec les informations du match.
 *
 * @author erdnaxe
 */
public final class KTPMatchInfo {

    private final Scoreboard board;
    private final Objective objective;
    private Integer episode = 0;
    private Integer nbJoueurs = 0;
    private Integer nbTeams = 0;
    private Integer oldEpisode;
    private Integer oldNbJoueurs;
    private Integer oldNbTeams;

    /**
     * Constructeur de cette classe
     *
     * @param displayName Nom qui s'affiche
     * @param sb Scoreboard principal
     */
    public KTPMatchInfo(String displayName, Scoreboard sb) {
        this.board = sb;

        // On enlève les anciens objectifs
        try {
            this.board.clearSlot(DisplaySlot.SIDEBAR);
        } catch (IllegalArgumentException e) {
        }

        try {
            this.board.getObjective("MatchInfo").unregister();
        } catch (NullPointerException e) {
        }

        // On crée un nouveau objectif
        this.objective = board.registerNewObjective("MatchInfo", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(displayName);
        this.refreshMatchInfo();
    }

    /**
     * Fonction pour raffraichir les informations
     */
    public void refreshMatchInfo() {
        if (!episode.equals(oldEpisode)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Episode "
                        + ChatColor.WHITE + oldEpisode));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle & on la stocke pour une futur comparaison
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Episode "
                    + ChatColor.WHITE + episode)).setScore(3);
            oldEpisode = episode;
        }

        if (!nbJoueurs.equals(oldNbJoueurs)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + oldNbJoueurs
                        + ChatColor.GRAY + ChatColor.ITALIC + " joueurs"));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle & on la stocke pour une futur comparaison
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + nbJoueurs
                    + ChatColor.GRAY + ChatColor.ITALIC + " joueurs")).setScore(2);
            oldNbJoueurs = nbJoueurs;
        }

        if (!nbTeams.equals(oldNbTeams)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + oldNbTeams
                        + ChatColor.GRAY + ChatColor.ITALIC + " teams"));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle & on la stocke pour une futur comparaison
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "" + nbTeams
                    + ChatColor.GRAY + ChatColor.ITALIC + " teams")).setScore(1);
            oldNbTeams = nbTeams;
        }
    }

    /**
     * Fonction pour récupérer l'épisode
     *
     * @return Numéro d'épisode
     */
    public Integer getEpisode() {
        return episode;
    }

    /**
     * Fonction pour modifier l'épisode
     *
     * @param ep Numéro d'épisode
     */
    public void setEpisode(Integer ep) {
        episode = ep;
    }

    /**
     * Fonction pour récupérer le nombre de joueurs
     *
     * @return Nombre de joueurs
     */
    public Integer getNbJoueurs() {
        return nbJoueurs;
    }

    /**
     * Fonction pour modifier le nombre de joueurs
     *
     * @param nb Nombre de joueurs
     */
    public void setNbJoueurs(Integer nb) {
        nbJoueurs = nb;
    }

    /**
     * Fonction pour récupérer le nombre de teams
     *
     * @return Nombre de teams
     */
    public Integer getNbTeams() {
        return nbTeams;
    }

    /**
     * Fonction pour modifier le nombre de teams
     *
     * @param nb Nombre de teams
     */
    public void setNbTeams(Integer nb) {
        nbTeams = nb;
    }
}
