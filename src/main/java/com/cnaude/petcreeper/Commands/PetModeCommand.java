/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.petcreeper.Commands;

import com.cnaude.petcreeper.Pet;
import com.cnaude.petcreeper.PetCreeper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

/**
 *
 * @author cnaude
 */
public class PetModeCommand implements CommandExecutor {

    private final PetCreeper plugin;

    public PetModeCommand(PetCreeper instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
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
                        Entity pe = plugin.getEntityOfPet(pet);
                        if (s.startsWith("a")) {
                            pet.mode = Pet.modes.AGGRESSIVE;
                            if (pe instanceof Wolf) {
                                ((Wolf) pe).setOwner(null);
                                ((Wolf) pe).setAngry(true);
                            }
                            plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                        } else if (s.startsWith("p")) {
                            pet.mode = Pet.modes.PASSIVE;
                            if (pe instanceof Wolf) {
                                ((Wolf) pe).setAngry(false);
                                ((Wolf) pe).setOwner(p);
                            }
                            plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                        } else if (s.startsWith("d")) {
                            pet.mode = Pet.modes.DEFENSIVE;
                            if (pe instanceof Wolf) {
                                ((Wolf) pe).setAngry(false);
                                ((Wolf) pe).setOwner(p);
                            }
                            plugin.message(p, ChatColor.GREEN + "You made your pet " + ChatColor.YELLOW + pet.mode + ChatColor.GREEN + "!");
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet mode.");
                        }
                    } else {
                        plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                    }
                } else {
                    plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + plugin.config.commandPrefix + "mode [id] [p|d|a]");
                }
            } else {
                plugin.message(p, ChatColor.RED + "You have no pets. :(");
            }
        }
        return true;
    }
}
