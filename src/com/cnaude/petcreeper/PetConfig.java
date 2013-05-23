package com.cnaude.petcreeper;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class PetConfig {
    
    private final PetCreeper plugin;
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
    public static boolean customNamePlates;
    public static String namePlateColor;
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

    public PetConfig(final PetCreeper plugin) {
        this.plugin = plugin;
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
            baitMap.put(s, getMat(plugin.getConfig().getString(s),s));
            tamingXPMap.put(s, (plugin.getConfig().getInt("mcMMOTamingXP." + s)));
        }

        provokable = plugin.getConfig().getBoolean("Provokable", true);
        ridable = plugin.getConfig().getBoolean("Ridable", true);
        attackTame = plugin.getConfig().getBoolean("AttackTame", false);
        idleDistance = plugin.getConfig().getInt("IdleDistance", 5);
        attackDistance = plugin.getConfig().getInt("AttackDistance", 10);
        maxPetsPerPlayer = plugin.getConfig().getInt("MaxPetsPerPlayer", 1);
        opsBypassPerms = plugin.getConfig().getBoolean("OpsBypassPerms", false);
        PetsAttackPlayers = plugin.getConfig().getBoolean("PetsAttackPets", true);
        petsAttackPets = plugin.getConfig().getBoolean("PetsAttackPets", true);
        mainLoop = plugin.getConfig().getLong("MainLoop", 1000L);
        disablePermissions = plugin.getConfig().getBoolean("DisablePermissions", false);
        defaultPetMode = plugin.getConfig().getString("DefaultPetMode", "P").toUpperCase();
        invinciblePets = plugin.getConfig().getBoolean("InvinciblePets", true);
        maxSpawnCount = plugin.getConfig().getInt("MaxSpawnCount", 1);
        overrideDefaultTaming = plugin.getConfig().getBoolean("OverrideDefaultTaming", true);
        nameFiles = (ArrayList)plugin.getConfig().getStringList("NameFiles");
        randomizePetNames = plugin.getConfig().getBoolean("RandomizePetNames", true);
        mcMMOSuport = plugin.getConfig().getBoolean("mcMMOSuport", true);     
        rememberPetLocation = plugin.getConfig().getBoolean("RememberPetLocation", false);
        noDropOnKillCommand = plugin.getConfig().getBoolean("NoDropOnKillCommand", false);
        defaultPetAge = plugin.getConfig().getString("DefaultPetAge", "adult");
        lockSpawnedBabies = plugin.getConfig().getBoolean("LockSpawnedBabies",false);
        customNamePlates = plugin.getConfig().getBoolean("CustomNamePlates",true);
        namePlateColor = plugin.getConfig().getString("NamePlateColor","GREEN");
        
        commandPrefix = plugin.getConfig().getString("CommandPrefix", "pet");
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
