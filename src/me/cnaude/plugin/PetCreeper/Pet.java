package me.cnaude.plugin.PetCreeper;

import org.bukkit.entity.EntityType;

public class Pet {

    public String player;
    public EntityType type;
    public int hp;
    public boolean sheared;
    public byte color;
    public boolean saddled;

    public Pet(String player, EntityType type, int hp) {
        this.player = player;
        this.type = type;
        this.hp = hp;
    }

    public Pet(String player, int hp, boolean sheared, byte color) {
        this.player = player;
        this.type = EntityType.SHEEP;
        this.sheared = sheared;
        this.color = color;
    }

    public Pet(String player, int hp, boolean saddled) {
        this.player = player;
        this.type = EntityType.PIG;
        this.hp = hp;
        this.saddled = saddled;
    }
}
