package me.cnaude.plugin.PetCreeper;

import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.server.Navigation;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
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
            for (Player p : plugin.getMasterList()) {
                Creature c = plugin.getPetOf(p);
                if (p.getWorld().equals(c.getWorld())) {
                    if ((!plugin.isFollowed(p))
                            || (c.getPassenger() != null)
                            || (c.getLocation().distance(p.getLocation()) < PetConfig.idleDistance)) {
                        c.setTarget(null);
                    } else {
                            c.setTarget((LivingEntity) p);
                 
                        
                        
                            Navigation n = ((CraftLivingEntity) c).getHandle().al();
                            n.a(p.getLocation().getX() + 2, p.getLocation().getY(), p.getLocation().getZ() + 2, 0.25f);
                        
                    }
                }
            }
        }
    }

    public void end() {
        timer.cancel();
    }
}



/* Location:           C:\Users\naudec.BWI\Downloads\PetCreeper\PetCreeper.jar
 * Qualified Name:     mathew.petcreeper.PetMainLoop
 * JD-Core Version:    0.6.0
 */