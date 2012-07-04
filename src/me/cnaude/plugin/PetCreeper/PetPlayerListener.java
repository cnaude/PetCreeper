package me.cnaude.plugin.PetCreeper;

import net.minecraft.server.Navigation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PetPlayerListener implements Listener {

    private final PetMain plugin;

    private void disconnect(Player p) {
        if (this.plugin.isPetOwner(p)) {
            if (this.plugin.getPetOf(p) instanceof Creature) {
                Creature c = this.plugin.getPetOf(p);
                if ((c instanceof Sheep)) {
                    Sheep s = (Sheep) c;
                    this.plugin.petList.add(new Pet(p.getName(), s.getHealth(), s.isSheared(), s.getColor().getData()));
                } else if ((c instanceof Pig)) {
                    Pig pig = (Pig) c;
                    this.plugin.petList.add(new Pet(p.getName(), pig.getHealth(), pig.hasSaddle()));
                } else {
                    this.plugin.petList.add(new Pet(p.getName(), this.plugin.getPetTypeOf(p), c.getHealth()));
                }
                this.plugin.untamePetOf(p);
                c.remove();
            } else if (this.plugin.getSlimePetOf(p) instanceof Slime) {
                Slime c = this.plugin.getSlimePetOf(p);
                this.plugin.petList.add(new Pet(p.getName(), this.plugin.getPetTypeOf(p), c.getHealth()));
                this.plugin.untamePetOf(p);
                c.remove();
            }
        }
    }

    private void teleport(Player p) {
        if (this.plugin.isFollowed(p)) {
            Creature c = this.plugin.getPetOf(p);
            if (c != null) {
                if (c.getWorld().equals(p.getWorld())) {
                    Location pos = p.getLocation().clone();
                    pos.setY(pos.getY() + 1.0D);
                    c.teleport(pos);
                } else {
                    disconnect(p);
                    this.plugin.petSpawn(p);
                }
            }
        }
    }

    public PetPlayerListener(PetMain instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.petSpawn(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        disconnect(p);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        Player p = event.getPlayer();
        disconnect(p);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause().equals(DamageCause.SUFFOCATION)) {
            if (event.getEntity().isInsideVehicle()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();
        if ((e instanceof Wolf) || (e instanceof Ocelot)) {
            return;
        }
        if ((e instanceof Creature)) {
            Creature c = (Creature) e;
            if (this.plugin.isPet(c)) {
                String petType = this.plugin.getPetNameOf(p);
                Player master = this.plugin.getMasterOf(c);
                if (master == p) {
                    Entity passenger = c.getPassenger();
                    if ((!(c instanceof Pig)) && (passenger == p)) {
                        c.eject();
                    } else if ((PetConfig.ridable) && (p.getItemInHand().getType() == Material.SADDLE) && (passenger == null)) {
                        if ((c instanceof Pig)) {
                            return;
                        }
                        c.setPassenger(p);
                    } else {
                        if (!this.plugin.hasPerm(p, "petcreeper.ride. " + c.getType().getName())
                                && !this.plugin.hasPerm(p, "petcreeper.ride.All")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission to ride that creature.");
                            return;
                        }
                        if (this.plugin.isFollowed(p)) {
                            p.sendMessage(ChatColor.GOLD + "Your " + petType + " is now not following you.");
                            this.plugin.setFollowed(p, false);
                        } else {
                            p.sendMessage(ChatColor.GOLD + "Your " + petType + " is now following you.");
                            this.plugin.setFollowed(p, true);
                        }
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "That " + this.plugin.getPetNameOf(master) + " belongs to " + master.getDisplayName() + ".");
                }

            } else {
                ItemStack bait = p.getItemInHand();
                int amt = bait.getAmount();
                if ((bait.getType() == PetConfig.getBait(c)) && (amt > 0)) {
                    if (this.plugin.isPetOwner(p)) {
                        p.sendMessage("You already have a pet!");
                        return;
                    }

                    if (!this.plugin.hasPerm(p, "petcreeper.tame." + c.getType().getName()) && !this.plugin.hasPerm(p, "petcreeper.tame.All")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + c.getType().getName() + ".");
                        return;
                    }

                    if (amt == 1) {
                        p.getInventory().removeItem(new ItemStack[]{bait});
                    } else {
                        bait.setAmount(amt - 1);
                    }
                    this.plugin.tamePetOf(p, c);

                    p.sendMessage(ChatColor.GREEN + "You tamed the " + this.plugin.getPetNameOf(p) + "!");
                }
            }
        } else if ((e instanceof Slime)) {
            Slime c = (Slime) e;
            if (this.plugin.isPet(c)) {
                String petType = this.plugin.getPetNameOf(p);
                Player master = this.plugin.getMasterOf(c);
                if (master == p) {
                    Entity passenger = c.getPassenger();
                    if ((!(c instanceof Pig)) && (passenger == p)) {
                        c.eject();
                    } else if ((PetConfig.ridable) && (p.getItemInHand().getType() == Material.SADDLE) && (passenger == null)) {
                        c.setPassenger(p);
                    } else {
                        if (!this.plugin.hasPerm(p, "petcreeper.ride. " + c.getType().getName())
                                && !this.plugin.hasPerm(p, "petcreeper.ride.All")) {
                            p.sendMessage(ChatColor.RED + "You don't have permission to ride that creature.");
                            return;
                        }
                        if (this.plugin.isFollowed(p)) {
                            p.sendMessage(ChatColor.GOLD + "Your " + petType + " is now not following you.");
                            this.plugin.setFollowed(p, false);
                        } else {
                            p.sendMessage(ChatColor.GOLD + "Your " + petType + " is now following you.");
                            this.plugin.setFollowed(p, true);
                        }
                    }
                } else {
                    p.sendMessage(ChatColor.GOLD + "That " + this.plugin.getPetNameOf(master) + " belongs to " + master.getDisplayName() + ".");
                }

            } else {
                ItemStack bait = p.getItemInHand();
                int amt = bait.getAmount();
                if ((bait.getType() == PetConfig.getBait(c)) && (amt > 0)) {
                    if (this.plugin.isPetOwner(p)) {
                        p.sendMessage("You already have a pet!");
                        return;
                    }

                    if (!this.plugin.hasPerm(p, "petcreeper.tame." + c.getType().getName()) && !this.plugin.hasPerm(p, "petcreeper.tame.All")) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to tame a " + c.getType().getName() + ".");
                        return;
                    }

                    if (amt == 1) {
                        p.getInventory().removeItem(new ItemStack[]{bait});
                    } else {
                        bait.setAmount(amt - 1);
                    }
                    this.plugin.tamePetOf(p, c);

                    p.sendMessage(ChatColor.GREEN + "You tamed the " + this.plugin.getPetNameOf(p) + "!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        teleport(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        teleport(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!this.plugin.hasPerm(p, "petcreeper.control")) {
            return;
        }
        Action action = event.getAction();
//        ItemStack item = p.getItemInHand();
        if (action == Action.LEFT_CLICK_AIR) {
            Block targetBlock = p.getTargetBlock(null, 100);
            Location blockLoc = targetBlock.getLocation();
            if (p.isInsideVehicle()) {
                Entity e = p.getVehicle();
                if (e.getType().isAlive()) {
                    //System.out.println("Vehicle is a pet! Target: " + blockLoc.toString());
                    Navigation n = ((CraftLivingEntity) e).getHandle().al();
                    n.a(blockLoc.getX(), blockLoc.getY(), blockLoc.getZ(), 0.25f);
                }
            }

        }
    }
}
