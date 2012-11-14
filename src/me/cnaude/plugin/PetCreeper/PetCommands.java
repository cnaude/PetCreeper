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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class PetCommands implements CommandExecutor {

    private final PetMain plugin;

    public PetCommands(PetMain instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "reload")) {
                if (p.hasPermission("petcreeper.reload")) {
                    plugin.loadConfig();
                    plugin.message(p, "Configuration reloaded.");
                } else {
                    plugin.message(p, "No permission to reload PetCreeper config!");
                }
            }


            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix)) {
                if (!plugin.hasPerm(p, "petcreeper.pet")) {
                    plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (p.isInsideVehicle()) {
                    if (p.getVehicle().getType().isAlive()) {
                        plugin.message(p, ChatColor.RED + "You can't use /" + PetConfig.commandPrefix + " when riding this " + p.getVehicle().getType().getName() + ".");
                        return true;
                    }
                }
                if (plugin.isPetOwner(p)) {
                    if (args.length == 1) {
                        if (args[0].matches("\\d+")) {
                            int idx = Integer.parseInt(args[0]) - 1;
                            if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                                Pet pet = plugin.getPetsOf(p).get(idx);
                                plugin.teleportPet(pet, true);
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                            }
                        } else if (args[0].toString().equalsIgnoreCase("all")) {
                            plugin.teleportPetsOf(p, true);
                        } else {
                            plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + " [id|all]");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + " [id|all]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "list")) {
                if (plugin.isPetOwner(p)) {
                    plugin.printPetListOf(p);
                } else {
                    plugin.message(p, ChatColor.RED + "You don't own a pet.");
                }
                return true;
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "mode")) {
                if (!plugin.hasPerm(p, "petcreeper.mode")) {
                    plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (plugin.isPetOwner(p)) {
                    if (args.length == 2 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                            String s = args[1].toLowerCase().substring(0, 1);
                            Pet pet = plugin.getPetsOf(p).get(idx);
                            if (s.startsWith("a")) {
                                pet.mode = Pet.modes.AGGRESSIVE;
                                plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                            } else if (s.startsWith("p")) {
                                pet.mode = Pet.modes.PASSIVE;
                                plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                            } else if (s.startsWith("d")) {
                                pet.mode = Pet.modes.DEFENSIVE;
                                plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid pet mode.");
                            }
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "mode [id] [p|d|a]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "name")) {
                if (plugin.isPetOwner(p)) {
                    if (args.length > 1 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                            String s = "";
                            for (int i = 1; i < args.length; i++) {
                                s += args[i] + " ";
                            }
                            s = s.substring(0, s.length() - 1);
                            Pet pet = plugin.getPetsOf(p).get(idx);
                            if (!s.isEmpty()) {
                                pet.petName = s;
                                Entity e = plugin.getEntityOfPet(pet);
                                if (plugin.petNameList.containsKey(e)) {
                                    plugin.petNameList.remove(e);
                                    plugin.petNameList.put(e, s);
                                }
                                plugin.message(p, ChatColor.GREEN + "You named your pet " + ChatColor.YELLOW + pet.petName + ChatColor.GREEN + "!");
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid pet name.");
                            }
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "name [id] [name]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "free")) {
                if (plugin.isPetOwner(p)) {
                    if (args.length == 1) {
                        if (args[0].matches("\\d+")) {
                            int idx = Integer.parseInt(args[0]) - 1;
                            if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                                Pet pet = plugin.getPetsOf(p).get(idx);
                                Entity e = plugin.getEntityOfPet(pet);
                                plugin.untamePetOf(p, e, true);
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                            }
                        } else if (args[0].toString().equalsIgnoreCase("all")) {
                            plugin.untameAllPetsOf(p);
                        } else {
                            plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "free [id|all]");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "free [id|all]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "info")) {
                if (plugin.isPetOwner(p)) {
                    if (args.length == 1 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                            Pet pet = plugin.getPetsOf(p).get(idx);
                            Entity e = plugin.getEntityOfPet(pet);
                            plugin.printPetInfo(p, e);
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "info [id]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "give")) {
                if (!plugin.hasPerm(p, "petcreeper.give")) {
                    plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (plugin.isPetOwner(p)) {
                    if (args.length == 2 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                            Pet pet = plugin.getPetsOf(p).get(idx);
                            Entity e = plugin.getEntityOfPet(pet);
                            Player rec = plugin.getServer().getPlayer(args[1]);
                            if (rec != null && rec instanceof Player) {
                                if (plugin.isPetOwner(rec)) {
                                    if (plugin.getPetsOf(rec).size() >= PetConfig.maxPetsPerPlayer) {
                                        plugin.message(p, ChatColor.RED + "Player " + rec.getName() + " already has maximum number of pets!");
                                        return true;
                                    }
                                }
                                plugin.untamePetOf(p, e, false);
                                if (plugin.tamePetOf(rec, e, true)) {
                                    plugin.message(p, ChatColor.GREEN + "You gave your pet " + ChatColor.YELLOW
                                            + pet.petName + ChatColor.GREEN + " to " + ChatColor.YELLOW + rec.getName()
                                            + ChatColor.GREEN + ".");
                                } else {
                                    plugin.message(p, ChatColor.RED + "Error give pet to player!");
                                }
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid player.");
                            }
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "give [id] [player]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "spawn")) {
                int spawnCount = 1;
                if (args.length == 2) {
                    if (args[1].matches("\\d+")) {
                        spawnCount = Integer.parseInt(args[1]);
                        if (spawnCount > PetConfig.maxSpawnCount) {
                            spawnCount = PetConfig.maxSpawnCount;
                        }
                    }
                }
                if (args.length >= 1) {
                    String petType = args[0];
                    if (petType.equalsIgnoreCase("ocelot")) {
                        petType = "Ozelot";
                    } else if (petType.equalsIgnoreCase("wither")) {
                        petType = "WitherBoss";
                    }
                    EntityType et = EntityType.fromName(petType);
                    if (et != null) {
                        if (!et.isAlive()) {
                            plugin.message(p, ChatColor.RED + "Invalid pet type.");
                            return true;
                        }
                        if (!plugin.hasPerm(p, "petcreeper.spawn." + et.getName()) && !plugin.hasPerm(p, "petcreeper.spawn.All")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission to spawn a " + et.getName() + ".");
                            return true;
                        }
                        for (int x = 1; x <= spawnCount; x++) {
                            if (plugin.isPetOwner(p)) {              
                                if (plugin.getPetsOf(p).size() >= PetConfig.maxPetsPerPlayer) {
                                    p.sendMessage(ChatColor.RED + "You have too many pets!");
                                    return true;
                                }
                            }
                            Entity e = p.getWorld().spawnEntity(p.getLocation(), et);
                            plugin.tamePetOf(p, e, true);
                        }
                    } else {
                        plugin.message(p, ChatColor.RED + "Invalid pet type.");
                    }
                } else {
                    plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "spawn [pet type] ([count])");
                }
            }
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "age")) {
                if (!plugin.hasPerm(p, "petcreeper.age")) {
                    plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (plugin.isPetOwner(p)) {
                    if (args.length == 2 && args[0].matches("\\d+")) {
                        int idx = Integer.parseInt(args[0]) - 1;
                        if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                            String s = args[1].toLowerCase().substring(0, 1);
                            Pet pet = plugin.getPetsOf(p).get(idx);
                            if (s.startsWith("b")) {
                                plugin.setPetAsBaby(pet);
                                plugin.message(p, ChatColor.GREEN + "Your pet is now a " + ChatColor.YELLOW + "baby" + ChatColor.GREEN + "!");
                            } else if (s.startsWith("a")) {
                                plugin.setPetAsAdult(pet);
                                plugin.message(p, ChatColor.GREEN + "Your pet is now an " + ChatColor.YELLOW + "adult" + ChatColor.GREEN + "!");
                            } else if (s.startsWith("l")) {
                                plugin.lockPetAge(pet);
                                plugin.message(p, ChatColor.GREEN + "Your pet's age is now " + ChatColor.YELLOW + "locked" + ChatColor.GREEN + "!");
                            } else if (s.startsWith("u")) {
                                plugin.unlockPetAge(pet);
                                plugin.message(p, ChatColor.GREEN + "Your pet's is now " + ChatColor.YELLOW + "unlocked" + ChatColor.GREEN + "!");
                            } else {
                                plugin.message(p, ChatColor.RED + "Invalid pet age.");
                            }
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "age [id] [baby|adult]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "reload")) {
                plugin.loadConfig();
            }
        }
        return true;
    }
}
