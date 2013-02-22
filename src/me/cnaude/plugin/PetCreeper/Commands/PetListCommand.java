/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper.Commands;

import me.cnaude.plugin.PetCreeper.PetConfig;
import me.cnaude.plugin.PetCreeper.PetMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

/**
 *
 * @author cnaude
 */
public class PetListCommand implements CommandExecutor {

    private final PetMain plugin;

    public PetListCommand(PetMain instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "list")) {
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
