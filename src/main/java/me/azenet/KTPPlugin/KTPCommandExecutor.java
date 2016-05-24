package me.azenet.KTPPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Gestion des commandes du plugin
 *
 * @author erdnaxe
 */
public class KTPCommandExecutor implements CommandExecutor {

    private final KTPPlugin plugin;
    private final KTPUtils tools;

    public KTPCommandExecutor(KTPPlugin pl) {        
        // On récupère la classe du plugin
        plugin = pl;
        
        // On récupère les outils
        tools = new KTPUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ktp")) {

            // Afficher l'aide si aucun paramètre n'est donné.
            if (args.length == 0) {
                sender.sendMessage(tools.createChatTitle("Aide : KTP", ChatColor.YELLOW));
                sender.sendMessage(ChatColor.GOLD + "/ktp start : " + ChatColor.WHITE + "Démarre le jeu");
                sender.sendMessage(ChatColor.GOLD + "/ktp shift : " + ChatColor.WHITE + "Saute un épisode");
                sender.sendMessage(ChatColor.GOLD + "/ktp addgamespawn : " + ChatColor.WHITE + "Ajoute un point de spawn");
                sender.sendMessage(ChatColor.GOLD + "/ktp setspawn : " + ChatColor.WHITE + "Modifie le centre de la map");
                sender.sendMessage(ChatColor.GOLD + "/ktp setsize <size> : " + ChatColor.WHITE + "Modifie la taille de la map");
                sender.sendMessage(ChatColor.GOLD + "/ktp generatewalls : " + ChatColor.WHITE + "Crée un mur autour de la map");
                sender.sendMessage(ChatColor.GOLD + "/ktp addteam : " + ChatColor.WHITE + "Ajoute une équipe");
                sender.sendMessage(ChatColor.DARK_BLUE + "Don't forget that you must be a player to issue one of these commands !" + ChatColor.WHITE + "");
                return true;
            }

            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                sender.sendMessage(ChatColor.GOLD + "To get help, enter " + ChatColor.WHITE + "/ktp" + ChatColor.GOLD + " alone.");
                return true;
            }

            // On vérifit que l'exécuteur est un joueur
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Vous devez etre un joueur");
                return true;
            }

            // Si /ktp start, alors on démarre le jeu
            if (args[0].equalsIgnoreCase("start")) {
                // On démarre
                plugin.startGame(sender);

                // Réponse
                Bukkit.getServer().broadcastMessage(tools.createChatTitle("GO !!!", ChatColor.GREEN));
                return true;
            }

            // Si /ktp shift, alors on saute un épisode
            if (args[0].equalsIgnoreCase("shift")) {
                // On saute l'épisode
                plugin.shiftEpisode();

                // Réponse
                Bukkit.getServer().broadcastMessage(tools.createChatTitle("[forcé par " + sender.getName() + "]", ChatColor.AQUA));
                return true;
            }

            // Si /ktp addgamespawn, alors on crée une positions en plus
            if (args[0].equalsIgnoreCase("addgamespawn")) {
                Player pl = (Player) sender;
                Location loc = pl.getLocation();

                // On ajoute la position
                plugin.addLocation(loc.getBlockX(), loc.getBlockZ());

                // Réponse
                pl.sendMessage(ChatColor.DARK_GRAY + "Position ajoutée: " + ChatColor.GRAY + pl.getLocation().getBlockX() + "," + pl.getLocation().getBlockZ());
                return true;
            }

            // SI /ktp setspawn, alors on change le spawn du monde
            if (args[0].equalsIgnoreCase("setspawn")) {
                return plugin.setSpawnLocation(sender);
            }

            // Si /ktp setsize, alors on change la taille du monde
            if (args[0].equalsIgnoreCase("setsize")) {
                // On modifie la taille
                plugin.setSize(Integer.parseInt(args[1]));

                // Réponse
                Bukkit.getServer().broadcastMessage(tools.createChatTitle("Changement de taille [" + args[1] + "]", ChatColor.GOLD));
                return true;
            }

            if (args[0].equalsIgnoreCase("generateWalls")) {
                // On génère les murs
                return plugin.generateWalls(sender);
            }

            if (args[0].equalsIgnoreCase("addteam")) {
                // On ouvre la GUI pour créer une Team
                return plugin.createTeamGUI((Player) sender);
            }
        }
        return false;
    }

}
