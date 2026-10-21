package net.logosnumen.nightmareevolved.memory;

import java.util.Locale;
import net.logosnumen.nightmareevolved.entity.colossus.ColossusEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

/**
 * The broad kind a creature belongs to, as far as what it leaves behind goes: what a Memory woven from it is called,
 * what shape it tends to take, and what power a Mirror of Truth can borrow from its reflection.
 */
public enum Family {
    UNDEAD,
    ARTHROPOD,
    BEAST,
    AQUATIC,
    FIRE,
    ENDER,
    SPIRIT,
    CONSTRUCT,
    HUMAN,
    STRANGE;

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static Family of(LivingEntity entity) {
        if (entity instanceof Vex || entity instanceof Phantom || entity instanceof Ghast) {
            return SPIRIT;
        } else if (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Strider) {
            return FIRE;
        } else if (entity instanceof EnderMan || entity instanceof Endermite || entity instanceof Shulker || entity instanceof EnderDragon) {
            return ENDER;
        } else if (entity instanceof AbstractGolem || entity instanceof ColossusEntity) {
            return CONSTRUCT;
        } else if (entity.getType().is(EntityTypeTags.UNDEAD)) {
            return UNDEAD;
        } else if (entity.getType().is(EntityTypeTags.ARTHROPOD)) {
            return ARTHROPOD;
        } else if (entity.getType().is(EntityTypeTags.AQUATIC)) {
            return AQUATIC;
        } else if (entity instanceof Player || entity instanceof AbstractVillager || entity instanceof AbstractIllager || entity instanceof Witch
            || entity instanceof AbstractPiglin) {
            return HUMAN;
        } else if (entity instanceof Animal) {
            return BEAST;
        }

        return STRANGE;
    }
}
