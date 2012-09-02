package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.Navigation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PetMain extends JavaPlugin {

    public static final String PLUGIN_NAME = "PetCreeper";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    private final PetPlayerListener playerListener = new PetPlayerListener(this);
    private final PetEntityListener entityListener = new PetEntityListener(this);
    private static PetMain instance = null;
    public ConcurrentHashMap<String, ArrayList<Pet>> playersWithPets = new ConcurrentHashMap<String, ArrayList<Pet>>();
    public ConcurrentHashMap<Entity, String> petList = new ConcurrentHashMap<Entity, String>();
    public ConcurrentHashMap<Entity, String> petNameList = new ConcurrentHashMap<Entity, String>();
    public ConcurrentHashMap<Entity, Boolean> petFollowList = new ConcurrentHashMap<Entity, Boolean>();
    public ConcurrentHashMap<Integer, Entity> entityIds = new ConcurrentHashMap<Integer, Entity>();
    static final Logger log = Logger.getLogger("Minecraft");
    PetMainLoop mainLoop;
    public boolean configLoaded = false;
    private static PetConfig config;
    private PetFile petFile = new PetFile(this);

    @Override
    public void onEnable() {
        loadConfig();

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        if (petFile.loadPets()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                spawnPetsOf(p);
            }
        }

        mainLoop = new PetMainLoop(this);

        registerCommand(PetConfig.commandPrefix);
        registerCommand(PetConfig.commandPrefix + "free");
        registerCommand(PetConfig.commandPrefix + "list");
        registerCommand(PetConfig.commandPrefix + "info");
        registerCommand(PetConfig.commandPrefix + "name");
        registerCommand(PetConfig.commandPrefix + "give");
        registerCommand(PetConfig.commandPrefix + "reload");
        registerCommand(PetConfig.commandPrefix + "mode");
    }

    private void registerCommand(String command) {
        try {
            getCommand(command).setExecutor(new PetCommands(this));
        } catch (Exception ex) {
            System.out.println("Failed to register command '" + command + "'! Is it allready used by some other Plugin? " + ex.getMessage());
        }
    }

    public void logInfo(String s) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, s));
    }

    public void message(Player p, String msg) {
        msg = msg.replaceAll("%PLAYER%", p.getName());
        msg = msg.replaceAll("%DPLAYER%", p.getDisplayName());
        p.sendMessage(msg);
    }

    public static PetConfig getPConfig() {
        return config;
    }

    void loadConfig() {
        if (!this.configLoaded) {
            getConfig().options().copyDefaults(true);
            saveConfig();
            logInfo("Configuration loaded.");
            config = new PetConfig(this);
        } else {
            reloadConfig();
            getConfig().options().copyDefaults(false);
            config = new PetConfig(this);
            logInfo("Configuration reloaded.");
        }
        instance = this;
        configLoaded = true;
    }

    public boolean hasPerm(Player p, String s) {
        if ((p.hasPermission(s)) || (p.hasPermission("petcreeper.*")) 
                || (p.isOp() && PetConfig.opsBypassPerms)
                || PetConfig.disablePermissions) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDisable() {
        this.mainLoop.end();
        
        for (String p : petList.values()) {
            despawnPetsOf(getServer().getPlayer(p));
        }
        petFile.savePets();
        petNameList.clear();
        entityIds.clear();
        petList.clear();
        petFollowList.clear();
        playersWithPets.clear();
    }

    public ArrayList<Pet> getPetsOf(Player p) {
        return playersWithPets.get(p.getName());
    }

    public String getNameOfPet(Entity e) {
        String s = e.getType().getName();
        if (petNameList.containsKey(e)) {
            s = petNameList.get(e);
        }
        return s;
    }

    public Boolean isPetFollowing(Entity e) {
        Boolean f = false;
        if (petFollowList.containsKey(e)) {
            f = petFollowList.get(e);
        }
        return f;
    }

    public void spawnPetsOf(Player p) {
        if (isPetOwner(p)) {
            for (Iterator<Pet> iterator = getPetsOf(p).iterator(); iterator.hasNext(); ) {
                Pet pet = iterator.next();
                if (!spawnPet(pet, p, true)) {
                    if (playersWithPets.get(p.getName()).contains(pet)) {
                        iterator.remove();
                    }
                }
            }            
        }
    }

    public void printPetInfo(Player p, Entity e) {
        message(p, ChatColor.GREEN + "=====" + ChatColor.YELLOW + "[Pet Details]" + ChatColor.GREEN + "=====");
        message(p, ChatColor.GREEN + "  Type: " + ChatColor.WHITE + e.getType().getName());
        message(p, ChatColor.GREEN + "  Health: " + ChatColor.WHITE + ((LivingEntity) e).getHealth());
        message(p, ChatColor.GREEN + "  Name: " + ChatColor.WHITE + getNameOfPet(e));
        message(p, ChatColor.GREEN + "  Following: " + ChatColor.WHITE + isPetFollowing(e));
        ChatColor mColor = ChatColor.BLUE;
        if (getModeOfPet(e, p) == Pet.modes.AGGRESSIVE) {
            mColor = ChatColor.RED;
        }
        if (getModeOfPet(e, p) == Pet.modes.DEFENSIVE) {
            mColor = ChatColor.GOLD;
        }
        message(p, ChatColor.GREEN + "  Mode: " + mColor + getModeOfPet(e, p));
        if (e.getType() == EntityType.SHEEP) {
            message(p, ChatColor.GREEN + "  Sheared: " + ChatColor.WHITE + ((Sheep) e).isSheared());
            message(p, ChatColor.GREEN + "  Color: " + ChatColor.WHITE + ((Sheep) e).getColor().name());
        }
        if (e.getType() == EntityType.PIG) {
            message(p, ChatColor.GREEN + "  Saddled: " + ChatColor.WHITE + ((Pig) e).hasSaddle());
        }
        if (e.getType() == EntityType.SLIME) {
            message(p, ChatColor.GREEN + "  Size: " + ChatColor.WHITE + ((Slime) e).getSize());
        }
        if (e.getType() == EntityType.MAGMA_CUBE) {
            message(p, ChatColor.GREEN + "  Size: " + ChatColor.WHITE + ((MagmaCube) e).getSize());
        }
        if (e.getType() == EntityType.ENDERMAN) {
            message(p, ChatColor.GREEN + "  Held Item: " + ChatColor.WHITE + ((Enderman) e).getCarriedMaterial().getItemType().name());
        }
        if (e.getType() == EntityType.VILLAGER) {
            message(p, ChatColor.GREEN + "  Profession: " + ChatColor.WHITE + ((Villager) e).getProfession());
        }
        if (e instanceof Ageable) {
            message(p, ChatColor.GREEN + "  Age: " + ChatColor.WHITE + ((Ageable) e).getAge());
        }
    }

    public boolean spawnPet(Pet pet, Player p, boolean msg) {
        boolean spawned = false;
        Location pos = p.getLocation().clone();
        pos.setY(pos.getY() + 1.0D);
        if (pos instanceof Location) {
            try {
                Entity e = p.getWorld().spawnEntity(pos, pet.type);                
                if (e instanceof Entity) {
                    pet.entityId = e.getEntityId();
                    if (pet.type == EntityType.SHEEP) {
                        ((Sheep) e).setSheared(pet.sheared);
                        ((Sheep) e).setColor(DyeColor.valueOf(pet.color));
                    }
                    if (pet.type == EntityType.PIG) {
                        ((Pig) e).setSaddle(pet.saddled);
                    }
                    if (pet.type == EntityType.SLIME) {
                        ((Slime) e).setSize(pet.size);
                    }
                    if (pet.type == EntityType.MAGMA_CUBE) {
                        ((MagmaCube) e).setSize(pet.size);
                    }
                    if (pet.type == EntityType.ENDERMAN) {
                        ((Enderman) e).setCarriedMaterial(pet.carriedMat);
                    }
                    if (pet.type == EntityType.VILLAGER) {
                        ((Villager) e).setProfession(pet.prof);
                    }
                    if (e instanceof Ageable) {
                        ((Ageable) e).setAge(pet.age);
                    }
                    ((LivingEntity) e).setHealth(pet.hp);
                    petList.put(e, p.getName());
                    petNameList.put(e, pet.petName);
                    petFollowList.put(e, pet.followed);
                    entityIds.put(e.getEntityId(), e);

                    if (msg) {
                        p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " greets you!");
                    }
                    spawned = true;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        }
        return spawned;
    }

    public Pet.modes getModeOfPet(Entity e, Player p) {
        for (Pet pet : getPetsOf(p)) {
            if (pet.entityId == e.getEntityId()) {
                return pet.mode;
            }
        }
        return Pet.modes.AGGRESSIVE;
    }

    public void despawnPetsOf(Player p) {
        if (isPetOwner(p)) {
            for (Iterator<Pet> iterator = getPetsOf(p).iterator(); iterator.hasNext(); ) {                                                                            
                despawnPet(iterator.next());
            }
        }
    }

    public void despawnPet(Pet pet) {
        if (entityIds.containsKey(pet.entityId)) {
            Entity e = entityIds.get(pet.entityId);
            pet.initPet(e);
            pet.petName = getNameOfPet(e);            
            pet.entityId = -1;
            cleanUpLists(e);
            e.remove();
        }
    }

    public void printPetListOf(Player p) {
        if (isPetOwner(p)) {
            p.sendMessage(ChatColor.GREEN + "You are the proud owner of the following pets:");
            for (int i = 0; i < getPetsOf(p).size(); i++) {
                Pet pet = getPetsOf(p).get(i);
                p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.AQUA + (i + 1) + ChatColor.YELLOW + "] "
                        + ChatColor.YELLOW + pet.type.getName()
                        + ChatColor.GREEN + " named " + ChatColor.YELLOW
                        + pet.petName + ChatColor.GREEN + ".");
            }
        }
    }

    public void teleportPetsOf(Player p, boolean msg) {
        if (isPetOwner(p)) {
            for (Iterator i = getPetsOf(p).iterator(); i.hasNext();) {
                teleportPet((Pet) i.next(), msg);
            }
        }
    }

    public Entity getEntityOfPet(Pet pet) {
        Entity e = null;
        if (entityIds.containsKey(pet.entityId)) {
            e = entityIds.get(pet.entityId);
        }
        return e;
    }

    public void teleportPet(Pet pet, boolean msg) {
        if (entityIds.containsKey(pet.entityId)) {
            Entity e = entityIds.get(pet.entityId);
            Player p =  getServer().getPlayer(petList.get(e));
            //if (e.getWorld() == p.getWorld()) {
            //    Location pos = p.getLocation().clone();
            //    pos.setY(pos.getY() + 1.0D);
            //    if (!e.teleport(pos)) {
                    this.despawnPet(pet);
                    this.spawnPet(pet, p, false);
            //    }
            //} else {
            //    this.despawnPet(pet);
            //    this.spawnPet(pet, p, false);
            //}
            if (msg) {
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getNameOfPet(e) + ChatColor.GREEN + " teleported to you.");
            }
        }
    }

    public void walkToPlayer(Entity e, Player p) {
        if (e.getPassenger() instanceof Player) {
            return;
        }
        if (e.getType() == EntityType.CREEPER) {
            Navigation n = ((CraftLivingEntity) e).getHandle().getNavigation();                                   
            n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);     
            return;
        }        
        if (e.getType() == EntityType.SQUID) {
            Navigation n = ((CraftLivingEntity) e).getHandle().getNavigation();                                   
            n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);     
            return;
        }
        if (e instanceof Monster) {
            ((Monster) e).setTarget(p);
        } else {
            Navigation n = ((CraftLivingEntity) e).getHandle().getNavigation();                                   
            n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);                     
        }
    }
    
    public void attackNearbyEntities(Entity e, Player p, Pet.modes mode) {     
        // Passive pets don't attack
        if (mode == Pet.modes.PASSIVE) {
            ((Monster) e).setTarget(null);
            return;
        }
        if (p.getNearbyEntities(PetConfig.attackDistance, PetConfig.attackDistance, PetConfig.attackDistance).isEmpty()) {
            ((Monster) e).setTarget(null);
            return;
        }
        if (e instanceof Monster) {   
            for (Iterator<Entity> iterator = p.getNearbyEntities(PetConfig.attackDistance, PetConfig.attackDistance, PetConfig.attackDistance).iterator(); iterator.hasNext(); ) {
                Entity target = iterator.next();    
                if (target == p) {
                    continue;
                }
                if (petList.containsKey(target)) {
                    // Don't attack owner
                    if (getServer().getPlayer(petList.get(target)) == p) {
                        continue;
                    }
                    // Globally we check if pets don't attack pets
                    if (!PetConfig.petsAttackPets) {
                        continue;
                    }
                }
                if (target instanceof Player) {
                    // Defensive pets don't attack players unless provoked
                    if (mode == Pet.modes.DEFENSIVE) {
                        continue;
                    }
                    // Globally we check if pets can attack players
                    if (!PetConfig.PetsAttackPlayers) {
                        continue;
                    }
                }
                if (target instanceof Monster
                        || (target instanceof LivingEntity && mode == Pet.modes.AGGRESSIVE)) {
                    /*
                     if (e instanceof Skeleton) {
                     ((Creature)e).
                     Location spawn = e.getLocation();
                     Location tl = target.getLocation().clone();
                     tl.setY(tl.getY() + 1);
                     spawn.setY(spawn.getY()+3);
                     Vector v = tl.toVector().subtract(spawn.toVector()).normalize();

                     int arrows = Math.round(1);
                     for (int i = 0; i < arrows; i++) {
                     Arrow ar = e.getWorld().spawnArrow(spawn, v, 2.0f, 12f);
                     //ar.setVelocity(ar.getVelocity());
                     ar.setShooter((LivingEntity)p);
                     }
                     break;
                     }
                     */
                    //p.sendMessage("targeting" + target.getType().getName());
                    ((Monster) e).setTarget((LivingEntity) target);
                    break;
                }
            }
        } else {
            //p.sendMessage("targeting null1");
            if (e instanceof Monster) {
                ((Monster) e).setTarget(null);
            }
        }
    }

    public boolean tamePetOf(Player p, Entity e, boolean spawned) {
        boolean tamed = false;
        if (e instanceof LivingEntity) {
            EntityType et = e.getType();
            ItemStack bait = p.getItemInHand();
            int amt = bait.getAmount();

            if (isPetOwner(p)) {
                if (getPetsOf(p).size() >= PetConfig.maxPetsPerPlayer) {
                    p.sendMessage(ChatColor.RED + "You already have the maximum number of pets!");
                    return false;
                }
            }

            if (((bait.getType() == PetConfig.getBait(et)) && (amt > 0)) || spawned) {
                if (!spawned) {
                    if (!hasPerm(p, "petcreeper.tame." + et.getName()) && !hasPerm(p, "petcreeper.tame.All")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + et.getName() + ".");
                        return false;
                    }
                }
                if (!isPetOwner(p)) {
                    this.playersWithPets.put(p.getName(), new ArrayList<Pet>());
                }
                
                Entity pe;
                if (e.getType() == EntityType.CREEPER) {
                    World world = e.getWorld();
                    Location loc = e.getLocation();
                    Boolean powered = ((Creeper)e).isPowered();
                    e.remove();                    
                    pe = world.spawnEntity(loc, EntityType.CREEPER);                    
                    ((Creeper)pe).setPowered(powered);   
                } else {
                    pe = e;
                }
                Pet pet = new Pet(pe);
                this.playersWithPets.get(p.getName()).add(pet);
                tamed = true;
                pe.setFireTicks(0);

                if (spawned) {
                    //p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.type.getName() + ChatColor.GREEN + " greets you!");
                } else {
                    if (amt == 1) {
                        p.getInventory().removeItem(new ItemStack[]{bait});
                    } else {
                        bait.setAmount(amt - 1);
                    }
                    walkToPlayer(pe, p);
                    p.sendMessage(ChatColor.GREEN + "You tamed the " + ChatColor.YELLOW + pet.type.getName() + ChatColor.GREEN + "!");
                }
                entityIds.put(pet.entityId, pe);
                petNameList.put(pe, pet.petName);
                petFollowList.put(pe, pet.followed);
                petList.put(pe, p.getName());
                if (PetConfig.defaultPetMode.equals("D")) {
                    pet.mode = Pet.modes.DEFENSIVE;
                }
                if (PetConfig.defaultPetMode.equals("P")) {
                    pet.mode = Pet.modes.PASSIVE;
                }
                if (PetConfig.defaultPetMode.equals("A")) {
                    pet.mode = Pet.modes.AGGRESSIVE;
                }
            }
        }
        return tamed;
    }

    public void untameAllPetsOf(Player p) {
        if (isPetOwner(p)) {
            for (Iterator<Pet> iterator = getPetsOf(p).iterator(); iterator.hasNext();) {
                Pet pet = iterator.next();
                Entity e = getEntityOfPet(pet);
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + petNameList.get(e) + ChatColor.GREEN + " is now free!");
                cleanUpLists(e);
            }
        }
        playersWithPets.remove(p.getName());
    }
       

    public Pet getPet(Entity e) {
        Pet returnPet = new Pet();
        for (Pet pet : getPetsOf(getServer().getPlayer(petList.get(e)))) {
            if (pet.entityId == e.getEntityId()) {
                returnPet = pet;
            }
        }
        return returnPet;
    }

    public void cleanUpLists(Entity e) {
        int id = e.getEntityId();
        if (petList.containsKey(e)) {
            petList.remove(e);
        }
        if (petNameList.containsKey(e)) {
            petNameList.remove(e);
        }
        if (petFollowList.containsKey(e)) {
            petFollowList.remove(e);
        }
        if (entityIds.containsKey(id)) {
            entityIds.remove(id);
        }
    }

    public void untamePetOf(Player p, Entity e, boolean msg) {
        if (isPetOwner(p)) {
            for (Iterator<Pet> iterator = getPetsOf(p).iterator(); iterator.hasNext(); ) {
                Pet pet = iterator.next();     
                if (pet.entityId == e.getEntityId()) {
                    cleanUpLists(e);
                    iterator.remove();
                    if (msg) {
                        p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " is now free!");
                    }
                }
            }
            if (playersWithPets.get(p.getName()).isEmpty()) {
                playersWithPets.remove(p.getName());
            }
        }
    }

    public Player getMasterOf(Entity pet) {
        return getServer().getPlayer(petList.get(pet));
    }

    public boolean isPet(Entity pet) {
        return petList.containsKey(pet);
    }

    public boolean isPetOwner(Player p) {
        return playersWithPets.containsKey(p.getName());
    }

    public void setFollowed(Player p, Pet pet, boolean f) {
        pet.followed = f;
    }

    public static PetMain get() {
        return instance;
    }
}
