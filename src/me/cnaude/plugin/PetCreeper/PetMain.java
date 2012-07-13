package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PetMain extends JavaPlugin {

    public static final String PLUGIN_NAME = "PetCreeper";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    private final PetPlayerListener playerListener = new PetPlayerListener(this);
    private final PetEntityListener entityListener = new PetEntityListener(this);
    private static PetMain instance = null;
    public HashMap<String, ArrayList<Pet>> playersWithPets = new HashMap<String, ArrayList<Pet>>();
    public HashMap<Entity, Player> petList = new HashMap<Entity, Player>();
    public HashMap<Entity, String> petNameList = new HashMap<Entity, String>();
    public HashMap<Entity, Boolean> petFollowList = new HashMap<Entity, Boolean>();
    public HashMap<Integer, Entity> entityIds = new HashMap<Integer, Entity>();
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
        
        registerCommand("pet");
        registerCommand("petfree");
        registerCommand("petlist");
        registerCommand("petinfo");
        registerCommand("petname");
        registerCommand("petgive");
        registerCommand("petreload");        
    }
    
    private void registerCommand(String command) {
        try {
            getCommand(command).setExecutor(new PetCommands(this));
        } catch (Exception ex) {
            System.out.println("Failed to register command '" + command + "'! Is it allready used by some other Plugin? " + ex.getMessage());
        }
    }

    public void logInfo(String s) {
        log.log(Level.INFO,String.format("%s %s",LOG_HEADER,s));
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
        if ((p.hasPermission(s)) || (p.isOp() && PetConfig.opsBypassPerms)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDisable() {
        this.mainLoop.end();
        for (Player p : petList.values()) {                              
            despawnPetsOf(p);                
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
            for (Iterator i = getPetsOf(p).iterator(); i.hasNext();) {
                Pet pet = (Pet) i.next();
                spawnPet(pet, p, true);
            }
        }
    }
    
    public void printPetInfo(Player p, Entity e) {
        message(p, ChatColor.GREEN + "=====" + ChatColor.YELLOW + "[Pet Details]" + ChatColor.GREEN + "=====");
        message(p, ChatColor.GREEN + "  Type: " + ChatColor.WHITE + e.getType().getName());
        message(p, ChatColor.GREEN + "  Health: " + ChatColor.WHITE + ((LivingEntity) e).getHealth());
        message(p, ChatColor.GREEN + "  Name: " + ChatColor.WHITE + getNameOfPet(e));
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
        if (petFollowList.containsKey(e)) {
            message(p, ChatColor.GREEN + "  Following: " + ChatColor.WHITE + petFollowList.get(e));
        }
    }

    public void spawnPet(Pet pet, Player p, boolean msg) {
        Location pos = p.getLocation().clone();
        pos.setY(pos.getY() + 1.0D);
        Entity e = p.getWorld().spawnCreature(pos, pet.type);
        if (e != null) {
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
                ((Ageable)e).setAge(pet.age);
            }
            ((LivingEntity) e).setHealth(pet.hp);
            petList.put(e, p);
            petNameList.put(e, pet.petName);
            petFollowList.put(e, pet.followed);
            entityIds.put(e.getEntityId(), e);
            if (msg) {
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " greets you!");
            }
        }
    }

    public void despawnPetsOf(Player p) {
        if (isPetOwner(p)) {
            for (Pet pet : getPetsOf(p)) {
                despawnPet(pet);
            }
        }
    }

    public void despawnPet(Pet pet) {
        if (entityIds.containsKey(pet.entityId)) {
            Entity e = entityIds.get(pet.entityId);
            pet.initPet(e);
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
                p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.AQUA + (i+1) + ChatColor.YELLOW + "] "
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
            Player p = petList.get(e);
            if (e.getWorld() == p.getWorld()) {
                Location pos = p.getLocation().clone();
                pos.setY(pos.getY() + 1.0D);
                e.teleport(pos);
            } else {
                this.despawnPet(pet);
                this.spawnPet(pet, p, false);
            }
            if (msg) {
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getNameOfPet(e) + ChatColor.GREEN + " teleported to you.");
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
                Pet pet = new Pet(e);
                this.playersWithPets.get(p.getName()).add(pet);
                tamed = true;

                if (spawned) {
                    //p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.type.getName() + ChatColor.GREEN + " greets you!");
                } else {
                    if (amt == 1) {
                        p.getInventory().removeItem(new ItemStack[]{bait});
                    } else {
                        bait.setAmount(amt - 1);
                    }
                    if (e instanceof Creature) {
                        ((Creature) e).setTarget(null);
                    }
                    p.sendMessage(ChatColor.GREEN + "You tamed the " + ChatColor.YELLOW + pet.type.getName() + ChatColor.GREEN + "!");
                }
                entityIds.put(pet.entityId, e);
                petNameList.put(e, pet.petName);
                petFollowList.put(e, pet.followed);
                petList.put(e, p);
            }
        }
        return tamed;
    }

    public void untameAllPetsOf(Player p) {
        if (isPetOwner(p)) {
            ArrayList<Entity> tmpEList = new ArrayList<Entity>();
            for (Map.Entry<Entity, Player> entry : petList.entrySet()) {
                Entity e = entry.getKey();
                Player player = entry.getValue();
                if (p == player) {
                    p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + petNameList.get(e) + ChatColor.GREEN + " is now free!");
                    tmpEList.add(e);
                }
            }
            for (Entity e : tmpEList) {
                cleanUpLists(e);
            }
            tmpEList.clear();
        }
        playersWithPets.remove(p.getName());
    }
    
    public Pet getPet(Entity e, Player p) {
       Pet returnPet = new Pet();
       for (Pet pet : getPetsOf(p)) {
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
            for (int i = getPetsOf(p).size() - 1; i >= 0; i--) {
                Pet pet = getPetsOf(p).get(i);
                if (pet.entityId == e.getEntityId()) {
                    cleanUpLists(e);
                    getPetsOf(p).remove(i);
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
        return petList.get(pet);
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
