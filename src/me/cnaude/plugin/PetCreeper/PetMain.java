package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

        getCommand("pet").setExecutor(this);

        if (petFile.loadPets()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                spawnPetsOf(p);
            }
        }
        
        mainLoop = new PetMainLoop(this);
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player p = (Player) sender;

        if (p.isInsideVehicle()) {
            if (p.getVehicle().getType().isAlive()) {
                p.sendMessage(ChatColor.RED + "You can't use /pet when riding this " + p.getVehicle().getType().getName() + ".");
                return true;
            }
        }

        if (commandLabel.equalsIgnoreCase("pet")) {
            if (!hasPerm(p, "petcreeper.pet")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (isPetOwner(p)) {
                teleportPetsOf(p);                
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petlist")) {
            if (isPetOwner(p)) {
                printPetListOf(p);
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petfree")) {
            if (isPetOwner(p)) {
                untameAllPetsOf(p);                
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petname")) {
            if (isPetOwner(p)) {                
                String s = "";
                for(String i : args) {
                    s +=  i + " ";
                }         
                s = s.substring(0, s.length() - 1);
                if (!s.isEmpty()) {                    
                    for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                        Pet pet = (Pet)i.next();
                        pet.petName = s;
                        p.sendMessage(ChatColor.GREEN + "You named your pet "+ ChatColor.YELLOW + pet.petName + ChatColor.GREEN + "!");
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW+ "Usage: " + ChatColor.WHITE + "/petname [name]");
                }                
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        return false;
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
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                Pet pet = (Pet)i.next();
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
            ((Sheep)e).setSheared(pet.sheared);
            ((Sheep)e).setColor(DyeColor.getByData(pet.color));
        }              
        if (pet.type == EntityType.PIG) {            
            ((Pig)e).setSaddle(pet.saddled);
        } 
        if (pet.type == EntityType.SLIME) {            
            ((Slime)e).setSize(pet.size);
        } 
        if (pet.type == EntityType.MAGMA_CUBE) {            
            ((MagmaCube)e).setSize(pet.size);
        }
        ((LivingEntity)e).setHealth(pet.hp);
        petList.put(e, p);
        petNameList.put(e, pet.petName);
        petFollowList.put(e, pet.followed);
        entityIds.put(e.getEntityId(), e);
        p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " greets you!");
    }

    public void despawnPetsOf(Player p) {
        if (isPetOwner(p)) {
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                despawnPet((Pet)i.next());                           
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
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                Pet pet = (Pet)i.next();
                p.sendMessage(ChatColor.YELLOW + pet.type.toString() 
                        + ChatColor.GREEN + " named " + ChatColor.YELLOW 
                        + pet.petName + ChatColor.GREEN + ".");
            }
        }
    }
    
    public void teleportPetsOf(Player p) {
        if (isPetOwner(p)) {
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                teleportPet((Pet)i.next());                          
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
    
    public void teleportPet(Pet pet) {
        if (entityIds.containsKey(pet.entityId)) {
            Entity e = entityIds.get(pet.entityId);
            Player p = petList.get(e);
            if (e.getWorld().equals(p.getWorld())) {
                Location pos = p.getLocation().clone();
                pos.setY(pos.getY() + 1.0D);
                e.teleport(pos);
            } else {
                this.despawnPet(pet);
                this.spawnPet(pet,p);
            }              
            p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getNameOfPet(e) + ChatColor.GREEN + " teleported to you.");                    
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
                        ((Monster)e).setTarget(null);                            
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
            for (Map.Entry<Entity, Player> entry : petList.entrySet()) {
                Entity e = entry.getKey();
                Player player = entry.getValue();                
                if (p == player) {
                    String s = petNameList.get(e);
                    cleanUpLists(e);                    
                    p.sendMessage(ChatColor.GREEN + "Your pet "+ ChatColor.YELLOW + s + ChatColor.GREEN + " is now free!");
                }
                
            }
        }        
        playersWithPets.remove(p.getName());
    }
    
    public void cleanUpLists(Entity e) {
        if (petList.containsKey(e)) {
            petList.remove(e);
        }
        if (petNameList.containsKey(e)) {
            petNameList.remove(e);
        }
        if (petFollowList.containsKey(e)) {
            petFollowList.remove(e);
        }
    }
    
    public void untamePetOf(Player p, Entity e) {
        if (isPetOwner(p)) {
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                Pet pet = (Pet)i.next();
                if (pet.entityId == e.getEntityId()) {
                    cleanUpLists(e);
                    p.sendMessage(ChatColor.GREEN + "Your pet "+ ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " is now free!");
                    playersWithPets.get(p.getName()).remove(pet);                    
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