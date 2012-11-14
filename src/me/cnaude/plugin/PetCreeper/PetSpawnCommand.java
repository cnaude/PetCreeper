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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class PetSpawnCommand implements CommandExecutor {

    private final PetMain plugin;

    public PetSpawnCommand(PetMain instance) {
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
            if (spawnCount > PetConfig.maxSpawnCount) {
                spawnCount = PetConfig.maxSpawnCount;
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
        return true;
    }
}
