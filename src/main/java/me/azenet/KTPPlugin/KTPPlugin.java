package me.azenet.KTPPlugin;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Ceci est la classe principale du plugin KTP. Ce plugin a été développé par
 * Azenet. Il a été forké ensuite par erdnaxe pour le rendre compatible et plus
 * documenté.
 *
 * @author erdnaxe & azenet
 */
public final class KTPPlugin extends JavaPlugin implements ConversationAbandonedListener {

    private static final Logger logger = Bukkit.getLogger();
    private FileConfiguration config_yml;
    private final Random random = new Random();
    private final KTPPrompts uhp = new KTPPrompts(this);

    private KTPMatchInfo MatchInfo;
    private KTPPlayerHealth PlayerHealth;

    private final LinkedList<Location> loc = new LinkedList<Location>();
    private ShapelessRecipe goldenMelon = null;
    private ShapedRecipe compass = null;
    private Integer episode = 0;
    private Boolean gameRunning = false;

    private Scoreboard sb = null;

    private Integer minutesLeft = 0;
    private Integer secondsLeft = 0;
    private final NumberFormat formatter = new DecimalFormat("00");
    private Boolean damageIsOn = false;
    private final ArrayList<KTPTeam> teams = new ArrayList<KTPTeam>();
    private final HashMap<String, ConversationFactory> cfs = new HashMap<String, ConversationFactory>();
    private final HashSet<String> deadPlayers = new HashSet<String>();
    private World world;

    @Override
    public void onEnable() {
        // On copie le fichier config.yml
        this.saveDefaultConfig();

        // On ouvre le fichier config.yml
        config_yml = this.getConfig();

        // On récupère le monde
        String mapName = config_yml.getString("map.name");
        logger.log(Level.INFO, "[KTPPlugin] Chargement pour la map : {0}", mapName);
        this.world = Bukkit.getWorld(mapName);

        // On récupère les positions et on les ajoutes
        List<String> listPositions = config_yml.getStringList("positions");
        for (String positions : listPositions) {
            String[] pos = positions.split(",");
            logger.log(Level.INFO, "[KTPPlugin] Ajout de la cordonn\u00e9e {0},{1}", new Object[]{pos[0], pos[1]});
            addLocation(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
        }

        // On ajoute les Listeners
        getServer().getPluginManager().registerEvents(new KTPPluginListener(this), this);

        // Recette du melon doré
        try {
            goldenMelon = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
            goldenMelon.addIngredient(1, Material.GOLD_BLOCK);
            goldenMelon.addIngredient(1, Material.MELON);
            this.getServer().addRecipe(goldenMelon);
        } finally {
            logger.info("[KTPPlugin] Recette du melon doré changé !");
        }

        // Recette de la boussole
        try {
            if (config_yml.getBoolean("compass")) {
                compass = new ShapedRecipe(new ItemStack(Material.COMPASS));
                compass.shape(new String[]{"CIE", "IRI", "BIF"});
                compass.setIngredient('I', Material.IRON_INGOT);
                compass.setIngredient('R', Material.REDSTONE);
                compass.setIngredient('C', Material.SULPHUR);
                compass.setIngredient('E', Material.SPIDER_EYE);
                compass.setIngredient('B', Material.BONE);
                compass.setIngredient('F', Material.ROTTEN_FLESH);
                this.getServer().addRecipe(compass);
            }
        } finally {
            logger.info("[KTPPlugin] Recette de la boussole changé !");
        }

        // Récupération du Scoreboard du serveur
        sb = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

        // Création des objectifs
        this.PlayerHealth = new KTPPlayerHealth(sb);
        this.MatchInfo = new KTPMatchInfo("KTP", sb);
        this.setMatchInfo();

        // Création de la barre de temps
        this.setTimeBarInfo();

        // On créer un environnement de début
        getServer().getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
        getServer().getWorlds().get(0).setTime(6000L);
        getServer().getWorlds().get(0).setStorm(false);
        getServer().getWorlds().get(0).setDifficulty(Difficulty.PEACEFUL);

        // A découvrir plus tard
        cfs.put("teamPrompt", new ConversationFactory(this)
                .withModality(true)
                .withFirstPrompt(uhp.getTNP())
                .withEscapeSequence("/cancel")
                .thatExcludesNonPlayersWithMessage("Il faut être un joueur ingame.")
                .withLocalEcho(false)
                .addConversationAbandonedListener(this));

        cfs.put("playerPrompt", new ConversationFactory(this)
                .withModality(true)
                .withFirstPrompt(uhp.getPP())
                .withEscapeSequence("/cancel")
                .thatExcludesNonPlayersWithMessage("Il faut être un joueur ingame.")
                .withLocalEcho(false)
                .addConversationAbandonedListener(this));

        logger.info("[KTPPlugin] KTPPlugin est maintenant chargé");
    }

    @Override
    public void onDisable() {
        logger.info("[KTPPlugin] KTPPlugin déchargé");
    }

    /**
     * Ajouter un endroit de spawn pour le KTP.
     *
     * @param x
     * @param z
     */
    public void addLocation(int x, int z) {
        loc.add(new Location(getServer().getWorlds().get(0), x, getServer().getWorlds().get(0).getHighestBlockYAt(x, z) + 120, z));
    }

    /**
     * Modifier la taille du terrain
     *
     * @param size Nouvelle taille
     */
    public void setSize(int size) {
        try {
            Integer halfMapSize = (int) Math.floor(size / 2);
            Integer wallHeight = this.getConfig().getInt("map.wall.height");
            Material wallBlock = Material.getMaterial(this.getConfig().getInt("map.wall.block"));

            Location spawn = world.getSpawnLocation();
            Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();

            spawn = world.getSpawnLocation();
            Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();

            spawn = world.getSpawnLocation();
            Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();

            spawn = world.getSpawnLocation();
            Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();

            for (Integer x = limitXInf; x <= limitXSup; x++) {
                world.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
                world.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);
                for (Integer y = 2; y <= wallHeight; y++) {
                    world.getBlockAt(x, y, limitZInf).setType(wallBlock);
                    world.getBlockAt(x, y, limitZSup).setType(wallBlock);
                }
            }

            for (Integer z = limitZInf; z <= limitZSup; z++) {
                world.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
                world.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);
                for (Integer y = 2; y <= wallHeight; y++) {
                    world.getBlockAt(limitXInf, y, z).setType(wallBlock);
                    world.getBlockAt(limitXSup, y, z).setType(wallBlock);
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Actualise la valeur de la barre de temps suivant le temps restant
     */
    public void setTimeBarInfo() {
        // Pour chaque joueur
        Player[] onlinePlayerList = Bukkit.getServer().getOnlinePlayers();
        for (Player player : onlinePlayerList) {
            BarAPI.setMessage(player, "Temps restant : " + formatter.format(this.minutesLeft) + ":" + formatter.format(this.secondsLeft));
            BarAPI.setHealth(player, this.minutesLeft * 5);
        }
    }

    /**
     * Actualiste les informations de la sidebar
     */
    public void setMatchInfo() {
        this.MatchInfo.setEpisode(episode);
        this.MatchInfo.setNbJoueurs(Bukkit.getServer().getOnlinePlayers().length);
        this.MatchInfo.setNbTeams(getAliveTeams().size());
        this.MatchInfo.refreshMatchInfo();
    }

    private ArrayList<KTPTeam> getAliveTeams() {
        ArrayList<KTPTeam> aliveTeams = new ArrayList<KTPTeam>();
        for (KTPTeam t : teams) {
            for (Player p : t.getPlayers()) {
                if (p.isOnline() && !aliveTeams.contains(t)) {
                    aliveTeams.add(t);
                }
            }
        }
        return aliveTeams;
    }

    @Override
    public boolean onCommand(final CommandSender s, Command c, String l, String[] a) {
        if (c.getName().equalsIgnoreCase("ktp")) {
            if (!(s instanceof Player)) {
                s.sendMessage(ChatColor.RED + "Vous devez être un joueur");
                return true;
            }
            Player pl = (Player) s;
            if (!pl.isOp()) {
                pl.sendMessage(ChatColor.RED + "Lolnope.");
                return true;
            }
            if (a.length == 0) {
                pl.sendMessage("Usage : /ktp <start|shift|size|team|addspawn|generatewalls>");
                return true;
            }
            if (a[0].equalsIgnoreCase("start")) {
                if (teams.isEmpty()) {
                    for (Player p : getServer().getOnlinePlayers()) {
                        KTPTeam uht = new KTPTeam(this.sb);
                        uht.setName(p.getName());
                        uht.setDisplayName(p.getName());
                        
                        uht.addPlayer(p);
                        teams.add(uht);
                    }
                }
                if (loc.size() < teams.size()) {
                    s.sendMessage(ChatColor.RED + "Pas assez de positions de TP");
                    return true;
                }
                LinkedList<Location> unusedTP = loc;
                for (final KTPTeam t : teams) {
                    final Location lo = unusedTP.get(this.random.nextInt(unusedTP.size()));
                    Bukkit.getScheduler().runTaskLater(this, new BukkitRunnable() {

                        @Override
                        public void run() {
                            t.teleportTo(lo);
                            for (Player p : t.getPlayers()) {
                                p.setGameMode(GameMode.SURVIVAL);
                                p.setHealth(20);
                                p.setFoodLevel(20);
                                p.setExhaustion(5F);
                                p.getInventory().clear();
                                p.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                                    new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
                                p.setExp(0L + 0F);
                                p.setLevel(0);
                                p.closeInventory();
                                p.getActivePotionEffects().clear();
                                p.setCompassTarget(lo);
                                setLife(p, 20);
                            }
                        }
                    }, 10L);

                    unusedTP.remove(lo);
                }
                Bukkit.getScheduler().runTaskLater(this, new BukkitRunnable() {

                    @Override
                    public void run() {
                        damageIsOn = true;
                    }
                }, 600L);

                world.setGameRuleValue("doDaylightCycle", ((Boolean) getConfig().getBoolean("daylightCycle.do")).toString());
                world.setTime(getConfig().getLong("daylightCycle.time"));
                world.setStorm(false);
                world.setDifficulty(Difficulty.HARD);
                this.episode = 1;
                this.minutesLeft = getEpisodeLength();
                this.secondsLeft = 0;

                // Gestion de la barre de temps
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
                    @Override
                    public void run() {
                        setTimeBarInfo();
                        secondsLeft--;
                        if (secondsLeft == -1) {
                            minutesLeft--;
                            secondsLeft = 59;
                        }
                        if (minutesLeft == -1) {
                            minutesLeft = getEpisodeLength();
                            secondsLeft = 0;
                            Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "-------- Fin episode " + episode + " --------");
                            shiftEpisode();
                        }
                    }
                }, 20L, 20L);

                // Gestion du Scoreboard
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
                    @Override
                    public void run() {
                        setMatchInfo();
                        secondsLeft--;
                        if (secondsLeft == -1) {
                            minutesLeft--;
                            secondsLeft = 59;
                        }
                        if (minutesLeft == -1) {
                            minutesLeft = getEpisodeLength();
                            secondsLeft = 0;
                            Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "-------- Fin episode " + episode + " --------");
                            shiftEpisode();
                        }
                    }
                }, 200L, 200L);

                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "--- GO ---");
                this.gameRunning = true;
                return true;
            } else if (a[0].equalsIgnoreCase("shift")) {
                Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "-------- Fin episode " + episode + " [forcé par " + s.getName() + "] --------");
                shiftEpisode();
                this.minutesLeft = getEpisodeLength();
                this.secondsLeft = 0;
                return true;
            } else if (a[0].equalsIgnoreCase("size")) {
                Bukkit.getServer().broadcastMessage(ChatColor.RED + "-------- Changement de taille : " + a[1] + " [forcé par " + s.getName() + "] --------");
                setSize(Integer.parseInt(a[1]));
                return true;
            } else if (a[0].equalsIgnoreCase("team")) {

                // Création d'un inventaire
                Inventory iv = this.getServer().createInventory(pl, 54, "Liste des teams");

                // Liste des teams disponibles
                ItemStack is;
                Integer slot = 0;
                for (KTPTeam t : teams) {
                    is = new ItemStack(Material.BEACON, t.getPlayers().size());
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(t.getChatColor() + t.getDisplayName());
                    ArrayList<String> lore = new ArrayList<String>();
                    for (Player p : t.getPlayers()) {
                        lore.add("- " + p.getDisplayName());
                    }
                    im.setLore(lore);
                    is.setItemMeta(im);
                    iv.setItem(slot, is);
                    slot++;
                }

                // Création d'un diamant
                ItemStack is2 = new ItemStack(Material.DIAMOND);
                is2.getItemMeta().setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Créer une team");
                iv.setItem(53, is2);

                // Affichage de l'inventaire
                pl.openInventory(iv);
                return true;

            } else if (a[0].equalsIgnoreCase("addspawn")) {
                addLocation(pl.getLocation().getBlockX(), pl.getLocation().getBlockZ());
                pl.sendMessage(ChatColor.DARK_GRAY + "Position ajoutée: " + ChatColor.GRAY + pl.getLocation().getBlockX() + "," + pl.getLocation().getBlockZ());
                return true;
            } else if (a[0].equalsIgnoreCase("generateWalls")) {
                pl.sendMessage(ChatColor.GRAY + "Génération en cours...");
                try {
                    Integer halfMapSize = (int) Math.floor(this.getConfig().getInt("map.size") / 2);
                    Integer wallHeight = this.getConfig().getInt("map.wall.height");
                    Material wallBlock = Material.getMaterial(this.getConfig().getInt("map.wall.block"));
                    World w = pl.getWorld();

                    Location spawn = w.getSpawnLocation();
                    Integer limitXInf = spawn.add(-halfMapSize, 0, 0).getBlockX();

                    spawn = w.getSpawnLocation();
                    Integer limitXSup = spawn.add(halfMapSize, 0, 0).getBlockX();

                    spawn = w.getSpawnLocation();
                    Integer limitZInf = spawn.add(0, 0, -halfMapSize).getBlockZ();

                    spawn = w.getSpawnLocation();
                    Integer limitZSup = spawn.add(0, 0, halfMapSize).getBlockZ();

                    for (Integer x = limitXInf; x <= limitXSup; x++) {
                        w.getBlockAt(x, 1, limitZInf).setType(Material.BEDROCK);
                        w.getBlockAt(x, 1, limitZSup).setType(Material.BEDROCK);
                        for (Integer y = 2; y <= wallHeight; y++) {
                            w.getBlockAt(x, y, limitZInf).setType(wallBlock);
                            w.getBlockAt(x, y, limitZSup).setType(wallBlock);
                        }
                    }

                    for (Integer z = limitZInf; z <= limitZSup; z++) {
                        w.getBlockAt(limitXInf, 1, z).setType(Material.BEDROCK);
                        w.getBlockAt(limitXSup, 1, z).setType(Material.BEDROCK);
                        for (Integer y = 2; y <= wallHeight; y++) {
                            w.getBlockAt(limitXInf, y, z).setType(wallBlock);
                            w.getBlockAt(limitXSup, y, z).setType(wallBlock);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    pl.sendMessage(ChatColor.RED + "Echec génération. Voir console pour détails.");
                    return true;
                }
                pl.sendMessage(ChatColor.GRAY + "Génération terminée.");
                return true;
            }
        }
        return false;
    }
    /*
     public void createTeamCreateInventory(Player pl) {
     // Création d'un inventaire
     Inventory iv = this.getServer().createInventory(pl, 54, "Création d'une team");

     // Liste des teams disponibles
     ItemStack is;
     Integer slot = 0;

     for (KTPTeam t : teams) {
     is = new ItemStack(Material.BEACON, t.getPlayers().size());
     ItemMeta im = is.getItemMeta();
     im.setDisplayName(t.getChatColor() + t.getDisplayName());
     ArrayList<String> lore = new ArrayList<String>();
     for (Player p : t.getPlayers()) {
     lore.add("- " + p.getDisplayName());
     }
     im.setLore(lore);
     is.setItemMeta(im);
     iv.setItem(slot, is);
     slot++;
     }

     // Création d'un diamant
     ItemStack is2 = new ItemStack(Material.DIAMOND);
     is2.getItemMeta().setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Créer une team");
     iv.setItem(53, is2);

     // Affichage de l'inventaire
     pl.openInventory(iv);
     }
     */

    public void shiftEpisode() {
        this.episode++;
    }

    public boolean isGameRunning() {
        return this.gameRunning;
    }

    public void updatePlayerListName(Player p) {
        /*p.setScoreboard(sb);
        Integer he = (int) Math.round(p.getHealth());
        sb.getObjective("Vie").getScore(p).setScore(he);*/
    }

    public void addToScoreboard(Player player) {
        updatePlayerListName(player);

        // AJout à la barre de temps
        BarAPI.setMessage(player, "Temps restant : --;--");
        BarAPI.setHealth(player, 100);
    }

    public void setLife(Player entity, int i) {
        entity.setScoreboard(sb);
        sb.getObjective("Vie").getScore(entity).setScore(i);
    }

    public boolean isTakingDamage() {
        return damageIsOn;
    }

    public Scoreboard getScoreboard() {
        return sb;
    }

    public KTPTeam getTeam(String name) {
        for (KTPTeam t : teams) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public KTPTeam getTeamForPlayer(Player p) {
        for (KTPTeam t : teams) {
            if (t.getPlayers().contains(p)) {
                return t;
            }
        }
        return null;
    }

    public Integer getEpisodeLength() {
        return this.getConfig().getInt("episodeLength");
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
        if (!abandonedEvent.gracefulExit()) {
            abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.RED + "Abandonné par " + abandonedEvent.getCanceller().getClass().getName());
        }
    }

    public boolean createTeam(String name, ChatColor color) {
        if (teams.size() <= 50) {
            KTPTeam cTeam = new KTPTeam(this.sb);
            cTeam.setName(name);
            cTeam.setDisplayName(name);
            cTeam.setChatColor(color);
            teams.add(cTeam);
            return true;
        }
        return false;
    }

    public ConversationFactory getConversationFactory(String string) {
        if (cfs.containsKey(string)) {
            return cfs.get(string);
        }
        return null;
    }

    public boolean isPlayerDead(String name) {
        return deadPlayers.contains(name);
    }

    public void addDead(String name) {
        deadPlayers.add(name);
    }

    public String getScoreboardName() {
        String s = this.getConfig().getString("scoreboard", "KTP");
        return s.substring(0, Math.min(s.length(), 16));
    }

    public boolean inSameTeam(Player pl, Player pl2) {
        return (getTeamForPlayer(pl).equals(getTeamForPlayer(pl2)));
    }
}
