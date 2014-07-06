package me.azenet.KTPPlugin;

import java.util.logging.Logger;
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

    private static final Logger logger = Bukkit.getLogger();
    private final Scoreboard board;
    private final Objective objective;
    private Integer episode = 0;
    private Integer nbJoueurs = 0;
    private Integer nbTeams = 0;
    private Integer oldEpisode;
    private Integer oldNbJoueurs;
    private Integer oldNbTeams;

    public KTPMatchInfo(String displayName, Scoreboard sb) {
        board = sb; // On récupère le scoreboard

        // On enlève les anciens objectifs
        try {
            board.clearSlot(DisplaySlot.SIDEBAR);
        } catch (IllegalArgumentException e) {
        } finally {
            logger.info("[KTPPlugin] Des objectifs ont été supprimés dans la sidebar.");
        }

        try {
            board.getObjective("MatchInfo").unregister();
        } catch (NullPointerException e) {
        } finally {
            logger.info("[KTPPlugin] Un ancien objectif (MatchInfo) a été supprimé.");
        }

        // On crée un nouveau objectif
        objective = board.registerNewObjective("MatchInfo", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(displayName);

        // Paramètres de base
        this.refreshMatchInfo();
    }

    public void refreshMatchInfo() {
        if (!episode.equals(oldEpisode)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Episode : " + ChatColor.BOLD + oldEpisode));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Episode : " + ChatColor.BOLD + episode)).setScore(3);

            // On stocke la variable pour une future comparaison
            oldEpisode = episode;

            logger.info("[KTPPlugin] MatchInfo rafraichi car il y a un changement d'épisode...");
        }

        if (!nbJoueurs.equals(oldNbJoueurs)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Joueurs : " + ChatColor.BOLD + oldNbJoueurs));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Joueurs : " + ChatColor.BOLD + nbJoueurs)).setScore(2);

            // On stocke la variable pour une future comparaison
            oldNbJoueurs = nbJoueurs;

            logger.info("[KTPPlugin] MatchInfo rafraichi car il y a un changement de nb de joueurs...");
        }

        if (!nbTeams.equals(oldNbTeams)) {
            // On supprime l'ancienne entrée
            try {
                board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Teams : " + ChatColor.BOLD + oldNbTeams));
            } catch (IllegalArgumentException e) {
            }

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Teams : " + ChatColor.BOLD + nbTeams)).setScore(1);

            // On stocke la variable pour une future comparaison
            oldNbTeams = nbTeams;

            logger.info("[KTPPlugin] MatchInfo rafraichi car il y a un changement de nb de teams...");
        }
    }

    public Integer getEpisode() {
        return episode;
    }

    public void setEpisode(Integer ep) {
        episode = ep;
    }

    public Integer getNbJoueurs() {
        return nbJoueurs;
    }

    public void setNbJoueurs(Integer nb) {
        nbJoueurs = nb;
    }

    public Integer getNbTeams() {
        return nbTeams;
    }

    public void setNbTeams(Integer nb) {
        nbTeams = nb;
    }
}
