/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.petcreeper.Commands;

import com.cnaude.petcreeper.PetCreeper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Zombie;

/**
 *
 * @author cnaude
 */
public class PetSpawnCommand implements CommandExecutor {

    private final PetCreeper plugin;

    public PetSpawnCommand(PetCreeper instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            int spawnCount = 1;
            if (args.length == 2) {
                if (args[1].matches("\\d+")) {
                    spawnCount = Integer.parseInt(args[1]);
                }
            }
            if (spawnCount > plugin.config.maxSpawnCount) {
                spawnCount = plugin.config.maxSpawnCount;
            }
            if (args.length >= 1) {
                String petType = args[0];
                String subType = "";
                if (petType.equalsIgnoreCase("ocelot")) {
                    petType = "Ozelot";
                } else if (petType.equalsIgnoreCase("wither")) {
                    petType = "WitherBoss";
                } else if (petType.equalsIgnoreCase("zombievillager")) {
                    petType = "Zombie";
                    subType = "Villager";
                } else if (petType.equalsIgnoreCase("witherskeleton")) {
                    petType = "Skeleton";
                    subType = "Wither";
                } else if (petType.equalsIgnoreCase("redcat")) {
                    petType = "Ozelot";
                    subType = "red_cat";
                } else if (petType.equalsIgnoreCase("blackcat")) {
                    petType = "Ozelot";
                    subType = "black_cat";
                } else if (petType.equalsIgnoreCase("siamesecat")) {
                    petType = "Ozelot";
                    subType = "siamese_cat";
                } else if (petType.equalsIgnoreCase("wildcat")) {
                    petType = "Ozelot";
                    subType = "wild_ocelot";
                } 
                EntityType et = EntityType.fromName(petType);
                if (et != null) {
                    if (!et.isAlive()) {
                        plugin.message(p, ChatColor.RED + "Invalid pet type.");
                        return true;
                    }
                    if (!plugin.hasPerm(p, "petcreeper.spawn." + et.getName()) && !plugin.hasPerm(p, "petcreeper.spawn.All")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to spawn a " + ChatColor.WHITE 
                                + et.getName() + ChatColor.RED + ".");
                        return true;
                    }
                    for (int x = 1; x <= spawnCount; x++) {
                        if (plugin.isPetOwner(p)) {
                            if (plugin.getPetsOf(p).size() >= plugin.config.maxPetsPerPlayer) {
                                p.sendMessage(ChatColor.RED + "You have too many pets!");
                                return true;
                            }
                        }
                        Location pos = p.getLocation().clone();
                        if (et.equals(EntityType.ENDER_DRAGON)) {
                            pos.setY(pos.getY() + 15.0D);
                        } else {
                            pos.setY(pos.getY() + 1.0D);
                        }
                        Entity e = p.getWorld().spawnEntity(pos, et);
                        if (e instanceof Ageable) {
                            if (plugin.config.defaultPetAge.equalsIgnoreCase("baby")) {
                                ((Ageable)e).setBaby();
                                ((Ageable)e).setAgeLock(plugin.config.lockSpawnedBabies);
                            } else if (plugin.config.defaultPetAge.equalsIgnoreCase("adult")) {
                                ((Ageable)e).setAdult();
                            } 
                        }
                        if (e instanceof Skeleton) {
                            if (subType.equalsIgnoreCase("wither")) {
                                ((Skeleton) e).setSkeletonType(SkeletonType.WITHER);
                            }
                        }
                        if (e instanceof Zombie) {
                            if (subType.equalsIgnoreCase("villager")) {
                                ((Zombie)e).setVillager(true);
                            }
                            if (plugin.config.defaultPetAge.equalsIgnoreCase("baby")) {
                                ((Zombie)e).setBaby(true);                                
                            }
                        }
                        if (e instanceof Ocelot) {
                            if (!subType.isEmpty()) {
                                ((Ocelot)e).setCatType(Ocelot.Type.valueOf(subType.toUpperCase()));
                            }                            
                        }
                        if (plugin.tamePetOf(p, e, true)) {
                            p.sendMessage(ChatColor.GREEN + "You spawned a pet " + ChatColor.YELLOW 
                                    + et.getName() + ChatColor.GREEN + " named " + ChatColor.YELLOW 
                                    + plugin.getNameOfPet(e) + ChatColor.GREEN + "!");
                        } else {                            
                            p.sendMessage(ChatColor.RED + "Unable to spawn a pet!");
                            if (e != null) {
                                // We should never really get here...
                                e.remove();
                            }
                        }
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "Invalid pet type.");
                }
            } else {
                plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + plugin.config.commandPrefix + "spawn [pet type] ([count])");
            }
        }
        return true;
    }
}
