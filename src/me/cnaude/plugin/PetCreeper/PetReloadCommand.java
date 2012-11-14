/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class PetReloadCommand implements CommandExecutor {

    private final PetMain plugin;

    public PetReloadCommand(PetMain instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (commandLabel.equalsIgnoreCase(PetConfig.commandPrefix + "reload")) {
                if (p.hasPermission("petcreeper.reload")) {
                    plugin.loadConfig();
                    plugin.message(p, "PetCreeper configuration reloaded.");
                } else {
                    plugin.message(p, "No permission to reload PetCreeper config!");
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            plugin.loadConfig();
        }
        return true;
    }
}
