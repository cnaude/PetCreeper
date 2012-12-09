package me.cnaude.plugin.PetCreeper;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.entity.CraftSkeleton;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.entity.CraftZombie;
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
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Wolf;
import org.bukkit.material.MaterialData;

public final class Pet {

    public EntityType type = EntityType.UNKNOWN;
    public Profession prof = Profession.FARMER;
    public String catType = "BLACK_CAT";
    public int entityId = -1;
    public int hp = 0;
    public int size = 0;
    public boolean sheared = false;
    public String color = "";
    public boolean saddled = false;
    public String petName = "";
    public boolean followed = true;
    public int age = 0;
    boolean powered = false;
    MaterialData carriedMat = new MaterialData(0);
    public modes mode = modes.PASSIVE;
    public int level = 1;
    public int exp = 0;
    public int skelType = 0;
    public boolean ageLocked = false;
    public boolean zombieVillager = false;    

    public enum modes {

        PASSIVE,
        DEFENSIVE,
        AGGRESSIVE,}

    public Pet(Entity e) {
        this.initPet(e);
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
            ((CraftSkeleton) e).getHandle().setSkeletonType(this.skelType);
        }
        if (this.type == EntityType.ZOMBIE) {
            ((CraftZombie)e).getHandle().setVillager(this.zombieVillager);
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
            if (this.followed) {
                ((Wolf)e).setSitting(false);
            } else {
                ((Wolf)e).setSitting(true);
            }  
            if (!color.isEmpty()) {
                ((CraftWolf)e).getHandle().setCollarColor(DyeColor.valueOf(this.color).getData());
            }
        }
        if (e instanceof Ocelot) {
            ((Ocelot)e).setCatType(Ocelot.Type.valueOf(this.catType));
            if (this.followed) {
                ((Ocelot)e).setSitting(false);
            } else {
                ((Ocelot)e).setSitting(true);
            }
        }
    }

    public void initPet(Entity e) {
        EntityType et = e.getType();
        int health = ((LivingEntity) e).getHealth();
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
            this.skelType = ((CraftSkeleton) e).getHandle().getSkeletonType();
        } else if (et == EntityType.ZOMBIE) {
            this.zombieVillager = ((CraftZombie)e).getHandle().isVillager();
        } else if (et == EntityType.OCELOT) {
            this.catType = ((Ocelot)e).getCatType().name();
        } else if (et == EntityType.WOLF) {
            this.color = (DyeColor.getByData((byte) ((CraftWolf)e).getHandle().getCollarColor())).name();                      
        }
        if (e instanceof Ageable) {
            this.age = ((Ageable) e).getAge();
            this.ageLocked = ((Ageable) e).getAgeLock();
        }
        this.type = et;
        this.hp = health;
        this.entityId = e.getEntityId();
        this.petName = et.getName();
        if (this.skelType == 1 && !PetConfig.randomizePetNames) {
            this.petName = "Wither" + this.petName;
        }
        if (PetConfig.randomizePetNames) {
            this.petName = PetMain.get().getRandomName();
        }
    }

    public Pet() {
    }
}
