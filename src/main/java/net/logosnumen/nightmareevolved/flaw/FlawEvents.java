package net.logosnumen.nightmareevolved.flaw;

import java.util.Locale;
import net.logosnumen.nightmareevolved.aspect.Bound;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** The Flaws that hang on things outside a fight: what is said, what one is called, what one promises, whom one loses. */
public final class FlawEvents {
    private static final String HIDDEN_TEAM = "ne_overlooked";

    private FlawEvents() {
    }

    private static boolean has(Player player, Flaw flaw) {
        return flaw.id().equals(Soul.of(player).flaw());
    }

    /**
     * Clear Conscience: a question put to someone (naming them, or asked within earshot) must be answered. Speaking at
     * all settles the debt.
     */
    public static void onChat(ServerChatEvent event) {
        ServerPlayer speaker = event.getPlayer();
        SoulData own = Soul.of(speaker);
        if (own.flawState().getLong("Owed") > 0) {
            own.flawState().putLong("Owed", 0);
            own.flawState().putInt("Strain", 0);
        }

        String text = event.getRawText().toLowerCase(Locale.ROOT);
        if (!text.contains("?")) {
            return;
        }

        for (ServerPlayer other : speaker.server.getPlayerList().getPlayers()) {
            boolean named = text.contains(other.getGameProfile().getName().toLowerCase(Locale.ROOT));
            boolean near = other.level() == speaker.level() && other.distanceToSqr(speaker) < 12.0 * 12.0;
            if (other != speaker && has(other, Flaw.CLEAR_CONSCIENCE) && (named || near)) {
                Soul.of(other).flawState().putLong("Owed", speaker.level().getGameTime());
                FlawKit.say(other, "clear_conscience_asked", speaker.getDisplayName());
            }
        }
    }

    /** Forgotten Name: nobody can keep hold of what he is called. */
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (has(event.getEntity(), Flaw.FORGOTTEN_NAME)) {
            event.setDisplayname(Component.translatable("flaw.nightmare_evolved.forgotten_name.shown"));
        }
    }

    public static void onTabName(PlayerEvent.TabListNameFormat event) {
        if (has(event.getEntity(), Flaw.FORGOTTEN_NAME)) {
            event.setDisplayName(Component.translatable("flaw.nightmare_evolved.forgotten_name.shown"));
        }
    }

    /** Overlooked: hides (or shows) a player's name above their head. */
    public static void hideName(ServerPlayer player, boolean hidden) {
        Scoreboard board = player.getScoreboard();
        PlayerTeam current = board.getPlayersTeam(player.getScoreboardName());
        if (hidden && current == null) {
            PlayerTeam team = board.getPlayerTeam(HIDDEN_TEAM);
            if (team == null) {
                team = board.addPlayerTeam(HIDDEN_TEAM);
                team.setNameTagVisibility(Team.Visibility.NEVER);
            }

            board.addPlayerToTeam(player.getScoreboardName(), team);
        } else if (!hidden && current != null && HIDDEN_TEAM.equals(current.getName())) {
            board.removePlayerFromTeam(player.getScoreboardName(), current);
        }
    }

    /** Binding Word: sneaking and touching a creature with an empty hand swears peace to it, for good. */
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()
            || !player.getMainHandItem().isEmpty() || !(event.getTarget() instanceof LivingEntity target) || !has(player, Flaw.BINDING_WORD)) {
            return;
        }

        SoulData soul = Soul.of(player);
        ListTag sworn = soul.flawState().getList("Sworn", Tag.TAG_STRING);
        String id = target.getUUID().toString();
        if (sworn.stream().noneMatch(t -> t.getAsString().equals(id))) {
            if (sworn.size() >= 64) {
                sworn.remove(0);
            }

            sworn.add(StringTag.valueOf(id));
            soul.flawState().put("Sworn", sworn);
            soul.changed();
            FlawKit.say(player, "binding_word_sworn", target.getDisplayName());
        }
    }

    static boolean sworn(SoulData soul, LivingEntity target) {
        String id = target.getUUID().toString();
        return soul.flawState().getList("Sworn", Tag.TAG_STRING).stream().anyMatch(t -> t.getAsString().equals(id));
    }

    /** Brittle Touch: tools wear out faster in her hands. */
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && has(player, Flaw.BRITTLE_TOUCH)) {
            ItemStack held = player.getMainHandItem();
            if (held.isDamageableItem()) {
                held.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
            }
        }
    }

    /** A Mother's Love: losing one who serves her wounds her too. */
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !Bound.bound(mob) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(Bound.owner(mob));
        if (owner != null && has(owner, Flaw.MOTHERS_LOVE)) {
            SoulData soul = Soul.of(owner);
            soul.setEssence(soul.essence() - soul.maxEssence() * 0.1);
            owner.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
            FlawKit.say(owner, "mothers_love");
        }
    }
}
