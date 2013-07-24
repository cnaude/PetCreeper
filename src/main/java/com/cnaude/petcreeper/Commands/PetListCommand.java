/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.petcreeper.Commands;

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
public class PetListCommand implements CommandExecutor {

    private final PetCreeper plugin;

    public PetListCommand(PetCreeper instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (commandLabel.equalsIgnoreCase(plugin.config.commandPrefix + "list")) {
                if (plugin.isPetOwner(p)) {
                    plugin.printPetListOf(p);
                } else {
                    plugin.message(p, ChatColor.RED + "You don't own a pet.");
                }                
            }
        }
        return true;
    }
}
