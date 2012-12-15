/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper.Commands;

import me.cnaude.plugin.PetCreeper.Pet;
import me.cnaude.plugin.PetCreeper.PetConfig;
import me.cnaude.plugin.PetCreeper.PetMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author cnaude
 */
public class PetSaddleCommand implements CommandExecutor {

    private final PetMain plugin;

    public PetSaddleCommand(PetMain instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!plugin.hasPerm(p, "petcreeper.saddle")) {
                plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            if (plugin.isPetOwner(p)) {
                if (args.length == 1 && args[0].matches("\\d+")) {
                    int idx = Integer.parseInt(args[0]) - 1;
                    if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {
                        Pet pet = plugin.getPetsOf(p).get(idx);
                        if (pet.type.equals(EntityType.PIG)) {
                            Entity e = plugin.getEntityOfPet(pet);
                            if (e != null) {
                                if (((Pig) e).hasSaddle()) {
                                    ((Pig)e).setSaddle(false);
                                    plugin.message(p, ChatColor.GREEN + "Your pet dropped the saddle!");
                                    e.getWorld().dropItemNaturally(e.getLocation(), new ItemStack(Material.SADDLE, 1));
                                    p.playSound(e.getLocation(), Sound.PIG_DEATH, 10, 1);
                                    
                                } else {
                                    plugin.message(p, ChatColor.GREEN + "Your pet looks at you strangely.");
                                }
                            }
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                        }
                    } else {
                        plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "saddle [id]");
                    }
                } else {
                    plugin.message(p, ChatColor.RED + "You have no pets. :(");
                }
            }            
        }
        return true;
    }
}