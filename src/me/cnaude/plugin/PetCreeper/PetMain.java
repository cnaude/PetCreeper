package me.cnaude.plugin.PetCreeper;

import java.io.*;
import java.util.ArrayList;
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
import org.bukkit.plugin.java.JavaPlugin;

public class PetMain extends JavaPlugin {

    private final PetPlayerListener playerListener = new PetPlayerListener(this);
    private final PetEntityListener entityListener = new PetEntityListener(this);
    private static PetMain instance = null;
    public ArrayList<Pet> petList = new ArrayList();
    private final HashMap<Player, Creature> pets = new HashMap();
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
                    if (EntityType.fromName(parts[1]) == EntityType.SHEEP) {
                        this.petList.add(new Pet(parts[0], Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3]), Byte.parseByte(parts[4])));
                    } else if (EntityType.fromName(parts[1]) == EntityType.PIG) {
                        this.petList.add(new Pet(parts[0], Integer.parseInt(parts[2]), Boolean.parseBoolean(parts[3])));
                    } else {
                        this.petList.add(new Pet(parts[0], EntityType.fromName(parts[1]), Integer.parseInt(parts[2])));
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
                this.petList.add(new Pet(p.getName(), s.getHealth(), s.isSheared(), s.getColor().getData()));
            } else if ((c instanceof Pig)) {
                Pig pig = (Pig) c;
                this.petList.add(new Pet(p.getName(), pig.getHealth(), pig.hasSaddle()));
            } else {
                this.petList.add(new Pet(p.getName(), getPetTypeOf(p), c.getHealth()));
            }
            c.remove();
        }

        File petFile = new File(this.dataFolder, "pets.txt");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(petFile));
            for (Pet pet : this.petList) {
                out.write(pet.player + "\t" + pet.type.getName() + "\t" + pet.hp);
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
            if (!p.hasPermission("petcreeper.pet")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (isPetOwner(p)) {
                teleportPetOf(p);
                p.sendMessage(ChatColor.GREEN + "Your " + getPetNameOf(p) + " teleported to you.");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petfree")) {
            if (isPetOwner(p)) {
                untamePetOf(p);
                p.sendMessage("You freed your pet.");
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        return false;
    }

    public void petSpawn(Player p) {                
        for (int i = 0; i < this.petList.size(); i++) {
            Pet pet = (Pet) this.petList.get(i);
            if (!pet.player.equals(p.getName())) {
                continue;
            }
            this.petList.remove(i);
            Creature c = this.spawnPetOf(p, pet.type);
            c.setHealth(pet.hp);
            if (pet.type == EntityType.SHEEP) {
                Sheep s = (Sheep) c;
                if (pet.sheared) {
                    s.setSheared(true);
                }
                s.setColor(DyeColor.getByData(pet.color));
            } else if (pet.type == EntityType.PIG) {
                Pig pig = (Pig) c;
                if (pet.saddled) {
                    pig.setSaddle(true);
                }
            }
            p.sendMessage(ChatColor.GREEN + "Your pet " + this.getPetNameOf(p) + " greets you.");
            break;
        }
    }
    
    public Creature spawnPetOf(Player p, EntityType type) {
        Creature c = getPetOf(p);
        if (c != null) {
            c.remove();
            untamePetOf(p);
        }

        Location pos = p.getLocation().clone();
        pos.setY(pos.getY() + 1.0D);
        c = (Creature) p.getWorld().spawnCreature(pos, type);
        if (c == null) {
            return null;
        }
        tamePetOf(p, c);
        return c;
    }
    
    public void despawnPetOf(Player p) {
        if (this.isPetOwner(p)) {
            Creature c = this.getPetOf(p);
            if ((c instanceof Sheep)) {
                Sheep s = (Sheep) c;
                this.petList.add(new Pet(p.getName(), s.getHealth(), s.isSheared(), s.getColor().getData()));
            } else if ((c instanceof Pig)) {
                Pig pig = (Pig) c;
                this.petList.add(new Pet(p.getName(), pig.getHealth(), pig.hasSaddle()));
            } else {
                this.petList.add(new Pet(p.getName(), this.getPetTypeOf(p), c.getHealth()));
            }
            this.untamePetOf(p);
            c.remove();
        }
    }

    public void teleportPetOf(Player p) {
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

    
    
    public void tamePetOf(Player p, Creature pet) {
        untamePetOf(p);
        Location loc = pet.getLocation();
        if (pet != null) {
            if (pet.getType().equals(EntityType.CREEPER)) {
                pet.remove();
                Creature c = (Creature)p.getWorld().spawnCreature(loc, EntityType.CREEPER);
                this.pets.put(p, c);
                this.petTypes.put(p, c.getType());
            } else {
                this.pets.put(p, pet);
                this.petTypes.put(p, pet.getType());
            }
        }
    }

    public void untamePetOf(Player player) {
        if (this.pets.containsKey(player)) {
            this.pets.remove(player);
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

    public Player getMasterOf(Creature pet) {
        if (this.pets.containsValue(pet)) {
            for (Map.Entry entry : this.pets.entrySet()) {
                if (entry.getValue().equals(pet)) {
                    return (Player) entry.getKey();
                }
            }
            return null;
        }

        return null;
    }

    public boolean isPet(Creature pet) {
        return this.pets.containsValue(pet);
    }

    public boolean isPetOwner(Player p) {
        return this.pets.containsKey(p);
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

/* Location:           C:\Users\naudec.BWI\Downloads\PetCreeper\PetCreeper.jar
 * Qualified Name:     mathew.petcreeper.PetMain
 * JD-Core Version:    0.6.0
 */