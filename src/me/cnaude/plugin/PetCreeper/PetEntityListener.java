package me.cnaude.plugin.PetCreeper;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

public class PetEntityListener implements Listener {

    private final PetMain plugin;

    public PetEntityListener(PetMain instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        Entity e = event.getEntity();
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(c)) {
                Player p = this.plugin.getMasterOf(c);
                if (p.getWorld().equals(c.getWorld())) {
                    if ((!this.plugin.isFollowed(p))
                            || (c.getPassenger() != null)
                            || (c.getLocation().distance(p.getLocation()) < PetConfig.idleDistance)) {
                        event.setTarget(null);
                    } else {
                        event.setTarget(p);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity e = event.getEntity();
        if ((e instanceof Creeper)) {
            Creeper c = (Creeper) e;
            if (this.plugin.isPet(c)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        //System.out.println("Shooting a bow: " + event.getEntityType().getName());
        Entity e = event.getEntity().getShooter();
        event.getEntity().remove();
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(c)) {
                //System.out.println("Cancelling bow"
                //        + ": " + event.getEntityType().getName());
                event.getEntity().remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombustEvent(EntityCombustEvent event) {
        if (event.getEntity() instanceof Creature) {
            if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
                Creature c = (Creature) event.getEntity();
                if (this.plugin.isPet(c)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityTeleportEvent(EntityTeleportEvent event) {
        if (event.getEntityType().isAlive()) {
            Creature c = (Creature) event.getEntity();
            if (this.plugin.isPet(c)) {
                if (c.getPassenger() instanceof Entity) {
                    //System.out.println("Cancelling teleport due to passenger!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity e = event.getEntity();
        Entity d = event.getDamager();
        if (e instanceof Creature) {
            Creature c = (Creature) e;

            if (this.plugin.isPet(c)) {
                if (!PetConfig.provokable) {
                    event.setCancelled(true);
                    return;
                }
                Entity attacker = event.getDamager();
                if ((attacker != null) && ((attacker instanceof Player))) {
                    Player p = (Player) attacker;
                    if (this.plugin.getMasterOf(c) == p) {
                        p.sendMessage(ChatColor.RED + "You made your " + this.plugin.getPetNameOf(p) + " angry!");
                        this.plugin.untamePetOf(p);

                        if ((c instanceof Monster)) {
                            c.setTarget(p);
                        } else {
                            c.setTarget(null);
                        }
                    }
                }
            } else if (PetConfig.attackTame) {
                Entity attacker = event.getDamager();
                if ((attacker != null) && ((attacker instanceof Player))) {
                    Player p = (Player) attacker;

                    if ((c instanceof Wolf) || (c instanceof Ocelot)) {
                        return;
                    }

                    ItemStack bait = p.getItemInHand();
                    int amt = bait.getAmount();
                    if ((bait.getType().equals(PetConfig.getBait(c))) && (amt > 0)) {
                        if (!this.plugin.hasPerm(p, "petcreeper.tame." + c.getType().getName()) 
                                && !this.plugin.hasPerm(p,"petcreeper.tame.All")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + c.getType().getName() + ".");
                            return;
                        }

                        if (this.plugin.isPetOwner(p)) {
                            p.sendMessage("You already have a pet!");
                            return;
                        }

                        if (amt == 1) {
                            p.getInventory().removeItem(new ItemStack[]{bait});
                        } else {
                            bait.setAmount(amt - 1);
                        }
                        this.plugin.tamePetOf(p, c);

                        p.sendMessage(ChatColor.GREEN + "You tamed the " + this.plugin.getPetNameOf(p) + "!");
                        c.setTarget(null);
                        event.setCancelled(true);
                    }
                }
            }
        } else if (e instanceof Ghast) {
            System.out.println("This is a ghast!");
        } else if ((e instanceof Player) && (d instanceof Creature)) {
            Creature c = (Creature) d;
            Player p = (Player) e;
            if (c != null && p != null) {
                if (this.plugin.getMasterOf(c) == p) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        Projectile p = event.getEntity();
        if (p.getShooter() instanceof Creature
                || p.getShooter() instanceof Flying) {
            if (p.getShooter().getType().equals(EntityType.SKELETON)
                    || p.getShooter().getType().equals(EntityType.BLAZE)
                    || p.getShooter().getType().equals(EntityType.GHAST)) {
                Creature c = (Creature) p.getShooter();
                if (this.plugin.isPet(c)) {
                    //System.out.println("Cancelling projectile from " + c.getType().getName());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(c)) {
                Player p = this.plugin.getMasterOf(c);
                p.sendMessage(ChatColor.RED + "Your " + this.plugin.getPetNameOf(p) + " has died!");
                this.plugin.untamePetOf(p);
            }
        }
    }
}
