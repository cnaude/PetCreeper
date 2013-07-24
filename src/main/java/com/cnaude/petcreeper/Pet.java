package com.cnaude.petcreeper;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.libs.com.google.gson.annotations.Expose;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.material.MaterialData;

public class Pet {
    
    @Expose public EntityType type = EntityType.UNKNOWN;
    @Expose public Profession prof = Profession.FARMER;
    @Expose public String catType = "BLACK_CAT";
    @Expose public int entityId = -1;
    @Expose public double hp = 0;
    @Expose public int size = 0;
    @Expose public boolean sheared = false;
    @Expose public String color = "";
    @Expose public boolean saddled = false;
    @Expose public String petName = "";
    @Expose public boolean followed = true;
    @Expose public int age = 0;
    @Expose boolean powered = false;
    @Expose MaterialData carriedMat = new MaterialData(0);
    @Expose public modes mode = modes.PASSIVE;
    @Expose public int level = 1;
    @Expose public int exp = 0;
    @Expose public int skelType = 0;
    @Expose public boolean ageLocked = false;
    @Expose public boolean zombieVillager = false;
    @Expose public double x, y, z;
    @Expose public String world;
    @Expose public boolean sitting = false;
    @Expose public boolean baby = false;

    public enum modes {
        PASSIVE,
        DEFENSIVE,
        AGGRESSIVE,
    }

    public Pet(Entity e) {
        this.initPet(e);
    }
    
    public Pet() {        
    }

    public void initEntity(Entity e, Player p) {
        if (this.type == EntityType.SHEEP) {
            ((Sheep) e).setSheared(this.sheared);
            ((Sheep) e).setColor(DyeColor.valueOf(this.color));
        }
        if (this.type == EntityType.PIG) {
            ((Pig) e).setSaddle(this.saddled);
        }
        if (this.type == EntityType.SLIME) {
            ((Slime) e).setSize(this.size);
        }
        if (this.type == EntityType.MAGMA_CUBE) {
            ((MagmaCube) e).setSize(this.size);
        }
        if (this.type == EntityType.ENDERMAN) {
            ((Enderman) e).setCarriedMaterial(this.carriedMat);
        }
        if (this.type == EntityType.VILLAGER) {
            ((Villager) e).setProfession(this.prof);
        }
        if (this.type == EntityType.SKELETON) {
            ((Skeleton) e).setSkeletonType(SkeletonType.getType(this.skelType));
        }
        if (this.type == EntityType.ZOMBIE) {
            ((Zombie) e).setVillager(this.zombieVillager);
            ((Zombie) e).setBaby(baby);
        }
        if (e instanceof Ageable) {
            ((Ageable) e).setAge(this.age);
            ((Ageable) e).setAgeLock(this.ageLocked);
        }
        if (e instanceof LivingEntity) {
            ((LivingEntity) e).setHealth(this.hp);
        } else if (e instanceof Ambient) {
            ((Ambient) e).setHealth(this.hp);
        }
        if (e instanceof Wolf && this.mode == Pet.modes.AGGRESSIVE) {
            ((Wolf) e).setOwner(null);
            ((Wolf) e).setAngry(true);
        } else if (e instanceof Tameable) {
            ((Tameable) e).setOwner(p);
        }
        if (e instanceof Wolf) {
            if (this.followed || !this.sitting) {
                ((Wolf) e).setSitting(false);
            } else {
                ((Wolf) e).setSitting(true);
            }
            if (!color.isEmpty()) {
                ((Wolf) e).setCollarColor(DyeColor.valueOf(this.color));
            }
        }
        if (e instanceof Ocelot) {
            ((Ocelot) e).setCatType(Ocelot.Type.valueOf(this.catType));
            if (this.followed || !this.sitting) {
                ((Ocelot) e).setSitting(false);
            } else {
                ((Ocelot) e).setSitting(true);
            }
        }
        if (PetCreeper.get().config.customNamePlates) {
            ((LivingEntity) e).setCustomName(ChatColor.translateAlternateColorCodes('&', petName));
            ((LivingEntity) e).setCustomNameVisible(true);
        }
    }

    public final void initPet(Entity e) {
        EntityType et = e.getType();
        double health = ((LivingEntity) e).getHealth();
        if (et == EntityType.CREEPER) {
            this.powered = ((Creeper) e).isPowered();
        } else if (et == EntityType.SHEEP) {
            Sheep s = (Sheep) e;
            this.sheared = s.isSheared();
            this.color = s.getColor().toString();
        } else if (et == EntityType.PIG) {
            Pig pig = (Pig) e;
            this.saddled = pig.hasSaddle();
        } else if (et == EntityType.SLIME) {
            Slime slime = (Slime) e;
            this.size = slime.getSize();
        } else if (et == EntityType.MAGMA_CUBE) {
            MagmaCube magmacube = (MagmaCube) e;
            this.size = magmacube.getSize();
        } else if (et == EntityType.VILLAGER) {
            Villager villager = (Villager) e;
            this.prof = villager.getProfession();
        } else if (et == EntityType.ENDERMAN) {
            Enderman enderman = (Enderman) e;
            this.carriedMat = enderman.getCarriedMaterial();
        } else if (et == EntityType.SKELETON) {
            this.skelType = ((Skeleton) e).getSkeletonType().getId();
        } else if (et == EntityType.ZOMBIE) {
            this.zombieVillager = ((Zombie) e).isVillager();
            this.baby = ((Zombie) e).isBaby();
        } else if (et == EntityType.OCELOT) {
            this.catType = ((Ocelot) e).getCatType().name();
            this.sitting = ((Ocelot) e).isSitting();
            if (this.sitting) {
                this.followed = false;
            } else {
                this.followed = true;
            }
        } else if (et == EntityType.WOLF) {
            this.color = ((Wolf) e).getCollarColor().name();
            this.sitting = ((Wolf) e).isSitting();
            if (this.sitting) {
                this.followed = false;
            } else {
                this.followed = true;
            }
        } 
        if (e instanceof Ageable) {
            this.age = ((Ageable) e).getAge();
            this.ageLocked = ((Ageable) e).getAgeLock();
        }
        this.type = et;
        this.hp = health;
        this.entityId = e.getEntityId();
        this.petName = et.getName();
        if (this.skelType == 1 && !PetCreeper.get().config.randomizePetNames) {
            this.petName = "Wither" + this.petName;
        }
            if (PetCreeper.get().config.randomizePetNames) {
                this.petName = PetCreeper.get().getRandomName();
            }
            this.petName = PetCreeper.get().colorizePetname(petName);
        
        if (PetCreeper.get().config.customNamePlates) {
            ((LivingEntity) e).setCustomName(petName);
            ((LivingEntity) e).setCustomNameVisible(true);
        }
        this.x = e.getLocation().getX();
        this.y = e.getLocation().getY();
        this.z = e.getLocation().getZ();
        this.world = e.getLocation().getWorld().getName();
    }
}
