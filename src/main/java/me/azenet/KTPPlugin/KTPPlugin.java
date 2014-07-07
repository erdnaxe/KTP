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
    private FileConfiguration config;
    private World world;
    private Scoreboard sb = null;

    private KTPMatchInfo MatchInfo;
    private KTPPlayerHealth PlayerHealth;
    private ShapelessRecipe goldenMelon = null;
    private ShapedRecipe compass = null;

    private Boolean gameRunning = false;
    private Boolean damageIsOn = false;
    private Integer episode = 0;
    private Integer minutesLeft = 0;
    private Integer secondsLeft = 0;

    private final KTPPrompts uhp = new KTPPrompts(this);
    private final LinkedList<Location> loc = new LinkedList<Location>();
    private final ArrayList<KTPTeam> teams = new ArrayList<KTPTeam>();
    private final HashMap<String, ConversationFactory> cfs = new HashMap<String, ConversationFactory>();
    private final HashSet<String> deadPlayers = new HashSet<String>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig(); // On copie le fichier config.yml
        config = this.getConfig(); // On ouvre le fichier config.yml

        // On récupère le monde
        this.world = Bukkit.getWorld(config.getString("map.name"));
        if (this.world == null) {
            throw new IllegalArgumentException("Mauvais nom de monde !");
        }

        // On récupère les positions et on les ajoutes
        List<String> listPositions = config.getStringList("positions");
        for (String positions : listPositions) {
            String[] pos = positions.split(",");
            logger.log(Level.INFO, "[KTPPlugin] Ajout de la cordonn\u00e9e ({0}, {1})", new Object[]{pos[0], pos[1]});
            addLocation(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
        }

        // On ajoute les Listeners
        getServer().getPluginManager().registerEvents(new KTPPluginListener(this), this);

        // Recette du melon doré
        if (config.getBoolean("modifications.recetteGoldenMelon")) {
            goldenMelon = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
            goldenMelon.addIngredient(1, Material.GOLD_BLOCK);
            goldenMelon.addIngredient(1, Material.MELON);
            this.getServer().addRecipe(goldenMelon);
        }

        // Recette de la boussole
        if (config.getBoolean("modifications.boussole")) {
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

        // Récupération du Scoreboard du serveur
        sb = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

        // Création des objectifs
        this.PlayerHealth = new KTPPlayerHealth(sb);
        this.MatchInfo = new KTPMatchInfo(config.getString("scoreboard", "Kill The Patrick"), sb);
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

        // On crée les commandes
        getCommand("ktp").setExecutor(new KTPCommandExecutor(this));

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
            NumberFormat formatter = new DecimalFormat("00");
            BarAPI.setMessage(player,
                    "Temps restant : " + formatter.format(this.minutesLeft) + ":" + formatter.format(this.secondsLeft));
            BarAPI.setHealth(player,
                    this.minutesLeft * 5);
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

    public boolean startGame(CommandSender sender) {
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
            sender.sendMessage(ChatColor.RED + "Pas assez de positions de TP");
            return true;
        }

        LinkedList<Location> unusedTP = loc;
        for (final KTPTeam t : teams) {
            Random random = new Random();
            final Location lo = unusedTP.get(random.nextInt(unusedTP.size()));
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
            }
        }, 20L, 20L);

        Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "--- GO ---");
        this.gameRunning = true;

        return true;
    }

    public boolean generateWalls(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "Génération en cours...");
        try {
            Integer halfMapSize = (int) Math.floor(this.getConfig().getInt("map.size") / 2);
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
            sender.sendMessage(ChatColor.RED + "Echec génération. Voir console pour détails.");
            return true;
        }
        sender.sendMessage(ChatColor.GRAY + "Génération terminée.");
        return true;
    }

    public boolean createTeamGUI(Player pl) {
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
    }

    public boolean setSpawnLocation(CommandSender sender) {
        Player pl = (Player) sender;
        Location pos = pl.getLocation();

        world.setSpawnLocation(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
        sender.sendMessage(ChatColor.GREEN + "Spawn déplacé !");

        return true;
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
        minutesLeft = getEpisodeLength();
        secondsLeft = 0;

        this.episode++;
    }

    public boolean isGameRunning() {
        return this.gameRunning;
    }

    public void addToScoreboard(Player player) {
        // AJout à la barre de temps
        BarAPI.setMessage(player, "Temps restant : --;--");
        BarAPI.setHealth(player, 100);
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
