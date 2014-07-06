package me.azenet.KTPPlugin;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Classe pour créer un Scoreboard avec les informations du match.
 *
 * @author erdnaxe
 */
public class KTPMatchInfo {

    private final Scoreboard board;
    private final Objective objective;
    private Integer episode = 0;
    private Integer nbJoueurs = 0;
    private Integer nbTeams = 0;
    private Integer oldEpisode = 0;
    private Integer oldNbJoueurs = 0;
    private Integer oldNbTeams = 0;

    private static final Logger logger = Bukkit.getLogger();

    public KTPMatchInfo(String displayName, KTPPlugin mainClass) {
        // On récupère le manager de la classe principale
        board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

        // Nouveau objectif
        objective = board.registerNewObjective("MatchInfo", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(displayName);
    }

    public void refreshMatchInfo() {
        if (!episode.equals(oldEpisode)) {
            // On supprime l'ancienne entrée
           // board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Episode : " + ChatColor.BOLD + oldEpisode));

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Episode : " + ChatColor.BOLD + episode)).setScore(3);

            // On stocke la variable pour une future comparaison
            oldEpisode = episode;

            logger.info("MatchInfo rafraichi car il y a un changement d'épisode...");
        }

        if (!nbJoueurs.equals(oldNbJoueurs)) {
            // On supprime l'ancienne entrée
            //board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Joueurs : " + ChatColor.BOLD + oldNbJoueurs));

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Joueurs : " + ChatColor.BOLD + nbJoueurs)).setScore(2);

            // On stocke la variable pour une future comparaison
            oldNbJoueurs = nbJoueurs;

            logger.info("MatchInfo rafraichi car il y a un changement de nb de joueurs...");
        }

        if (!nbTeams.equals(oldNbTeams)) {
            // On supprime l'ancienne entrée
            //board.resetScores(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Teams : " + ChatColor.BOLD + oldNbTeams));

            // On ajoute la nouvelle
            objective.getScore(Bukkit.getOfflinePlayer(ChatColor.WHITE + "Teams : " + ChatColor.BOLD + nbTeams)).setScore(1);

            // On stocke la variable pour une future comparaison
            oldNbTeams = nbTeams;

            logger.info("MatchInfo rafraichi car il y a un changement de nb de teams...");
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
