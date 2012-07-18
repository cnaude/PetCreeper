package me.cnaude.plugin.PetCreeper;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.material.MaterialData;

public final class Pet {

    public EntityType type = EntityType.UNKNOWN;
    public Profession prof = Profession.FARMER;
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
            
    public enum modes {
        PASSIVE,
        DEFENSIVE,
        AGGRESSIVE,
    }

    public Pet(Entity e) {
        this.initPet(e);                    
    }
    
    public void initPet(Entity e) {
        EntityType et = e.getType();
        int health = ((LivingEntity) e).getHealth();
        if (et == EntityType.CREEPER) {
            World world = e.getWorld();
            Location loc = e.getLocation();
            this.powered = ((Creeper)e).isPowered();
            e.remove();
            Entity creeper = world.spawnCreature(loc, EntityType.CREEPER);
            this.entityId = creeper.getEntityId();
            ((Creeper)creeper).setPowered(powered);
        } else {
            if (et == EntityType.SHEEP) {
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
                Enderman enderman = (Enderman)e;
                carriedMat = enderman.getCarriedMaterial();
            }
        }
        if (e instanceof Ageable) {
            this.age = ((Ageable)e).getAge();
        }
        this.type = et;
        this.hp = health;
        this.entityId = e.getEntityId();
        this.petName = et.getName();        
    }
    
    public Pet() {
    }
}
