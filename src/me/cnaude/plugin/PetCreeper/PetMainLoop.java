package me.cnaude.plugin.PetCreeper;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.server.Navigation;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
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
            for (Map.Entry<Entity, Player> entry : plugin.petList.entrySet()) {
                Entity pet = entry.getKey();                
                Player p = entry.getValue();

                if (p.getWorld().equals(pet.getWorld())) {                                        
                    if (pet instanceof Creature) {
                        ((Creature) pet).setTarget(p);
                    }
                    Navigation n = ((CraftLivingEntity) pet).getHandle().al();
                    n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);                    
                } else {
                    plugin.teleportPetOf(p);
                }    
            }
        }
    }

    public void end() {
        timer.cancel();
    }
}
