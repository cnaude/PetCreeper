package me.cnaude.plugin.PetCreeper;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.server.Navigation;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
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
                Entity e = entry.getKey();                
                Player p = entry.getValue();
                if (plugin.petFollowList.get(e)) {                    
                    if (p.getWorld() == e.getWorld()) {                                        
                        if (e instanceof Creature) {
                            ((Creature) e).setTarget(p);
                        }
                        Navigation n = ((CraftLivingEntity) e).getHandle().al();
                        n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);                    
                    } else {    
                        Pet pet = plugin.getPet(e,p);
                        plugin.teleportPet(pet, true);                        
                        //plugin.despawnPet(pet);
                        //plugin.spawnPet(pet,p, true);
                    }
                }
            }
        }
    }

    public void end() {
        timer.cancel();
    }
}
