/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class PetCommands implements CommandExecutor {
    
    private final PetMain plugin;
    
    public PetCommands(PetMain instance) {
        this.plugin = instance;

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
            if (!this.plugin.hasPerm(p, "petcreeper.pet")) {
                p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (this.plugin.isPetOwner(p)) {
                this.plugin.teleportPetsOf(p, true);
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petlist")) {
            if (this.plugin.isPetOwner(p)) {
                this.plugin.printPetListOf(p);
            } else {
                p.sendMessage(ChatColor.RED + "You don't own a pet.");
            }
            return true;
        }
        if (commandLabel.equalsIgnoreCase("petname")) {
            if (this.plugin.isPetOwner(p)) {
                if (args.length > 1 && args[0].matches("\\d+")) {
                    int idx = Integer.parseInt(args[0]);
                    if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                        String s = "";
                        for (int i = 1; i < args.length; i++) {
                            s += args[i] + " ";
                        }
                        s = s.substring(0, s.length() - 1);
                        Pet pet = this.plugin.getPetsOf(p).get(idx);
                        if (!s.isEmpty()) {
                            pet.petName = s;
                            p.sendMessage(ChatColor.GREEN + "You named your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + "!");
                        } else {
                            p.sendMessage(ChatColor.RED + "Invalid pet name.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Invalid pet ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/petname [id] [name]");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You have no pets. :(");
            }
        }
        if (commandLabel.equalsIgnoreCase("petfree")) {
            if (this.plugin.isPetOwner(p)) {
                if (args.length == 1 && args[0].matches("\\d+")) {
                    int idx = Integer.parseInt(args[0]);
                    if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                        Pet pet = this.plugin.getPetsOf(p).get(idx);
                        Entity e = plugin.getEntityOfPet(pet);
                        plugin.untamePetOf(p, e);                            
                    } else {
                        p.sendMessage(ChatColor.RED + "Invalid pet ID.");
                    }
                } else if (args[0].toString().equalsIgnoreCase("all")) {
                    plugin.untameAllPetsOf(p);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/petfree [id|all] [name]");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You have no pets. :(");
            }
        }
        if (commandLabel.equalsIgnoreCase("petgive")) {
            if (this.plugin.isPetOwner(p)) {
                if (args.length == 2 && args[0].matches("\\d+")) {
                    int idx = Integer.parseInt(args[0]);
                    if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {                        
                        Pet pet = this.plugin.getPetsOf(p).get(idx);
                        Entity e = plugin.getEntityOfPet(pet);
                        Player rec = plugin.getServer().getPlayer(args[1]);
                        if (rec != null && rec instanceof Player) {                           
                            if (plugin.isPetOwner(rec)) {
                                if (plugin.getPetsOf(rec).size() >= PetConfig.maxPetsPerPlayer) {
                                    p.sendMessage(ChatColor.RED + "Player " + rec.getName() + " already has maximum number of pets!");    
                                    return true;
                                }
                            }
                            plugin.untamePetOf(p, e);                                    
                            if (plugin.tamePetOf(rec, e, true)) {
                                p.sendMessage(ChatColor.GREEN + "You gave your pet " + ChatColor.YELLOW 
                                        + pet.petName + ChatColor.GREEN + " to " + ChatColor.YELLOW + rec.getName() 
                                        + ChatColor.GREEN + ".");                                
                            } else {
                                p.sendMessage(ChatColor.RED + "Error give pet to player!");
                            }                                                                                                                           
                        } else {
                            p.sendMessage(ChatColor.RED + "Invalid player.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "Invalid pet ID.");
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/petgive [id] [player]");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You have no pets. :(");
            }
        }
        return true;
    }
}
