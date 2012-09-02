/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if(commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "reload")) {
                if (p.hasPermission("petcreeper.reload")) {                
                    this.plugin.loadConfig();
                    this.plugin.message(p,"Configuration reloaded.");
                } else {
                    this.plugin.message(p,"No permission to reload PetCreeper config!");
                }                
            }


            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix)) {
                if (!this.plugin.hasPerm(p, "petcreeper.pet")) {
                    this.plugin.message(p,ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (p.isInsideVehicle()) {
                    if (p.getVehicle().getType().isAlive()) {
                        this.plugin.message(p,ChatColor.RED + "You can't use /" + PetConfig.commandPrefix + " when riding this " + p.getVehicle().getType().getName() + ".");
                        return true;
                    }
                }
                if (this.plugin.isPetOwner(p)) {
                    if (args.length == 1) {
                        if (args[0].matches("\\d+")) {
                            int idx = Integer.parseInt(args[0]) - 1;
                            if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                                Pet pet = this.plugin.getPetsOf(p).get(idx);                                
                                plugin.teleportPet(pet, true);                            
                            } else {
                                this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                            }
                        } else if (args[0].toString().equalsIgnoreCase("all")) {
                            plugin.teleportPetsOf(p,true);
                        } else {
                            this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + " [id|all]");
                        }
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + " [id|all]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "list")) {
                if (this.plugin.isPetOwner(p)) {
                    this.plugin.printPetListOf(p);
                } else {
                    this.plugin.message(p,ChatColor.RED + "You don't own a pet.");
                }
                return true;
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "mode")) {
                if (!this.plugin.hasPerm(p, "petcreeper.mode")) {
                    this.plugin.message(p,ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (this.plugin.isPetOwner(p)) {
                    if (args.length == 2 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                            String s = args[1].toLowerCase().substring(0,1);                            
                            Pet pet = this.plugin.getPetsOf(p).get(idx);                            
                            if (s.startsWith("a")) {
                                pet.mode = Pet.modes.AGGRESSIVE;
                                this.plugin.message(p,ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                            } else if (s.startsWith("p")) {
                                pet.mode = Pet.modes.PASSIVE;
                                this.plugin.message(p,ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                            } else if (s.startsWith("d")) {
                                pet.mode = Pet.modes.DEFENSIVE;
                                this.plugin.message(p,ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!"); 
                            } else {
                                this.plugin.message(p,ChatColor.RED + "Invalid pet mode.");
                            }
                        } else {
                            this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "mode [id] [p|d|a]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "name")) {
                if (this.plugin.isPetOwner(p)) {
                    if (args.length > 1 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                            String s = "";
                            for (int i = 1; i < args.length; i++) {
                                s += args[i] + " ";
                            }
                            s = s.substring(0, s.length() - 1);
                            Pet pet = this.plugin.getPetsOf(p).get(idx);
                            if (!s.isEmpty()) {
                                pet.petName = s;
                                Entity e = plugin.getEntityOfPet(pet);
                                if (plugin.petNameList.containsKey(e)) {
                                    plugin.petNameList.remove(e);
                                    plugin.petNameList.put(e, s);
                                }
                                this.plugin.message(p,ChatColor.GREEN + "You named your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + "!");
                            } else {
                                this.plugin.message(p,ChatColor.RED + "Invalid pet name.");
                            }
                        } else {
                            this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "name [id] [name]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "free")) {
                if (this.plugin.isPetOwner(p)) {
                    if (args.length == 1) {
                        if (args[0].matches("\\d+")) {
                            int idx = Integer.parseInt(args[0]) - 1;
                            if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                                Pet pet = this.plugin.getPetsOf(p).get(idx);
                                Entity e = plugin.getEntityOfPet(pet);
                                plugin.untamePetOf(p, e, true);                            
                            } else {
                                this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                            }
                        } else if (args[0].toString().equalsIgnoreCase("all")) {
                            plugin.untameAllPetsOf(p);
                        } else {
                            this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "free [id|all]");
                        }
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "free [id|all]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "info")) {
                if (this.plugin.isPetOwner(p)) {
                    if (args.length == 1 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {
                            Pet pet = this.plugin.getPetsOf(p).get(idx);
                            Entity e = plugin.getEntityOfPet(pet);
                            plugin.printPetInfo(p, e);                            
                        } else {
                            this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                        }                
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "info [id]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "give")) {
                if (!this.plugin.hasPerm(p, "petcreeper.give")) {
                    this.plugin.message(p,ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (this.plugin.isPetOwner(p)) {
                    if (args.length == 2 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < this.plugin.getPetsOf(p).size()) {                        
                            Pet pet = this.plugin.getPetsOf(p).get(idx);
                            Entity e = plugin.getEntityOfPet(pet);
                            Player rec = plugin.getServer().getPlayer(args[1]);
                            if (rec != null && rec instanceof Player) {                           
                                if (plugin.isPetOwner(rec)) {
                                    if (plugin.getPetsOf(rec).size() >= PetConfig.maxPetsPerPlayer) {
                                        this.plugin.message(p,ChatColor.RED + "Player " + rec.getName() + " already has maximum number of pets!");    
                                        return true;
                                    }
                                }
                                plugin.untamePetOf(p, e, false);                                    
                                if (plugin.tamePetOf(rec, e, true)) {
                                    this.plugin.message(p,ChatColor.GREEN + "You gave your pet " + ChatColor.YELLOW 
                                            + pet.petName + ChatColor.GREEN + " to " + ChatColor.YELLOW + rec.getName() 
                                            + ChatColor.GREEN + ".");                                
                                } else {
                                    this.plugin.message(p,ChatColor.RED + "Error give pet to player!");
                                }                                                                                                                           
                            } else {
                                this.plugin.message(p,ChatColor.RED + "Invalid player.");
                            }
                        } else {
                            this.plugin.message(p,ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        this.plugin.message(p,ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "give [id] [player]");
                    }
                } else {
                    this.plugin.message(p,ChatColor.RED + "You have no pets. :(");
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if(commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "reload")) {
                this.plugin.loadConfig();            
            }
        }
        return true;
    }
}
