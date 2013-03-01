package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class PetConfig {

    private final Configuration config;
    private final PetMain plugin;
    private static HashMap<String, ItemStack> baitMap = new HashMap<String, ItemStack>();
    private static HashMap<String, Integer> tamingXPMap = new HashMap<String, Integer>();
    public static boolean provokable;
    public static boolean ridable;
    public static boolean attackTame;
    public static int idleDistance;
    public static int attackDistance;
    public static int maxPetsPerPlayer;
    public static boolean opsBypassPerms;
    public static boolean PetsAttackPlayers;
    public static boolean petsAttackPets;
    public static boolean invinciblePets;
    public static long mainLoop;
    public static boolean disablePermissions;
    public static String defaultPetMode;
    public static String commandPrefix;
    public static int maxSpawnCount;
    public static boolean overrideDefaultTaming;
    public static ArrayList<String> nameFiles = new ArrayList<String>();
    public static boolean randomizePetNames;
    public static boolean mcMMOSuport;    
    public static boolean rememberPetLocation;
    public static boolean noDropOnKillCommand;
    public static String defaultPetAge;
    public static boolean lockSpawnedBabies;
    public static final String mobs[] = {
        "Bat",
        "Blaze",
        "CaveSpider",
        "Chicken",
        "Cow",
        "Creeper",
        "EnderDragon",
        "Enderman",
        "Ghast",
        "Giant",
        "Golem",
        "MagmaCube",
        "MushroomCow",
        "Ozelot",
        "Pig",
        "PigZombie",
        "Sheep",
        "Silverfish",
        "Skeleton",
        "Slime",
        "SnowMan",
        "Spider",
        "Squid",
        "Villager",
        "VillagerGolem",
        "Witch",
        "WitherBoss",
        "Wolf",
        "Zombie"
    };

    public PetConfig(PetMain instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
        load();
    }

    public ItemStack getMat(String s, String mobName) {
        String matName;
        ItemStack item = new ItemStack(0);
        Material mat = Material.AIR;
        if (s == null) {            
            return item;
        }         
        byte byteCode = (byte)0;        
        if (s.contains(":")) {
            String tmp[] = s.split(":",2);
            matName = tmp[0]; 
            if (tmp[1].matches("\\d+")) {
                byteCode = (byte)Integer.parseInt(tmp[1]);
            }
        } else { 
            matName = s;
        }
        if (matName.matches("\\d+")) {
            mat = Material.getMaterial(Integer.parseInt(matName));
        } else if (Material.matchMaterial(matName) != null) {
            mat = Material.matchMaterial(matName);
        } else {
            plugin.logInfo("Invalid bait: " + matName);
        }  
        if (mat != null) {
            item = new ItemStack(mat,1,byteCode);
            plugin.logInfo("[" + mobName + "] [" + s + "] [" + item.getType().toString() + "]");
        }
        return item;
    }
    
    public void load() {
        for (String s : mobs) {
            baitMap.put(s, getMat(config.getString(s),s));
            tamingXPMap.put(s, (config.getInt("mcMMOTamingXP." + s)));
        }        

        provokable = config.getBoolean("Provokable", true);
        ridable = config.getBoolean("Ridable", true);
        attackTame = config.getBoolean("AttackTame", false);
        idleDistance = config.getInt("IdleDistance", 5);
        attackDistance = config.getInt("AttackDistance", 10);
        maxPetsPerPlayer = config.getInt("MaxPetsPerPlayer", 1);
        opsBypassPerms = config.getBoolean("OpsBypassPerms", false);
        PetsAttackPlayers = config.getBoolean("PetsAttackPets", true);
        petsAttackPets = config.getBoolean("PetsAttackPets", true);
        mainLoop = config.getLong("MainLoop", 1000L);
        disablePermissions = config.getBoolean("DisablePermissions", false);
        defaultPetMode = config.getString("DefaultPetMode", "P").toUpperCase();
        invinciblePets = config.getBoolean("InvinciblePets", true);
        maxSpawnCount = config.getInt("MaxSpawnCount", 1);
        overrideDefaultTaming = config.getBoolean("OverrideDefaultTaming", true);
        nameFiles = (ArrayList)config.getStringList("NameFiles");
        randomizePetNames = config.getBoolean("RandomizePetNames", true);
        mcMMOSuport = config.getBoolean("mcMMOSuport", true);     
        rememberPetLocation = config.getBoolean("RememberPetLocation", false);
        noDropOnKillCommand = config.getBoolean("NoDropOnKillCommand", false);
        defaultPetAge = config.getString("DefaultPetAge", "adult");
        lockSpawnedBabies = config.getBoolean("LockSpawnedBabies",false);
        
        commandPrefix = config.getString("CommandPrefix", "pet");
    }

    public static ItemStack getBait(EntityType type) {        
        if (baitMap.containsKey(type.getName())) {
            return baitMap.get(type.getName());
        } else {
            return new ItemStack(0);
        }
    }
    
    public static Integer getTamingXP(EntityType type) {        
        if (tamingXPMap.containsKey(type.getName())) {
            return tamingXPMap.get(type.getName());
        } else {
            return 0;
        }
    }
}
