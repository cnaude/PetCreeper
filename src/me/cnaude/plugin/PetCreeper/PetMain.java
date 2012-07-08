package me.cnaude.plugin.PetCreeper;

import com.google.gson.Gson;
import java.io.*;
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
    private File dataFolder;
    PetMainLoop mainLoop;
    private PetConfig config;

    @Override
    public void onEnable() {
        loadConfig();

        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        getCommand("pet").setExecutor(this);

        this.dataFolder = new File("plugins/PetCreeper");
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }

        File creeperFile = new File(this.dataFolder, "pets.txt");
        File creeperFileJson = new File(this.dataFolder, "pets.json");
        if (creeperFile.exists()) {
            System.out.println("Found old pets.txt. Attempting to load pets.");
            try {
                BufferedReader in = new BufferedReader(new FileReader(creeperFile));
                String line;
                String player = "";
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }                    
                    String[] parts = line.split(":", 2);
                    parts[1] = parts[1].replaceAll("^\\s+", "");
                    parts[1] = parts[1].replaceAll("\\s+$", "");
                    if (parts[0].toUpperCase().equals("PLAYER")) {                        
                        player = parts[1];
                        System.out.println("Loading pet for " + player);
                        this.playersWithPets.put(player, new ArrayList<Pet>());
                    } else if (parts[0].toUpperCase().equals("PETTYPE")) {
                        this.playersWithPets.get(player).get(0).type = EntityType.fromName(parts[1]);                                                
                    } else if (parts[0].toUpperCase().equals("PETNAME")) {
                        this.playersWithPets.get(player).get(0).petName = parts[1];
                    } else if (parts[0].toUpperCase().equals("PETHP")) {
                        this.playersWithPets.get(player).get(0).hp = Integer.parseInt(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SADDLED")) {
                        this.playersWithPets.get(player).get(0).saddled =  Boolean.parseBoolean(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SHEARED")) {
                        this.playersWithPets.get(player).get(0).sheared = Boolean.parseBoolean(parts[1]);
                    } else if (parts[0].toUpperCase().equals("SHEEPCOLOR")) {
                        this.playersWithPets.get(player).get(0).color = Byte.parseByte(parts[1]);
                    } else if (parts[0].toUpperCase().equals("FOLLOWED")) {
                        this.playersWithPets.get(player).get(0).followed = Boolean.parseBoolean(parts[1]);
                    }                            
                }
                in.close();
                creeperFile.renameTo(new File(this.dataFolder, "pets.txt.old"));
            } catch (Exception e) {
            }
        } else if (creeperFileJson.exists()) {
            System.out.println("Found pets.json. Attempting to load pets.");
            Gson gson = new Gson();            
            try {             
                BufferedReader in = new BufferedReader(new FileReader(creeperFileJson));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }                    
                    String[] parts = line.split("=", 2);
                    String player = parts[0];
                    String json = parts[1];
                    Pet pet = gson.fromJson(json, Pet.class);
                    this.playersWithPets.put(player, new ArrayList<Pet>());
                    this.playersWithPets.get(player).add(pet); 
                    System.out.println("Loaded pet " + pet.type.getName() + " of " + player);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        mainLoop = new PetMainLoop(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            petSpawn(p);
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
        try {
            File petFile = new File(this.dataFolder, "pets.json");
            
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Map.Entry<String, ArrayList<Pet>> entry : playersWithPets.entrySet()) {            
                Gson gson = new Gson();
                ArrayList<Pet> pets = entry.getValue();
                for(Iterator i = pets.iterator();i.hasNext();) {
                    Pet pet = (Pet)i.next();                    
                    String json = gson.toJson((Object)pet);
                    out.write(entry.getKey() + "=" + json + "\n");
                }                
            }
            out.close();
            petList.clear();
            petNameList.clear();
            petFollowList.clear();
            playersWithPets.clear();
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    
    public void petSpawn(Player p) {     
        if (isPetOwner(p)) {            
            for(Iterator i = getPetsOf(p).iterator();i.hasNext();) {  
                Pet pet = (Pet)i.next();
                Location pos = p.getLocation().clone();
                pos.setY(pos.getY() + 1.0D);
                LivingEntity e = (LivingEntity)p.getWorld().spawnCreature(pos, pet.type);
                pet.entityId = e.getEntityId();
                petList.put(e, p);
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
                e.setHealth(pet.hp);    
                petNameList.put(e, pet.petName);
                petFollowList.put(e, pet.followed);
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " greets you!");
            }
        }
    }

    public void despawnPetOf(Player p) {
        if (isPetOwner(p)) {
            playersWithPets.remove(p.getName());
            ArrayList<Pet> pets = new ArrayList<Pet>();
            for (Map.Entry<Entity, Player> entry : petList.entrySet()) {
                Entity e = entry.getKey();                
                if (p == entry.getValue()) {
                    Pet pet = new Pet(e);                    
                    cleanUpLists(e);
                    e.remove();
                    pets.add(pet);
                }
            }
            if (!pets.isEmpty()) {
                playersWithPets.put(p.getName(), pets);
            }
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
        if (this.isPetOwner(p)) {   
            for (Map.Entry<Entity, Player> entry : petList.entrySet()) {
                Entity e = entry.getKey();
                if (e.getWorld().equals(p.getWorld())) {
                    Location pos = p.getLocation().clone();
                    pos.setY(pos.getY() + 1.0D);
                    e.teleport(pos);
                } else {
                    this.despawnPetOf(p);
                    this.petSpawn(p);
                }                
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getNameOfPet(e) + ChatColor.GREEN + " teleported to you.");
            }
        }
    }
    
    public void teleportPetOf(Entity e, Player p) {
        if (e.getWorld().equals(p.getWorld())) {
            Location pos = p.getLocation().clone();
            pos.setY(pos.getY() + 1.0D);
            e.teleport(pos);
        } else {
            this.despawnPetOf(p);
            this.petSpawn(p);
        }                
        p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getNameOfPet(e) + ChatColor.GREEN + " teleported to you.");                    
    }

    public boolean tamePetOf(Player p, Entity pet, boolean spawned) {        
        if (pet instanceof LivingEntity) {
            EntityType et = pet.getType();                
            ItemStack bait = p.getItemInHand();
            int amt = bait.getAmount();
            int hp = ((LivingEntity)pet).getMaxHealth();
            boolean tamed = false;

            /*if (isPetOwner(p)) {
                p.sendMessage("You already have a pet!");
                return false;
            }*/

            if (!hasPerm(p, "petcreeper.tame." + et.getName()) && !hasPerm(p, "petcreeper.tame.All")) {
                p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + et.getName() + ".");
                return false;
            }                        

            if ((bait.getType() == PetConfig.getBait(et)) && (amt > 0)) { 
                if (!isPetOwner(p)) {
                    this.playersWithPets.put(p.getName(), new ArrayList<Pet>());
                }
                Pet newPet = new Pet(pet);
                
                this.playersWithPets.get(p.getName()).add(newPet);                

                if (tamed) {
                    p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + newPet.type.getName() + ChatColor.GREEN + " greets you!");
                } else {
                    if (tamed) {
                        if (amt == 1) {
                            p.getInventory().removeItem(new ItemStack[]{bait});
                        } else {
                            bait.setAmount(amt - 1);
                        }                    
                        if (pet instanceof Monster) {
                            ((Monster)pet).setTarget(null);                            
                        }
                        p.sendMessage(ChatColor.GREEN + "You tamed the " + ChatColor.YELLOW + newPet.type.getName() + ChatColor.GREEN + "!");
                    }   
                }
            }
            return tamed;
        } else {
            return false;
        }
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
                if (pet.e == e) {
                    cleanUpLists(e);
                    p.sendMessage(ChatColor.GREEN + "Your pet "+ ChatColor.YELLOW + pet.petName + ChatColor.GREEN + " is now free!");
                    playersWithPets.remove(pet);
                    
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