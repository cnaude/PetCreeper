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
                if ((!this.plugin.isFollowed(p)) || (c.getPassenger() != null) || (c.getLocation().distance(p.getLocation()) < PetConfig.idleDistance)) {
                    event.setTarget(null);
                } else {
                    event.setTarget(p);
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
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        //System.out.println("Damaged: "+event.getEntityType().getName());
        Entity e = event.getEntity();
        if ((e instanceof Creature)) {
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

                    if ((c.getType().equals(EntityType.WOLF))
                            || (c.getType().equals(EntityType.SKELETON))
                            || (c.getType().equals(EntityType.GHAST))
                            || (c.getType().equals(EntityType.SLIME))) {
                        //return;
                    }

                    ItemStack bait = p.getItemInHand();
                    int amt = bait.getAmount();
                    if ((bait.getType().equals(PetConfig.getBait(c))) && (amt > 0)) {
                        if (!this.plugin.isPermitted(p, "tame. " + c.getType().getName())) {
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
                        event.setCancelled(true);
                    }
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

/* Location:           C:\Users\naudec.BWI\Downloads\PetCreeper\PetCreeper.jar
 * Qualified Name:     mathew.petcreeper.PetEntityListener
 * JD-Core Version:    0.6.0
 */