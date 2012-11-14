package me.cnaude.plugin.PetCreeper;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;

public final class PetConfig {

    private final Configuration config;
    private static HashMap<String, Material> baitMap = new HashMap<String, Material>();
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

    public PetConfig(PetMain plug) {
        config = plug.getConfig();
        load();
    }

    public void load() {
        baitMap.put("Bat", Material.getMaterial(config.getInt("Bat", 375)));
        baitMap.put("Blaze", Material.getMaterial(config.getInt("Blaze", 369)));
        baitMap.put("CaveSpider", Material.getMaterial(config.getInt("CaveSpider", 287)));
        baitMap.put("Chicken", Material.getMaterial(config.getInt("Chicken", 295)));
        baitMap.put("Cow", Material.getMaterial(config.getInt("Cow", 295)));
        baitMap.put("Creeper", Material.getMaterial(config.getInt("Creeper", 318)));
        baitMap.put("EnderDragon", Material.getMaterial(config.getInt("EnderDragon", 122)));
        baitMap.put("Enderman", Material.getMaterial(config.getInt("Enderman", 381)));
        baitMap.put("Ghast", Material.getMaterial(config.getInt("Ghast", 370)));
        baitMap.put("Giant", Material.getMaterial(config.getInt("Giant", 41)));
        baitMap.put("Golem", Material.getMaterial(config.getInt("Golem", 265)));
        //baitMap.put("HumanEntity", Material.getMaterial(config.getInt("HumanEntity", 264)));
        baitMap.put("LavaSlime", Material.getMaterial(config.getInt("MagmaCube", 378)));
        baitMap.put("MushroomCow", Material.getMaterial(config.getInt("MushroomCow", 40)));
        baitMap.put("Ozelot", Material.getMaterial(config.getInt("Ozelot", 357)));
        baitMap.put("Pig", Material.getMaterial(config.getInt("Pig", 319)));
        baitMap.put("PigZombie", Material.getMaterial(config.getInt("PigZombie", 319)));
        baitMap.put("Sheep", Material.getMaterial(config.getInt("Sheep", 338)));
        baitMap.put("Silverfish", Material.getMaterial(config.getInt("Silverfish", 280)));
        baitMap.put("Skeleton", Material.getMaterial(config.getInt("Skeleton", 352)));
        baitMap.put("Slime", Material.getMaterial(config.getInt("Slime", 341)));
        baitMap.put("SnowMan", Material.getMaterial(config.getInt("SnowMan", 332)));
        baitMap.put("Spider", Material.getMaterial(config.getInt("Spider", 287)));
        baitMap.put("Squid", Material.getMaterial(config.getInt("Squid", 349)));
        baitMap.put("Villager", Material.getMaterial(config.getInt("Villager", 38)));
        baitMap.put("VillagerGolem", Material.getMaterial(config.getInt("VillagerGolem", 295)));
        baitMap.put("Witch", Material.getMaterial(config.getInt("Witch", 115)));
        baitMap.put("WitherBoss", Material.getMaterial(config.getInt("WitherBoss", 399)));
        baitMap.put("Wolf", Material.getMaterial(config.getInt("Wolf", 319)));
        baitMap.put("Zombie", Material.getMaterial(config.getInt("Zombie", 295)));

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
        
        commandPrefix = config.getString("CommandPrefix", "pet");
    }

    public static Material getBait(EntityType type) {        
        if (baitMap.containsKey(type.getName())) {
            return baitMap.get(type.getName());
        } else {
            return Material.AIR;
        }
    }
}
