package me.cnaude.plugin.PetCreeper;

import org.bukkit.entity.EntityType;

public class Pet {

    public EntityType type;
    public int hp;
    public boolean sheared;
    public byte color;
    public boolean saddled;

    public Pet(EntityType type, int hp) {        
        this.type = type;
        this.hp = hp;
    }

    public Pet(int hp, boolean sheared, byte color) {        
        this.type = EntityType.SHEEP;
        this.sheared = sheared;
        this.color = color;
    }

    public Pet(int hp, boolean saddled) {        
        this.type = EntityType.PIG;
        this.hp = hp;
        this.saddled = saddled;
    }
}
