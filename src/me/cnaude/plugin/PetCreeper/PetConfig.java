package me.cnaude.plugin.PetCreeper;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Creature;

public final class PetConfig {

    private final Configuration config;
    private static HashMap<String, Material> baitMap = new HashMap<String, Material>();
    public static boolean provokable;
    public static boolean ridable;
    public static boolean attackTame;
    public static int idleDistance;
    public static boolean opsBypassPerms;

    public PetConfig(PetMain plug) {
        config = plug.getConfig();
        load();
    }

    public void load() {
        baitMap.put("Chicken", Material.getMaterial(config.getInt("Chicken", 295)));
        baitMap.put("Cow", Material.getMaterial(config.getInt("Cow", 295)));
        baitMap.put("Creeper", Material.getMaterial(config.getInt("Creeper", 318)));
        baitMap.put("Giant", Material.getMaterial(config.getInt("Giant", 41)));
        baitMap.put("Pig", Material.getMaterial(config.getInt("Pig", 319)));
        baitMap.put("PigZombie", Material.getMaterial(config.getInt("PigZombie", 319)));
        baitMap.put("Sheep", Material.getMaterial(config.getInt("Sheep", 338)));
        baitMap.put("Skeleton", Material.getMaterial(config.getInt("Skeleton", 352)));
        baitMap.put("Spider", Material.getMaterial(config.getInt("Spider", 287)));
        baitMap.put("CaveSpider", Material.getMaterial(config.getInt("CaveSpider", 287)));
        baitMap.put("Squid", Material.getMaterial(config.getInt("Squid", 349)));
        baitMap.put("Zombie", Material.getMaterial(config.getInt("Zombie", 295)));
        baitMap.put("Golem", Material.getMaterial(config.getInt("Golem", 295)));
        baitMap.put("VillagerGolem", Material.getMaterial(config.getInt("VillagerGolem", 295)));
        baitMap.put("Enderman", Material.getMaterial(config.getInt("Enderman", 368)));
        baitMap.put("Villager", Material.getMaterial(config.getInt("Villager", 38)));
        baitMap.put("Silverfish", Material.getMaterial(config.getInt("Silverfish", 280)));
        baitMap.put("Blaze", Material.getMaterial(config.getInt("Blaze", 369)));
        baitMap.put("Ghast", Material.getMaterial(config.getInt("Ghast", 370)));
        baitMap.put("Slime", Material.getMaterial(config.getInt("Slime", 341)));
        baitMap.put("MushroomCow", Material.getMaterial(config.getInt("MushroomCow", 40)));


        provokable = config.getBoolean("Provokable", true);
        ridable = config.getBoolean("Ridable", true);
        attackTame = config.getBoolean("AttackTame", false);
        idleDistance = config.getInt("IdleDistance", 5);
        opsBypassPerms = config.getBoolean("OpsBypassPerms", false);
    }

    public static Material getBait(Creature pet) {
        if (baitMap.containsKey(pet.getType().getName())) {
            return baitMap.get(pet.getType().getName());
        } else {
            return Material.AIR;
        }
    }
}
