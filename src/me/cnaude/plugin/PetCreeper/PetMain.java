package me.cnaude.plugin.PetCreeper;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    public HashMap<String, Pet> petList = new HashMap();
    private final HashMap<Player, Creature> pets = new HashMap();
    private final HashMap<Player, Slime> slimePets = new HashMap();
    private final HashMap<Player, Ghast> ghastPets = new HashMap();
    private final HashMap<Player, Boolean> petFollow = new HashMap();
    private final HashMap<Player, EntityType> petTypes = new HashMap();
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
        if (creeperFile.exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(creeperFile));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }
                    String[] parts = line.split("\t", 5);
                    String pName = parts[0];
                    if (EntityType.fromName(parts[1]) == EntityType.SHEEP) {
                        this.petList.put(pName, new Pet(Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]), Byte.parseByte(parts[4])));
                    } else if (EntityType.fromName(parts[1]) == EntityType.PIG) {
                        this.petList.put(pName, new Pet(Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3])));
                    } else {
                        this.petList.put(pName, new Pet(EntityType.fromName(parts[1]), Integer.parseInt(parts[2])));
                    }
                }
                in.close();
            } catch (Exception e) {
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

        for (Map.Entry entry : this.pets.entrySet()) {
            Player p = (Player) entry.getKey();
            Creature c = (Creature) entry.getValue();

            if ((c instanceof Sheep)) {
                Sheep s = (Sheep) c;
                this.petList.put(p.getName(), new Pet(s.getHealth(), s.isSheared(), s.getColor().getData()));
            } else if ((c instanceof Pig)) {
                Pig pig = (Pig) c;
                this.petList.put(p.getName(), new Pet(pig.getHealth(), pig.hasSaddle()));
            } else {
                this.petList.put(p.getName(), new Pet(getPetTypeOf(p), c.getHealth()));
            }
            c.remove();
        }
        
        for (Map.Entry entry : this.slimePets.entrySet()) {
            Player p = (Player) entry.getKey();
            Slime c = (Slime) entry.getValue();
            this.petList.put(p.getName(), new Pet(getPetTypeOf(p), c.getHealth()));            
            c.remove();
        }
        
        for (Map.Entry entry : this.ghastPets.entrySet()) {
            Player p = (Player) entry.getKey();
            Ghast c = (Ghast) entry.getValue();
            this.petList.put(p.getName(), new Pet(getPetTypeOf(p), c.getHealth()));            
            c.remove();
        }

        File petFile = new File(this.dataFolder, "pets.txt");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Map.Entry<String, Pet> entry : petList.entrySet()) {
                String player = entry.getKey();
                Pet pet = entry.getValue();
                
                out.write(player + "\t" + pet.type.getName() + "\t" + pet.hp);
                if (pet.type == EntityType.SHEEP) {
                    out.write("\t" + pet.sheared + "\t" + pet.color);
                } else if (pet.type == EntityType.PIG) {
                    out.write("\t" + pet.saddled);
                }
                out.write("\n");
            }
            out.close();
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
                teleportPetOf(p);
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getPetNameOf(p) + ChatColor.GREEN + " teleported to you.");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petlist")) {
            if (isPetOwner(p)) {
                p.sendMessage(ChatColor.GREEN + "You are the proud owner of a pet " + ChatColor.YELLOW + getPetNameOf(p) + ChatColor.GREEN + ".");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petfree")) {
            if (isPetOwner(p)) {
                String s = getPetNameOf(p);
                untamePetOf(p);
                p.sendMessage(ChatColor.GREEN + "You freed your pet "+ ChatColor.YELLOW + s + ChatColor.GREEN + "!");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        return false;
    }

    public void petSpawn(Player p) {
        if (petList.containsKey(p.getName())) {
            Pet pet = petList.get(p.getName());
            spawnPetOf(p, pet.type, pet.hp, pet.sheared, pet.saddled, pet.color);    
            petList.remove(p.getName());
        }
    }

    public void spawnPetOf(Player p, EntityType type, int hp, boolean sheared, boolean saddled, byte color) {  
        if (type == EntityType.SLIME) {
            Slime c = getSlimePetOf(p);
            if (c != null) {
                c.remove();
                untamePetOf(p);
            }
        } else if (type == EntityType.GHAST) {
            Ghast c = getGhastPetOf(p);
            if (c != null) {
                c.remove();
                untamePetOf(p);
            }
        } else {
            Creature c = getPetOf(p);
            if (c != null) {
                c.remove();
                untamePetOf(p);
            }
        }
        Location pos = p.getLocation().clone();
        pos.setY(pos.getY() + 1.0D);
        LivingEntity e =  (LivingEntity)p.getWorld().spawnCreature(pos, type);
        if (hp > e.getMaxHealth()) {
            e.setHealth(e.getMaxHealth());
        } else {
            e.setHealth(hp);                        
        }
        if (type == EntityType.SHEEP) {            
            ((Sheep)e).setSheared(sheared);
            ((Sheep)e).setColor(DyeColor.getByData(color));
        }              
        if (type == EntityType.PIG) {            
            ((Pig)e).setSaddle(saddled);
        } 
        tamePetOf(p, e, true);
        }


    public void despawnPetOf(Player p) {
        if (this.isPetOwner(p)) {
            EntityType et = getPetTypeOf(p);
            if (et == EntityType.SLIME) {
                Slime c = this.getSlimePetOf(p);
                this.petList.put(p.getName(), new Pet(et, c.getHealth()));
                this.untamePetOf(p);
                c.remove();
            } else if (et == EntityType.GHAST) {
                Ghast c = this.getGhastPetOf(p);
                this.petList.put(p.getName(), new Pet(et, c.getHealth()));
                this.untamePetOf(p);
                c.remove();
            } else {
                Creature c = this.getPetOf(p);
                if (et == EntityType.SHEEP) {
                    Sheep s = (Sheep) c;
                    this.petList.put(p.getName(), new Pet(s.getHealth(), s.isSheared(), s.getColor().getData()));
                } else if (et == EntityType.PIG) {
                    Pig pig = (Pig) c;
                    this.petList.put(p.getName(), new Pet(pig.getHealth(), pig.hasSaddle()));
                } else {
                    this.petList.put(p.getName(), new Pet(et, c.getHealth()));
                }
                this.untamePetOf(p);
                c.remove();
            }
        }
    }
    
    public void teleportPetOf(Player p) {
        if (this.isPetOwner(p)) {
            EntityType et = getPetTypeOf(p);
            if (et == EntityType.SLIME) {
                Slime c = getSlimePetOf(p);
                if (c.getWorld().equals(p.getWorld())) {
                    Location pos = p.getLocation().clone();
                    pos.setY(pos.getY() + 1.0D);
                    c.teleport(pos);
                } else {
                    this.despawnPetOf(p);
                    this.petSpawn(p);
                }    
            } else if (et == EntityType.GHAST) {
                Ghast c = getGhastPetOf(p);
                if (c.getWorld().equals(p.getWorld())) {
                    Location pos = p.getLocation().clone();
                    pos.setY(pos.getY() + 1.0D);
                    c.teleport(pos);
                } else {
                    this.despawnPetOf(p);
                    this.petSpawn(p);
                }    
            } else {
                Creature c = getPetOf(p);
                if (c.getWorld().equals(p.getWorld())) {
                    Location pos = p.getLocation().clone();
                    pos.setY(pos.getY() + 1.0D);
                    c.teleport(pos);
                } else {
                    this.despawnPetOf(p);
                    this.petSpawn(p);
                }            
            }
        }
    }

    public boolean tamePetOf(Player p, Entity pet, boolean spawned) {
        Location loc = pet.getLocation();
        EntityType et = pet.getType();                
        ItemStack bait = p.getItemInHand();
        int amt = bait.getAmount();
        boolean tamed = false;
        
        if (isPetOwner(p)) {
            p.sendMessage("You already have a pet!");
            return false;
        }
        
        if (!hasPerm(p, "petcreeper.tame." + et.getName()) && !hasPerm(p, "petcreeper.tame.All")) {
            p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + et.getName() + ".");
            return false;
        }        
        
        untamePetOf(p);
        
        if ((bait.getType() == PetConfig.getBait(et)) && (amt > 0)) {                        
            if (et == EntityType.SLIME) {
                this.slimePets.put(p, (Slime)pet);
                this.petTypes.put(p, et);
                tamed = true;
            } else if (et == EntityType.GHAST) {
                this.ghastPets.put(p, (Ghast)pet);
                this.petTypes.put(p, et);
                tamed = true;
            } else if (et == EntityType.CREEPER) {
                // We reomve the creeper to fix the bloated creeper effect
                pet.remove();
                Creature c = (Creature) p.getWorld().spawnCreature(loc, EntityType.CREEPER);
                this.pets.put(p, c);
                this.petTypes.put(p, et);
                tamed = true;
            } else if (pet instanceof Creature) {
                this.pets.put(p, (Creature)pet);
                this.petTypes.put(p, et);
                tamed = true;
            } 

            if (spawned) {
                p.sendMessage(ChatColor.GREEN + "Your pet " + getPetNameOf(p) + " greets you!");
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
                    p.sendMessage(ChatColor.GREEN + "You tamed the " + getPetNameOf(p) + "!");
                }   
            }
        }
        return tamed;
    }
       
    public void untamePetOf(Player player) {
        if (this.pets.containsKey(player)) {
            this.pets.remove(player);
            this.petFollow.remove(player);
            this.petTypes.remove(player);
        }
        if (this.slimePets.containsKey(player)) {
            this.slimePets.remove(player);
            this.petFollow.remove(player);
            this.petTypes.remove(player);
        }        
        if (this.ghastPets.containsKey(player)) {
            this.ghastPets.remove(player);
            this.petFollow.remove(player);
            this.petTypes.remove(player);
        }
    }

    public Creature getPetOf(Player player) {
        if (this.pets.containsKey(player)) {
            return (Creature) this.pets.get(player);
        }
        return null;
    }

    public Slime getSlimePetOf(Player player) {
        if (this.slimePets.containsKey(player)) {
            return (Slime) this.slimePets.get(player);
        }
        return null;
    }
            
    public Ghast getGhastPetOf(Player player) {
        if (this.ghastPets.containsKey(player)) {
            return (Ghast) this.ghastPets.get(player);
        }
        return null;
    }

    public Player getMasterOf(Entity pet) {
        if (pet.getType() == EntityType.SLIME) {
            Slime c = (Slime)pet;
            if (this.slimePets.containsValue(c)) {
                for (Map.Entry entry : this.slimePets.entrySet()) {
                    if (entry.getValue().equals(c)) {
                        return (Player) entry.getKey();
                    }
                }            
            }
        } else if (pet.getType() == EntityType.GHAST) {
            Ghast c = (Ghast)pet;
           if (this.ghastPets.containsValue(c)) {
                for (Map.Entry entry : this.ghastPets.entrySet()) {
                    if (entry.getValue().equals(c)) {
                        return (Player) entry.getKey();
                    }
                }         
            }
        } else if (pet instanceof Creature) {
            Creature c = (Creature)pet;
            if (this.pets.containsValue(c)) {
                for (Map.Entry entry : this.pets.entrySet()) {
                    if (entry.getValue().equals(c)) {
                        return (Player) entry.getKey();
                    }
                }                
            }
        }
        return null;
    }

    public boolean isPet(Entity pet) {
        if (pet.getType() == EntityType.SLIME) {            
            return this.slimePets.containsValue((Slime)pet);
        } else if (pet.getType() == EntityType.GHAST) {            
            return this.ghastPets.containsValue((Ghast)pet);
        } else if (pet instanceof Creature) {
            return this.pets.containsValue((Creature)pet);
        } else {
            return false;
        }
    }    

    public boolean isPetOwner(Player p) {
        if (this.pets.containsKey(p)
                || this.slimePets.containsKey(p)
                || this.ghastPets.containsKey(p)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFollowed(Player p) {
        if (this.petFollow.containsKey(p)) {
            return ((Boolean) this.petFollow.get(p)).booleanValue();
        }
        return true;
    }

    public void setFollowed(Player p, boolean f) {
        this.petFollow.put(p, Boolean.valueOf(f));
    }

    public EntityType getPetTypeOf(Player p) {
        if (this.petTypes.containsKey(p)) {
            return (EntityType) this.petTypes.get(p);
        }
        return null;
    }

    public String getPetNameOf(Player p) {
        return getPetTypeOf(p).getName();
    }

    public Set<Player> getMasterList() {
        return this.pets.keySet();
    }

    public static PetMain get() {
        return instance;
    }
}