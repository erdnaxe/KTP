package me.azenet.KTPPlugin;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Classe pour créer un objectif pour voir la vie des joueurs dans la liste des
 * joueurs
 *
 * @author erdnaxe
 */
public final class KTPPlayerHealth {

    private static final Logger logger = Bukkit.getLogger();
    private static final String objectiveName = "PlayerHealth";
    private final Scoreboard board;
    private final Objective objective;

    /**
     * Fonction de construction
     *
     * @param sb Main scoreboard
     */
    public KTPPlayerHealth(Scoreboard sb) {
        board = sb; // On récupère le scoreboard

        // On enlève les anciens objectifs
        try {
            sb.clearSlot(DisplaySlot.PLAYER_LIST);
        } catch (IllegalArgumentException e) {
        } finally {
            logger.info("[KTPPlugin] Des objectifs ont été supprimés dans la liste des joueurs.");
        }

        try {
            sb.getObjective("PlayerHealth").unregister();
        } catch (NullPointerException e) {
        } finally {
            logger.info("[KTPPlugin] Un ancien objectif (PlayerHealth) a été supprimé.");
        }

        // Création du Scoreboard des vies
        objective = sb.registerNewObjective(objectiveName, "health");
        objective.setDisplayName(objectiveName);
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

}
