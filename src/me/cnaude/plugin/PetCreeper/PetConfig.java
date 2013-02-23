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

    public PetConfig(PetMain instance) {
        this.plugin = instance;
        this.config = plugin.getConfig();
        load();
    }

    public ItemStack getMat(String matName) {
        ItemStack item = new ItemStack(0);
        Material mat = Material.AIR;
        if (matName == null) {            
            return item;
        }         
        byte byteCode = (byte)0;        
        if (matName.contains(":")) {
            String tmp[] = matName.split(":",2);
            matName = tmp[0]; 
            if (tmp[1].matches("\\d+")) {
                byteCode = (byte)Integer.parseInt(tmp[1]);
            }
        } 
        if (matName.matches("\\d+")) {
            mat = Material.getMaterial(Integer.parseInt(matName));
        } else if (Material.matchMaterial(matName) != null) {
            mat = Material.matchMaterial(matName);
        } else {
            plugin.logInfo("Invalid bait: " + matName);
        }  
        item = new ItemStack(mat,1,byteCode);
        return item;
    }
    
    public void load() {
        baitMap.put("Bat", getMat(config.getString("Bat", "375")));
        baitMap.put("Blaze", getMat(config.getString("Blaze", "369")));
        baitMap.put("CaveSpider", getMat(config.getString("CaveSpider", "287")));
        baitMap.put("Chicken", getMat(config.getString("Chicken", "295")));
        baitMap.put("Cow", getMat(config.getString("Cow", "295")));
        baitMap.put("Creeper", getMat(config.getString("Creeper", "318")));
        baitMap.put("EnderDragon", getMat(config.getString("EnderDragon", "122")));
        baitMap.put("Enderman", getMat(config.getString("Enderman", "381")));
        baitMap.put("Ghast", getMat(config.getString("Ghast", "370")));
        baitMap.put("Giant", getMat(config.getString("Giant", "41")));
        baitMap.put("Golem", getMat(config.getString("Golem", "265")));
        //baitMap.put("HumanEntity", getMat(config.getString("HumanEntity", "264")));
        baitMap.put("LavaSlime", getMat(config.getString("MagmaCube", "378")));
        baitMap.put("MushroomCow", getMat(config.getString("MushroomCow", "40")));
        baitMap.put("Ozelot", getMat(config.getString("Ozelot", "357")));
        baitMap.put("Pig", getMat(config.getString("Pig", "319")));
        baitMap.put("PigZombie", getMat(config.getString("PigZombie", "319")));
        baitMap.put("Sheep", getMat(config.getString("Sheep", "338")));
        baitMap.put("Silverfish", getMat(config.getString("Silverfish", "280")));
        baitMap.put("Skeleton", getMat(config.getString("Skeleton", "352")));
        baitMap.put("Slime", getMat(config.getString("Slime", "341")));
        baitMap.put("SnowMan", getMat(config.getString("SnowMan", "332")));
        baitMap.put("Spider", getMat(config.getString("Spider", "287")));
        baitMap.put("Squid", getMat(config.getString("Squid", "349")));
        baitMap.put("Villager", getMat(config.getString("Villager", "38")));
        baitMap.put("VillagerGolem", getMat(config.getString("VillagerGolem", "295")));
        baitMap.put("Witch", getMat(config.getString("Witch", "115")));
        baitMap.put("WitherBoss", getMat(config.getString("WitherBoss", "399")));
        baitMap.put("Wolf", getMat(config.getString("Wolf", "319")));
        baitMap.put("Zombie", getMat(config.getString("Zombie", "295")));
        
        tamingXPMap.put("Bat", (config.getInt("mcMMOTamingXP.Bat", 10)));
        tamingXPMap.put("Blaze", (config.getInt("mcMMOTamingXP.Blaze", 10)));
        tamingXPMap.put("CaveSpider", (config.getInt("mcMMOTamingXP.CaveSpider", 10)));
        tamingXPMap.put("Chicken", (config.getInt("mcMMOTamingXP.Chicken", 10)));
        tamingXPMap.put("Cow", (config.getInt("mcMMOTamingXP.Cow", 10)));
        tamingXPMap.put("Creeper", (config.getInt("mcMMOTamingXP.Creeper", 10)));
        tamingXPMap.put("EnderDragon", (config.getInt("mcMMOTamingXP.EnderDragon", 10)));
        tamingXPMap.put("Enderman", (config.getInt("mcMMOTamingXP.Enderman", 10)));
        tamingXPMap.put("Ghast", (config.getInt("mcMMOTamingXP.Ghast", 10)));
        tamingXPMap.put("Giant", (config.getInt("mcMMOTamingXP.Giant", 10)));
        tamingXPMap.put("Golem", (config.getInt("mcMMOTamingXP.Golem", 10)));
        //tamingXPMap.put("HumanEntity", (config.getInt("HumanEntity", 10)));
        tamingXPMap.put("LavaSlime", (config.getInt("MagmaCube", 10)));
        tamingXPMap.put("MushroomCow", (config.getInt("mcMMOTamingXP.MushroomCow", 10)));
        tamingXPMap.put("Ozelot", (config.getInt("mcMMOTamingXP.Ozelot", 10)));
        tamingXPMap.put("Pig", (config.getInt("mcMMOTamingXP.Pig", 10)));
        tamingXPMap.put("PigZombie", (config.getInt("mcMMOTamingXP.PigZombie", 10)));
        tamingXPMap.put("Sheep", (config.getInt("mcMMOTamingXP.Sheep", 10)));
        tamingXPMap.put("Silverfish", (config.getInt("mcMMOTamingXP.Silverfish", 10)));
        tamingXPMap.put("Skeleton", (config.getInt("mcMMOTamingXP.Skeleton", 10)));
        tamingXPMap.put("Slime", (config.getInt("mcMMOTamingXP.Slime", 10)));
        tamingXPMap.put("SnowMan", (config.getInt("mcMMOTamingXP.SnowMan", 10)));
        tamingXPMap.put("Spider", (config.getInt("mcMMOTamingXP.Spider", 10)));
        tamingXPMap.put("Squid", (config.getInt("mcMMOTamingXP.Squid", 10)));
        tamingXPMap.put("Villager", (config.getInt("mcMMOTamingXP.Villager", 10)));
        tamingXPMap.put("VillagerGolem", (config.getInt("mcMMOTamingXP.VillagerGolem", 10)));
        tamingXPMap.put("Witch", (config.getInt("mcMMOTamingXP.Witch", 115)));
        tamingXPMap.put("WitherBoss", (config.getInt("mcMMOTamingXP.WitherBoss", 10)));
        tamingXPMap.put("Wolf", (config.getInt("mcMMOTamingXP.Wolf", 10)));
        tamingXPMap.put("Zombie", (config.getInt("mcMMOTamingXP.Zombie", 10)));

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
