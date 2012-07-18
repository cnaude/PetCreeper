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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PetPlayerListener implements Listener {

    private final PetMain plugin;

    public PetPlayerListener(PetMain instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.spawnPetsOf(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.plugin.spawnPetsOf(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        this.plugin.despawnPetsOf(event.getEntity());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.despawnPetsOf(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        this.plugin.despawnPetsOf(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause().equals(DamageCause.SUFFOCATION)) {
            if (event.getEntity().isInsideVehicle()) {
                Entity vehicle = event.getEntity().getVehicle();
                if (this.plugin.isPet(vehicle)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();
        EntityType et = e.getType();
        if ((et == EntityType.WOLF) || (et == EntityType.OCELOT)) {
            return;
        }
        if (e instanceof Entity) {
            if (this.plugin.isPet(e)) {
                Player master = this.plugin.getMasterOf(e);
                if (master == p) {
                    Entity passenger = e.getPassenger();
                    if ((!(et == EntityType.PIG)) && (passenger == p)) {
                        e.eject();
                    } else if ((PetConfig.ridable) && (p.getItemInHand().getType() == Material.SADDLE) && (passenger == null)) {
                        if ((et == EntityType.PIG)) {
                            return;
                        }
                        if (this.plugin.hasPerm(p, "petcreeper.ride. " + et.getName())
                                || this.plugin.hasPerm(p, "petcreeper.ride.All")) {
                            e.setPassenger(p);
                        } else {
                            this.plugin.message(p, ChatColor.RED + "You don't have permission to ride that creature.");
                        }
                    } else {
                        if (this.plugin.isPetFollowing(e)) {
                            this.plugin.message(p, ChatColor.GOLD + "Your " + et + " is no longer following you.");
                            this.plugin.petFollowList.remove(e);
                            this.plugin.petFollowList.put(e, false);
                        } else {
                            this.plugin.message(p, ChatColor.GOLD + "Your " + et + " is now following you.");
                            this.plugin.petFollowList.remove(e);
                            this.plugin.petFollowList.put(e, true);
                        }
                    }
                } else {
                    this.plugin.message(p, ChatColor.GOLD + "That " + e.getType().getName() + " belongs to " + master.getDisplayName() + ".");
                }

            } else {
                this.plugin.tamePetOf(p, e, false);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        this.plugin.teleportPetsOf(event.getPlayer(), false);
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
