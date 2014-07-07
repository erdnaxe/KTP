package me.azenet.KTPPlugin;

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
        }

        try {
            board.getObjective("PlayerHealth").unregister();
        } catch (NullPointerException e) {
        }

        // Création du Scoreboard des vies
        objective = sb.registerNewObjective("PlayerHealth", "health");
        objective.setDisplayName("PlayerHealth");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
    }

}
