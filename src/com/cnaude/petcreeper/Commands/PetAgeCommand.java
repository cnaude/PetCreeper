/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.petcreeper.Commands;

import com.cnaude.petcreeper.Pet;
import com.cnaude.petcreeper.PetConfig;
import com.cnaude.petcreeper.PetCreeper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class PetAgeCommand implements CommandExecutor {

    private final PetCreeper plugin;

    public PetAgeCommand(PetCreeper instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
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
        return true;
    }
}
