package me.cnaude.plugin.PetCreeper;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PetPlayerListener implements Listener {

    private final PetMain plugin;

    private void disconnect(Player p) {
        if (this.plugin.isPetOwner(p)) {
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
        }
    }

    private void teleport(Player p) {
        if (!this.plugin.isFollowed(p)) {
            return;
        }
        Creature c = this.plugin.getPetOf(p);
        if (c != null) {
            Location pos = p.getLocation().clone();
            pos.setY(pos.getY() + 1.0D);
            c.teleport(pos);
        }
    }

    public PetPlayerListener(PetMain instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        for (int i = 0; i < this.plugin.petList.size(); i++) {
            Pet pet = (Pet) this.plugin.petList.get(i);
            if (!pet.player.equals(p.getName())) {
                continue;
            }
            this.plugin.petList.remove(i);
            Creature c = this.plugin.spawnPetOf(p, pet.type);
            c.setHealth(pet.hp);
            if (pet.type == EntityType.SHEEP) {
                Sheep s = (Sheep) c;
                if (pet.sheared) {
                    s.setSheared(true);
                }
                s.setColor(DyeColor.getByData(pet.color));
            } else if (pet.type == EntityType.PIG) {
                Pig pig = (Pig) c;
                if (pet.saddled) {
                    pig.setSaddle(true);
                }
            }
            p.sendMessage(ChatColor.GREEN + "Your pet " + this.plugin.getPetNameOf(p) + " greets you.");
            break;
        }
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity e = event.getRightClicked();
        Player p = event.getPlayer();

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
                        if (!this.plugin.isPermitted(p, "ride. " + c.getType().getName())) {
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
                if (((c instanceof Wolf)) || ((c instanceof Skeleton)) || ((c instanceof Ghast)) || ((c instanceof Slime))) {
                    return;
                }
                ItemStack bait = p.getItemInHand();
                int amt = bait.getAmount();
                if ((bait.getType() == PetConfig.getBait(c)) && (amt > 0)) {
                    if (this.plugin.isPetOwner(p)) {
                        p.sendMessage("You already have a pet!");
                        return;
                    }

                    if (!this.plugin.isPermitted(p, "tame. " + c.getType().getName())) {
                        p.sendMessage(ChatColor.RED + "You don't have permission to tame that creature.");
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
}

/* Location:           C:\Users\naudec.BWI\Downloads\PetCreeper\PetCreeper.jar
 * Qualified Name:     mathew.petcreeper.PetPlayerListener
 * JD-Core Version:    0.6.0
 */