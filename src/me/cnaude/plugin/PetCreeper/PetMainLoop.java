package me.cnaude.plugin.PetCreeper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class PetMainLoop {

    Timer timer;
    private final PetMain plugin;

    public PetMainLoop(PetMain instance) {
        plugin = instance;
        timer = new Timer();
        timer.schedule(new petTask(), 0, PetConfig.mainLoop);
        System.out.println("PetCreeper main loop running. [" + PetConfig.mainLoop + "]");
    }

    class petTask extends TimerTask {

        @Override
        public void run() {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.isPetOwner(p)) {
                    for (Pet pet : plugin.getPetsOf(p)) {
                        Entity e = plugin.getEntityOfPet(pet);
                        if (e != null) {
                            if (p.getWorld() == e.getWorld()) {
                                if (p.getLocation().distance(e.getLocation()) > PetConfig.idleDistance
                                        && pet.followed) {
                                    plugin.walkToPlayer(e, p);
                                } else if (e instanceof Monster) {
                                    plugin.attackNearbyEntities(e, p, pet.mode);
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
