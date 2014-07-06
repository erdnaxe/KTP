package me.azenet.KTPPlugin;

import org.bukkit.ChatColor;

/**
 * Quelques outils partiques
 *
 * @author erdnaxe
 */
public class KTPUtils {

    /**
     * Fonction pour formatter un titre dans le chat.
     *
     * @param titre Titre original.
     * @param couleur Couleur des pointillés
     * @return Titre formaté.
     */
    public String createChatTitle(String titre, ChatColor couleur) {
        Integer taillePointillees = ((46 - titre.length()) / 2);
        String chaineSortie = couleur + "";

        for (Integer i = 0; i <= taillePointillees; i++) {
            chaineSortie += "-";
        }

        chaineSortie += ChatColor.WHITE + " " + titre + " " + couleur;

        for (Integer i = 0; i <= taillePointillees; i++) {
            chaineSortie += "-";
        }

        return chaineSortie;
    }

}
