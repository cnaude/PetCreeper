package me.cnaude.plugin.PetCreeper;

import java.io.*;
import java.util.HashMap;
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
    public HashMap<String, Pet> playersWithPets = new HashMap();
    public HashMap<Entity, Player> petList = new HashMap();
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
                String player = "";
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }
                    if (line.contains("\t")) {
                        String[] parts = line.split("\t", 5);
                        String pName = parts[0];
                        if (EntityType.fromName(parts[1]) == EntityType.SHEEP) {
                            this.playersWithPets.put(pName, new Pet(Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]), Byte.parseByte(parts[4])));
                        } else if (EntityType.fromName(parts[1]) == EntityType.PIG) {
                            this.playersWithPets.put(pName, new Pet(Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3])));
                        } else {
                            this.playersWithPets.put(pName, new Pet(EntityType.fromName(parts[1]), Integer.parseInt(parts[2])));
                        }
                    } else {
                        String[] parts = line.split(":", 2);
                        parts[1] = parts[1].replaceAll("^\\s+", "");
                        parts[1] = parts[1].replaceAll("\\s+$", "");
                        if (parts[0].toUpperCase().equals("PLAYER")) {                        
                            player = parts[1];
                            System.out.println("Loading pet for " + player);
                            this.playersWithPets.put(player, new Pet());
                        } else if (parts[0].toUpperCase().equals("PETTYPE")) {
                            this.playersWithPets.get(player).type = EntityType.fromName(parts[1]);                                                
                        } else if (parts[0].toUpperCase().equals("PETNAME")) {
                            this.playersWithPets.get(player).petName = parts[1];
                        } else if (parts[0].toUpperCase().equals("PETHP")) {
                            this.playersWithPets.get(player).hp = Integer.parseInt(parts[1]);
                        } else if (parts[0].toUpperCase().equals("SADDLED")) {
                            this.playersWithPets.get(player).saddled =  Boolean.parseBoolean(parts[1]);
                        } else if (parts[0].toUpperCase().equals("SHEARED")) {
                            this.playersWithPets.get(player).sheared = Boolean.parseBoolean(parts[1]);
                        } else if (parts[0].toUpperCase().equals("SHEEPCOLOR")) {
                            this.playersWithPets.get(player).color = Byte.parseByte(parts[1]);
                        } else if (parts[0].toUpperCase().equals("FOLLOWED")) {
                            this.playersWithPets.get(player).followed = Boolean.parseBoolean(parts[1]);
                        }        
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
        File petFile = new File(this.dataFolder, "pets.txt");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Map.Entry<String, Pet> entry : playersWithPets.entrySet()) {
                String player = entry.getKey();
                Pet pet = entry.getValue();                
                out.write("Player: " + player + "\n");
                out.write("PetType: " + pet.type.getName() + "\n");
                out.write("PetHP: " + pet.hp + "\n");                
                out.write("Followed: " + pet.followed + "\n");                
                if (pet.type == EntityType.SHEEP) {
                    out.write("Sheared: " + pet.sheared + "\n");
                    out.write("SheepColor: " + pet.color + "\n");
                } else if (pet.type == EntityType.PIG) {
                    out.write("Saddled: " + pet.saddled + "\n");
                }                
                out.write("PetName: " + pet.petName + "\n");  
                playersWithPets.get(player).e.remove();
                
            }
            out.close();
            petList.clear();
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
                teleportPetOf(p);
                p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getPetNameOf(p) + ChatColor.GREEN + " teleported to you.");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petlist")) {
            if (isPetOwner(p)) {
                p.sendMessage(ChatColor.GREEN + "You are the proud owner of a pet " 
                        + ChatColor.YELLOW + getPetTypeOf(p).getName() 
                        + ChatColor.GREEN + " named " + ChatColor.YELLOW 
                        + getPetNameOf(p) + ChatColor.GREEN + ".");
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
        if (commandLabel.equalsIgnoreCase("petname")) {
            if (isPetOwner(p)) {                
                String s = "";
                for(String i : args) {
                    s +=  i + " ";
                }         
                s = s.substring(0, s.length() - 1);
                if (!s.isEmpty()) {
                    playersWithPets.get(p.getName().toString()).petName = s;
                    p.sendMessage(ChatColor.GREEN + "You named your pet "+ ChatColor.YELLOW + s + ChatColor.GREEN + "!");
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

    public void petSpawn(Player p) {     
        if (isPetOwner(p)) {
            Pet pet = playersWithPets.get(p.getName().toString());

            Location pos = p.getLocation().clone();
            pos.setY(pos.getY() + 1.0D);
            LivingEntity e = (LivingEntity)p.getWorld().spawnCreature(pos, pet.type);
            playersWithPets.get(p.getName().toString()).e = e;
            petList.put(e, p);
            if (pet.hp > e.getMaxHealth()) {
                e.setHealth(e.getMaxHealth());
            } else {
                e.setHealth(pet.hp);                        
            }
            if (pet.type == EntityType.SHEEP) {            
                ((Sheep)e).setSheared(pet.sheared);
                ((Sheep)e).setColor(DyeColor.getByData(pet.color));
            }              
            if (pet.type == EntityType.PIG) {            
                ((Pig)e).setSaddle(pet.saddled);
            } 
            p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getPetNameOf(p) + ChatColor.GREEN + " greets you!");
        }

    }

    public void despawnPetOf(Player p) {
        if (isPetOwner(p)) {
            Entity e = getPetOf(p);
            EntityType et = e.getType();
            if (et == EntityType.SHEEP) {
                Sheep s = (Sheep) e;
                this.playersWithPets.get(p.getName().toString()).sheared = s.isSheared();
                this.playersWithPets.get(p.getName().toString()).color = s.getColor().getData();
            } else if (et == EntityType.PIG) {
                Pig pig = (Pig) e;
                this.playersWithPets.get(p.getName().toString()).saddled = pig.hasSaddle();
            }
            this.playersWithPets.get(p.getName().toString()).hp = ((LivingEntity)e).getHealth();
            e.remove();   
        }
    }
    
    public void teleportPetOf(Player p) {
        if (this.isPetOwner(p)) {                        
            Entity e = getPetOf(p);
            if (e.getWorld().equals(p.getWorld())) {
                Location pos = p.getLocation().clone();
                pos.setY(pos.getY() + 1.0D);
                e.teleport(pos);
            } else {
                this.despawnPetOf(p);
                this.petSpawn(p);
            }                
        }
    }

    public boolean tamePetOf(Player p, Entity pet, boolean spawned) {        
        if (pet instanceof LivingEntity) {
            EntityType et = pet.getType();                
            ItemStack bait = p.getItemInHand();
            int amt = bait.getAmount();
            int hp = ((LivingEntity)pet).getMaxHealth();
            boolean tamed = false;

            if (isPetOwner(p)) {
                p.sendMessage("You already have a pet!");
                return false;
            }

            if (!hasPerm(p, "petcreeper.tame." + et.getName()) && !hasPerm(p, "petcreeper.tame.All")) {
                p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + et.getName() + ".");
                return false;
            }                        

            if ((bait.getType() == PetConfig.getBait(et)) && (amt > 0)) {                                    
                if (et == EntityType.CREEPER) {                
                    pet.remove(); 
                    Entity creeper = p.getWorld().spawnCreature(pet.getLocation(), EntityType.CREEPER);
                    this.petList.put(creeper, p);                                
                    this.playersWithPets.put(p.getName(), new Pet(et, hp));
                    this.playersWithPets.get(p.getName().toString()).e = creeper;
                    tamed = true;
                } else {
                    this.petList.put(pet, p);
                    if (et == EntityType.SHEEP) {
                        Sheep s = (Sheep) pet;
                        this.playersWithPets.put(p.getName(), new Pet(hp, s.isSheared(), s.getColor().getData()));
                    } else if (et == EntityType.PIG) {
                        Pig pig = (Pig) pet;
                        this.playersWithPets.put(p.getName(), new Pet(hp, pig.hasSaddle()));
                    } else {
                        this.playersWithPets.put(p.getName(), new Pet(et, hp));
                    }
                    this.playersWithPets.get(p.getName().toString()).e = pet;
                    tamed = true;
                } 


                if (tamed) {
                    p.sendMessage(ChatColor.GREEN + "Your pet " + ChatColor.YELLOW + getPetNameOf(p) + ChatColor.GREEN + " greets you!");
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
        } else {
            return false;
        }
    }
       
    public void untamePetOf(Player player) {
        if (playersWithPets.containsKey(player.getName())) {
            Entity e = getPetOf(player);
            petList.remove(e);        
            playersWithPets.remove(player.getName());
        }
    }

    public Entity getPetOf(Player player) {
        if (playersWithPets.containsKey(player.getName())) {
            return playersWithPets.get(player.getName()).e;            
        }
        return null;
    }

    public Player getMasterOf(Entity pet) {
        return petList.get(pet);
    }

    public boolean isPet(Entity pet) {
        return petList.containsKey(pet);        
    }    

    public boolean isPetOwner(Player p) {
        return (playersWithPets.containsKey(p.getName().toString()));
    }

    public boolean isFollowed(Player p) {
        if (playersWithPets.containsKey(p.getName().toString())) {
            return playersWithPets.get(p.getName().toString()).followed;
        }
        return false;
    }

    public void setFollowed(Player p, boolean f) {
        playersWithPets.get(p.getName().toString()).followed = f;
    }

    public EntityType getPetTypeOf(Player p) {
        if (playersWithPets.containsKey(p.getName().toString())) {
            return playersWithPets.get(p.getName().toString()).type;
        }
        return null;
    }

    public String getPetNameOf(Player p) {
        String petName = "";
        if (playersWithPets.containsKey(p.getName().toString())) {
            if (playersWithPets.get(p.getName().toString()).petName.isEmpty()) {
                petName = playersWithPets.get(p.getName().toString()).type.getName();
            } else {
                petName = playersWithPets.get(p.getName().toString()).petName;
            }
        }
        return petName; 
    }

    public static PetMain get() {
        return instance;
    }
}