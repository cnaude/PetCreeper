package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PetMain extends JavaPlugin {

    private final PetPlayerListener playerListener = new PetPlayerListener(this);
    private final PetEntityListener entityListener = new PetEntityListener(this);
    private static PetMain instance = null;
    public HashMap<String, ArrayList<Pet>> playersWithPets = new HashMap<String, ArrayList<Pet>>();
    public HashMap<Entity, Player> petList = new HashMap<Entity, Player>();
    public HashMap<Entity, String> petNameList = new HashMap<Entity, String>();
    public HashMap<Entity, Boolean> petFollowList = new HashMap<Entity, Boolean>();
    public HashMap<Integer, Entity> entityIds = new HashMap<Integer, Entity>();
    PetMainLoop mainLoop;
    private PetConfig config;
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
    }
    
    private void registerCommand(String command) {
        try {
            getCommand(command).setExecutor(new PetCommands(this));
        } catch (Exception ex) {
            System.out.println("Failed to register command '" + command + "'! Is it allready used by some other Plugin? " + ex.getMessage());
        }
    }

    void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = new PetConfig(this);
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
        if (petFile.savePets()) {
            entityIds.clear();
            petList.clear();
            petNameList.clear();
            petFollowList.clear();
            playersWithPets.clear();
        }
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
                spawnPet(pet, p);
            }
        }
    }

    public void spawnPet(Pet pet, Player p) {
        Location pos = p.getLocation().clone();
        pos.setY(pos.getY() + 1.0D);
        Entity e = p.getWorld().spawnCreature(pos, pet.type);
        pet.entityId = e.getEntityId();
        if (pet.type == EntityType.SHEEP) {
            ((Sheep) e).setSheared(pet.sheared);
            ((Sheep) e).setColor(DyeColor.getByData(pet.color));
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
        p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " greets you!");
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
                //for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                Pet pet = getPetsOf(p).get(i);
                p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.AQUA + i + ChatColor.YELLOW + "] "
                        + ChatColor.YELLOW + pet.type.toString()
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
            if (e.getWorld().equals(p.getWorld())) {
                Location pos = p.getLocation().clone();
                pos.setY(pos.getY() + 1.0D);
                e.teleport(pos);
            } else {
                this.despawnPet(pet);
                this.spawnPet(pet, p);
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

            /*if (isPetOwner(p)) {
             p.sendMessage("You already have a pet!");
             return false;
             }*/
            
            if (isPetOwner(p)) {
                if (getPetsOf(p).size() >= PetConfig.maxPetsPerPlayer) {
                    p.sendMessage(ChatColor.RED + "You already have the maximum number of pets!");
                    return false;
                }
            }

            if ((bait.getType() == PetConfig.getBait(et)) && (amt > 0)) {
                if (!hasPerm(p, "petcreeper.tame." + et.getName()) && !hasPerm(p, "petcreeper.tame.All")) {
                    p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + et.getName() + ".");
                    return false;
                }
                if (!isPetOwner(p)) {
                    this.playersWithPets.put(p.getName(), new ArrayList<Pet>());
                }
                Pet pet = new Pet(e);
                this.playersWithPets.get(p.getName()).add(pet);
                tamed = true;

                if (spawned) {
                    p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.type.getName() + ChatColor.GREEN + " greets you!");
                } else {
                    if (amt == 1) {
                        p.getInventory().removeItem(new ItemStack[]{bait});
                    } else {
                        bait.setAmount(amt - 1);
                    }
                    if (e instanceof Monster) {
                        ((Monster) e).setTarget(null);
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

    public void untamePetOf(Player p, Entity e) {
        if (isPetOwner(p)) {
            for (int i = getPetsOf(p).size(); i > 0; i--) {
                Pet pet = getPetsOf(p).get(i);
                if (pet.entityId == e.getEntityId()) {
                    cleanUpLists(e);
                    getPetsOf(p).remove(i);
                    p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " is now free!");
                }
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
        return (playersWithPets.containsKey(p.getName()));
    }

    public void setFollowed(Player p, Pet pet, boolean f) {
        pet.followed = f;
    }

    public static PetMain get() {
        return instance;
    }
}
