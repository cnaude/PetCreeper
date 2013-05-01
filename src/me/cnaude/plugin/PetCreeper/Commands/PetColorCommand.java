/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.PetCreeper.Commands;

import me.cnaude.plugin.PetCreeper.Pet;
import me.cnaude.plugin.PetCreeper.PetConfig;
import me.cnaude.plugin.PetCreeper.PetMain;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;

/**
 *
 * @author cnaude
 */
public class PetColorCommand implements CommandExecutor {

    private final PetMain plugin;

    public PetColorCommand(PetMain instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!plugin.hasPerm(p, "petcreeper.color")) {
                plugin.message(p, ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }
            if (plugin.isPetOwner(p)) {
                if (args.length > 1 && args[0].matches("\\d+")) {
                    int idx = Integer.parseInt(args[0]) - 1;
                    if (idx >= 0 && idx < plugin.getPetsOf(p).size()) {                        
                        Pet pet = plugin.getPetsOf(p).get(idx);
                        String color = args[1].toUpperCase();
                        if (!color.isEmpty()) {
                            //getHandle().setCollarColor(color.getData());
                            DyeColor dc;
                            try {
                                dc = DyeColor.valueOf(color);
                            }
                            catch (IllegalArgumentException ex) {
                                plugin.message(p, ChatColor.RED + "Invalid color.");
                                return true;
                            }                            
                            if (dc != null) {
                                if (pet.type.equals(EntityType.WOLF)) {
                                    pet.color = color;
                                    ((Wolf)plugin.getEntityOfPet(pet)).setCollarColor(dc);
                                    plugin.message(p, ChatColor.GREEN + "You changed your wolf's collar to " + ChatColor.YELLOW + pet.color + ChatColor.GREEN + "!");                                    
                                } else if (pet.type.equals(EntityType.SHEEP)) {
                                    pet.color = color;
                                    ((Sheep)plugin.getEntityOfPet(pet)).setColor(dc);   
                                    plugin.message(p, ChatColor.GREEN + "You dyed your sheep " + ChatColor.YELLOW + pet.color + ChatColor.GREEN + "!");                                    
                                }
                            }                            
                        } else {
                            plugin.message(p, ChatColor.RED + "Invalid pet name.");
                        }
                    } else {
                        plugin.message(p, ChatColor.RED + "Invalid pet ID.");
                    }
                } else {
                    plugin.message(p, ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + "/" + PetConfig.commandPrefix + "color [id] [color]");
                }
            } else {
                plugin.message(p, ChatColor.RED + "You have no pets. :(");
            }
        }
        return true;
    }
}
