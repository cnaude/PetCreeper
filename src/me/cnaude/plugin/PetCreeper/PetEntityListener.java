package me.cnaude.plugin.PetCreeper;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftArrow;
import org.bukkit.craftbukkit.entity.CraftEnderCrystal;
import org.bukkit.craftbukkit.entity.CraftFireball;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class PetEntityListener implements Listener {

    private final PetMain plugin;

    public PetEntityListener(PetMain instance) {
        this.plugin = instance;
    }

    /*
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity e = event.getEntity();
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(e)) {
                Player p = this.plugin.getMasterOf(e);
                if (((Creature)e).getTarget() instanceof Player) {
                    Player target = (Player)((Creature)e).getTarget();
                    if (p == target || plugin.getModeOfPet(e,p) == Pet.modes.PASSIVE) {
                        if(p.getWorld() == c.getWorld()) {
                            if ((!this.plugin.isPetFollowing(e)) || (c.getPassenger() != null) || (c.getLocation().distance(p.getLocation()) < PetConfig.idleDistance)) {                                                        
                                //event.setCancelled(true);                        
                                //c.setTarget(null);
                            }
                        }
                    }
                }
            }
        }
    }
    */
    /* Does this event fire as well?
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTargetLivingEntityEvent (EntityTargetLivingEntityEvent  event) {
        Entity e = event.getEntity();
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(e)) {
                Player p = this.plugin.getMasterOf(e);
                if (c.getTarget() instanceof Player) {
                    Player target = (Player)((Creature)e).getTarget();
                    if (p == target || PetConfig.friendly) {
                        if(p.getWorld() == c.getWorld()) {
                            if ((!this.plugin.isPetFollowing(e)) || (c.getPassenger() != null) || (c.getLocation().distance(p.getLocation()) < PetConfig.idleDistance)) {                                                        
                                //event.setCancelled(true);                        
                                c.setTarget(null);
                            }
                        }
                    } 
                }
            }
        }
    }
    */

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity e = event.getEntity();
        if ((e instanceof Creeper)) {            
            if (this.plugin.isPet(e)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void stopDragonDamage(EntityExplodeEvent event) {
        Entity e = event.getEntity();
        if (this.plugin.isPet(e)) {
            event.setCancelled(true);  
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {        
        Entity e = event.getEntity().getShooter();                    
        if (this.plugin.isPet(e)) {
            event.getEntity().remove();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombustEvent(EntityCombustEvent event) {
        Entity e = event.getEntity();
        if (event.getEntity() instanceof Creature) {
            if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
                if (this.plugin.isPet(e)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTeleportEvent(EntityTeleportEvent event) {
        Entity e = event.getEntity();
        if (event.getEntityType().isAlive()) {
            if (this.plugin.isPet(e)) {
                if (e.getType() == EntityType.ENDERMAN) {
                    Creature c = (Creature) e;
                    if (c.getPassenger() instanceof Entity) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity e = event.getEntity();
        Entity d = event.getDamager();        

        if ((e instanceof Wolf) || (e instanceof Ocelot) || (e instanceof CraftEnderCrystal)) {
            return;
        }
        
        if (e instanceof Player) {
            Player p = (Player) e;
            if (d instanceof LivingEntity) {
                if (this.plugin.isPet(d)) {
                    if ((this.plugin.getMasterOf(d) == p)
                            || this.plugin.getPet(d).mode == Pet.modes.PASSIVE) {
                        if (d instanceof Monster) {
                            ((Monster)d).setTarget(null);
                        }
                        event.setCancelled(true);

                    }
                } else {
                    if (plugin.isPetOwner((Player)e)) {
                        for (Pet pet : this.plugin.getPetsOf((Player)(e))) {
                            if (pet.mode != Pet.modes.PASSIVE) {
                                Entity pe = this.plugin.getEntityOfPet(pet);
                                if (pe instanceof Monster) {
                                    ((Monster)pe).setTarget((LivingEntity)d);
                                }                               
                            }
                        }                    
                    }
                }
            } else if (d instanceof Arrow) {
                Arrow ar = (Arrow)d;
                Entity shooter = ar.getShooter();
                if (this.plugin.isPet(shooter)) {
                    if ((this.plugin.getMasterOf(shooter) == p)
                            || this.plugin.getPet(shooter).mode == Pet.modes.PASSIVE
                            || this.plugin.getPet(shooter).mode == Pet.modes.DEFENSIVE) {
                        if (shooter instanceof Monster) {
                            ((Monster)shooter).setTarget(null);
                        }
                        ar.remove();
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            if (this.plugin.isPet(e)) {
                if (!PetConfig.provokable && this.plugin.getPet(e).mode == Pet.modes.PASSIVE) {
                    event.setCancelled(true);
                    return;
                }
                if (d instanceof Player) { 
                    Player p = (Player)d;
                    if (this.plugin.getMasterOf(e) == p) {
                        p.sendMessage(ChatColor.RED + "You made your " + this.plugin.getNameOfPet(e) + " angry!");
                        this.plugin.untamePetOf(p,e, true);
                    }
                }
            } else if (PetConfig.attackTame) {
                if ((d != null) && ((d instanceof Player))) {
                    Player p = (Player) d;                                        
                    if (this.plugin.tamePetOf(p, e, false)) {
                        event.setCancelled(true);                            
                    }                    
                }
            }
        }
    }
    
    
    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        Projectile p = event.getEntity();        
        Entity e = event.getEntity();
        if (e instanceof CraftFireball || e instanceof CraftArrow) {        
            Entity sh = (Entity) p.getShooter();
            if (this.plugin.isPet(sh)) {
                Entity target = ((Creature)sh).getTarget();
                if (target instanceof Player) {
                    Pet pet = this.plugin.getPet(sh);
                    if ((Player)target == this.plugin.getMasterOf(sh) || pet.mode == Pet.modes.PASSIVE) {                        
                        event.setCancelled(true);
                        p.remove();
                    }
                }
            }
        } else if (this.plugin.isPet(e)) {            
            event.setCancelled(true);
            p.remove();
        }
    }
    

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        if (this.plugin.isPet(e)) {
            Player p = this.plugin.getMasterOf(e);            
            p.sendMessage(ChatColor.RED + "Your pet " + ChatColor.YELLOW + this.plugin.getNameOfPet(e) + ChatColor.RED + " has died!");
            this.plugin.untamePetOf(p,e,false);
        }
    }
}
