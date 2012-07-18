package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.server.Navigation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class PetMainLoop {

    Timer timer;
    private final PetMain plugin;

    public PetMainLoop(PetMain instance) {
        plugin = instance;
        timer = new Timer();
        timer.schedule(new petTask(), 0, 1000L);
        System.out.println("PetCreeper main loop running.");
    }

    class petTask extends TimerTask {

        @Override
        public void run() {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.isPetOwner(p)) {
                    for (Pet pet : (ArrayList<Pet>)plugin.getPetsOf(p).clone()) {                                                                
                            Entity e = plugin.getEntityOfPet(pet);
                            if (e != null) {
                                if (p.getWorld() == e.getWorld()) {                       
                                    if (p.getLocation().distance(e.getLocation()) > PetConfig.idleDistance
                                            && pet.followed) {   
                                        plugin.walkToPlayer(e,p);                                    
                                    } else if (e instanceof Monster) {
                                        plugin.attackNearbyEntities(e,pet.mode);                                    
                                    } 
                                } else if (pet.followed) {                                                            
                                    plugin.teleportPet(pet, true);                        
                                }
                            }
                        } 
                    }                    
                }
            }
        }    

    public void end() {
        timer.cancel();
    }
}
