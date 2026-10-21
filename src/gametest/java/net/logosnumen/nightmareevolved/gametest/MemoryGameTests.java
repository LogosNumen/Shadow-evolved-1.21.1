package net.logosnumen.nightmareevolved.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.attribute.SoulAttribute;
import net.logosnumen.nightmareevolved.creature.CreatureClass;
import net.logosnumen.nightmareevolved.creature.CreatureRank;
import net.logosnumen.nightmareevolved.creature.Creatures;
import net.logosnumen.nightmareevolved.echo.EchoRecord;
import net.logosnumen.nightmareevolved.echo.EchoTrait;
import net.logosnumen.nightmareevolved.echo.Echoes;
import net.logosnumen.nightmareevolved.memory.Enchant;
import net.logosnumen.nightmareevolved.memory.Family;
import net.logosnumen.nightmareevolved.memory.Memories;
import net.logosnumen.nightmareevolved.memory.MemoryData;
import net.logosnumen.nightmareevolved.memory.MemoryDef;
import net.logosnumen.nightmareevolved.memory.MemoryItems;
import net.logosnumen.nightmareevolved.memory.MemoryRecord;
import net.logosnumen.nightmareevolved.memory.SoulMemories;
import net.logosnumen.nightmareevolved.memory.Worn;
import net.logosnumen.nightmareevolved.soul.Rank;
import net.logosnumen.nightmareevolved.soul.Soul;
import net.logosnumen.nightmareevolved.soul.SoulData;
import net.logosnumen.nightmareevolved.soulsea.SeaRegions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Memories and Echoes as the novel has them: a Memory's Rank and Tier come from its prey; summoning costs essence and
 * dismissing gives it back; resting Memories mend; old copies dissolve; Memories die with their owner, all but the
 * Shard Memories; seven Shards make the Oath Keys; Echoes are frozen copies that die for good, and Sunny can make
 * them Shadows. And everything the player sees has its art and its words.
 */
@GameTestHolder(NightmareEvolved.MOD_ID)
@PrefixGameTestTemplate(false)
public class MemoryGameTests {
    private static ServerPlayer soul(GameTestHelper helper, Rank rank) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ne_memory_test"));
        player.moveTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.0, 1.5)));
        Soul.setRank(player, rank);
        return player;
    }

    @GameTest(template = "empty")
    public static void rankAndTierComeFromThePrey(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        Zombie tyrant = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        Creatures.raise(tyrant, CreatureRank.AWAKENED, CreatureClass.TYRANT, false, -1L);
        MemoryData woven = Memories.weave(tyrant, player.getUUID(), player.getRandom());
        helper.assertTrue(woven.rank() == 1 && woven.tier() == 5, "An Awakened Tyrant leaves a Tier V Awakened Memory");
        Zombie devil = helper.spawn(EntityType.ZOMBIE, 3, 2, 3);
        Creatures.raise(devil, CreatureRank.FALLEN, CreatureClass.DEVIL, false, -1L);
        MemoryData fallen = Memories.weave(devil, player.getUUID(), player.getRandom());
        helper.assertTrue(fallen.rank() == 2 && fallen.tier() == 4, "A Fallen Devil leaves a Tier IV Ascended Memory");
        helper.assertTrue(fallen.enchants().size() == Memories.capacity(4), "A woven Memory fills its weave's anchors");
        helper.assertTrue(Memories.capacity(1) == 1 && Memories.capacity(7) == 4, "Higher Tiers hold more enchantments");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theNovelsMemoriesAreAsTheNovelHasThem(GameTestHelper helper) {
        helper.assertTrue(Memories.MIDNIGHT_SHARD.rank.ordinal() == 1 && Memories.MIDNIGHT_SHARD.tier == 3, "Midnight Shard is a Tier III Awakened weapon");
        helper.assertTrue(Memories.MIDNIGHT_SHARD.enchants.contains(Enchant.UNBROKEN), "Midnight Shard is [Unbroken]");
        helper.assertTrue(Memories.COVETOUS_COFFER.enchants.size() == 4, "The Covetous Coffer has its four enchantments");
        helper.assertTrue(Memories.SHARDS.size() == 7 && Memories.SHARDS.stream().allMatch(d -> d.shard), "There are seven Shard Memories");
        helper.assertTrue(Memories.WEAVERS_MASK.rank.ordinal() == 6 && Memories.WEAVERS_MASK.tier == 7, "The Weaver's Mask is Divine, Tier VII");
        for (MemoryDef def : Memories.all()) {
            helper.assertTrue(def.named || def.enchants.size() <= 1, def.id + ": a woven shape carries at most its one fixed enchantment");
            helper.assertTrue(!def.armor() || !def.layers.isEmpty(), def.id + ": armour has layers");
        }

        for (MemoryDef.Kind kind : MemoryDef.Kind.values()) {
            if (kind != MemoryDef.Kind.LINEAGE && kind != MemoryDef.Kind.TOOL) {
                helper.assertTrue(Enchant.pool(kind).size() >= 4, kind + ": the Spell has enchantments to roll");
            }
        }

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void summoningCostsEssenceDismissingGivesItBack(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.MASTER);
        SoulData soul = Soul.of(player);
        MemoryRecord record = SoulMemories.grant(player, Memories.MOONLIGHT_SHARD, false);
        helper.assertTrue(record != null, "A Master's soul takes a Memory");
        soul.setEssence(soul.maxEssence());
        double full = soul.essence();
        helper.assertTrue(SoulMemories.summon(player, record.id, -1), "The Memory is summoned");
        MemoryData held = SoulMemories.data(player.getMainHandItem());
        helper.assertTrue(held != null && held.id().equals(record.id), "Moonlight Shard is in the hand at once");
        helper.assertTrue(soul.essence() < full, "Summoning cost essence");
        ItemStack old = player.getMainHandItem().copy();
        SoulMemories.dismiss(player, record.id, -1, true);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "Dismissed, it leaves the hand");
        helper.assertTrue(Math.abs(soul.essence() - full) < 1.0E-3, "Dismissed nearby, its essence comes back");
        SoulMemories.summon(player, record.id, -1);
        MemoryData again = SoulMemories.data(player.getMainHandItem());
        helper.assertTrue(again != null && again.nonce() != SoulMemories.data(old).nonce(), "A copy from an earlier summoning no longer matches its soul");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void restingMemoriesMend(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        MemoryRecord record = SoulMemories.grant(player, Memories.AZURE_BLADE, false);
        ItemStack piece = record.pieces.get(0).stack;
        piece.setDamageValue(piece.getMaxDamage() / 2);
        int before = piece.getDamageValue();
        SoulMemories.mend(player);
        helper.assertTrue(piece.getDamageValue() < before, "A Memory resting in the soul mends");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void memoriesDieWithTheirOwnerButShardsDoNot(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        SoulMemories.grant(player, Memories.AZURE_BLADE, false);
        SoulMemories.grant(player, Memories.MIDNIGHT_SHARD, false);
        SoulMemories.onDeath(player);
        helper.assertTrue(Soul.of(player).memories().isEmpty(), "The owner's death takes their Memories with them");
        List<ItemEntity> left = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(8.0),
            e -> SoulMemories.data(e.getItem()) != null);
        helper.assertTrue(left.size() == 1 && Memories.MIDNIGHT_SHARD.id.equals(SoulMemories.data(left.get(0).getItem()).def()),
            "Only the Shard Memory is left behind");
        helper.assertTrue(!SoulMemories.data(left.get(0).getItem()).owned(), "It belongs to no one now");
        left.forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theGameRuleCanKeepMemories(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        SoulMemories.grant(player, Memories.AZURE_BLADE, false);
        helper.getLevel().getGameRules().getRule(MemoryItems.KEEP_MEMORIES).set(true, helper.getLevel().getServer());
        SoulMemories.onDeath(player);
        helper.getLevel().getGameRules().getRule(MemoryItems.KEEP_MEMORIES).set(false, helper.getLevel().getServer());
        helper.assertTrue(Soul.of(player).memories().size() == 1, "With keepSoulMemories on, Memories outlive a death");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sevenShardsMakeTheOathKeys(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        for (MemoryDef shard : Memories.SHARDS) {
            SoulMemories.grant(player, shard, false);
        }

        boolean keys = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            keys |= player.getInventory().getItem(slot).is(MemoryItems.OATH_KEY.get());
        }

        helper.assertTrue(keys, "The Oath Keys take shape once all seven Shards are held");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aLineageMemoryBecomesAWeave(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.SAINT);
        MemoryRecord record = SoulMemories.grant(player, Memories.DROP_OF_ICHOR, false);
        Worn w = new Worn(player, Soul.of(player), record.pieces.get(0).stack, record.data(), Worn.Slot.MAIN_HAND);
        helper.assertTrue(Enchant.WEAVERS_LINEAGE.use(w, InteractionHand.MAIN_HAND), "The drop is consumed");
        helper.assertTrue(Soul.of(player).hasAttribute(SoulAttribute.BLOOD_WEAVE.id()), "Weaver's lineage awakens as Blood Weave");
        helper.assertTrue(Soul.of(player).memory(record.id) == null, "A consumed lineage Memory is gone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void echoesAreFrozenAndDieForGood(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        SoulData soul = Soul.of(player);
        Zombie prey = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        EchoRecord record = Echoes.grant(player, prey, false);
        helper.assertTrue(record != null && !record.traits.isEmpty(), "A slain creature can become an Echo with its Attributes");
        prey.discard();
        soul.setEssence(soul.maxEssence());
        helper.assertTrue(Echoes.summon(player, record.id), "The Echo is called out");
        Mob body = (Mob)helper.getLevel().getEntity(record.body);
        helper.assertTrue(body != null && Echoes.isEcho(body), "Its body stands in the world, marked as an Echo");
        body.setHealth(body.getMaxHealth() * 0.4F);
        Echoes.dismiss(player, record.id, true);
        helper.assertTrue(record.health < 0.5F && !record.out, "It keeps its wounds when dismissed");
        Echoes.heal(player);
        helper.assertTrue(record.health > 0.45F, "At rest, it heals");
        Echoes.summon(player, record.id);
        Mob again = (Mob)helper.getLevel().getEntity(record.body);
        Echoes.slain(player, again);
        again.discard();
        helper.assertTrue(soul.echo(record.id) == null, "A slain Echo is gone for good");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sunnyMakesEchoesIntoShadows(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.AWAKENED);
        SoulData soul = Soul.of(player);
        Zombie prey = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        EchoRecord record = Echoes.grant(player, prey, false);
        prey.discard();
        helper.assertTrue(Echoes.whyNotShadow(soul, record) != null, "Only the Shadow Slave can make Shadows");
        soul.setAspect("shadow_slave", net.logosnumen.nightmareevolved.soul.Tier.DIVINE);
        soul.setFragments(soul.capacity());
        helper.assertTrue(Echoes.whyNotShadow(soul, record) != null, "It needs the matching shadow standing in the sea");
        var standing = Echoes.standing(soul);
        standing.add(StringTag.valueOf(record.type));
        soul.abilityState().put("Shadows", standing);
        int fragments = soul.fragments();
        helper.assertTrue(Echoes.makeShadow(player, record.id), "The Echo becomes a Shadow");
        helper.assertTrue(record.shadow && soul.fragments() == fragments - Echoes.SHADOW_COST, "It costs a hundred fragments");
        helper.assertTrue(Echoes.standing(soul).isEmpty(), "The standing shadow dissolves into it");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theCovetousCofferWalksAfterItsOwner(GameTestHelper helper) {
        ServerPlayer player = soul(helper, Rank.MASTER);
        SoulData soul = Soul.of(player);
        MemoryRecord record = SoulMemories.grant(player, Memories.COVETOUS_COFFER, false);
        soul.setEssence(soul.maxEssence());
        SoulMemories.summon(player, record.id, -1);
        helper.runAfterDelay(SoulMemories.GATHER + 2, () -> {
            ItemStack held = player.getMainHandItem();
            MemoryData data = SoulMemories.data(held);
            helper.assertTrue(data != null && data.id().equals(record.id), "The coffer gathers into its owner's hand");
            Worn w = new Worn(player, soul, held, data, Worn.Slot.MAIN_HAND);
            helper.assertTrue(Enchant.LOCOMOTIVE_CHIFFONIER.useOn(w, helper.absolutePos(new net.minecraft.core.BlockPos(2, 0, 2)),
                net.minecraft.core.Direction.UP), "Set on the ground, it climbs down");
            helper.assertTrue(player.getMainHandItem().isEmpty(), "It is no longer in the hand");
            helper.assertTrue(!helper.getLevel().getEntitiesOfClass(net.logosnumen.nightmareevolved.entity.memory.WalkingCofferEntity.class,
                player.getBoundingBox().inflate(6.0)).isEmpty(), "It stands on its own");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void everySoulHasItsOwnSea(GameTestHelper helper) {
        for (int i = 0; i < 1200; i += 37) {
            helper.assertTrue(SeaRegions.indexAt(SeaRegions.centerX(i), SeaRegions.centerZ(i)) == i, "Region " + i + " maps back to itself");
            helper.assertTrue(SeaRegions.indexAt(SeaRegions.centerX(i) + 70, SeaRegions.centerZ(i) - 70) == i, "Its whole sea belongs to it");
        }

        helper.assertTrue(SeaRegions.SPACING > 32 * 16 * 2, "No two seas are ever in sight of each other");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyMemoryHasItsArtAndItsWords(GameTestHelper helper) {
        JsonObject lang = json("/assets/nightmare_evolved/lang/en_us.json");
        helper.assertTrue(lang != null, "The language file loads");
        List<String> missing = new ArrayList<>();
        for (MemoryDef def : Memories.all()) {
            need(lang, missing, "memory.nightmare_evolved." + def.id);
            need(lang, missing, "memory.nightmare_evolved." + def.id + ".desc");
        }

        for (Enchant enchant : Enchant.values()) {
            need(lang, missing, "enchant.nightmare_evolved." + enchant.id());
            need(lang, missing, "enchant.nightmare_evolved." + enchant.id() + ".desc");
        }

        for (EchoTrait trait : EchoTrait.values()) {
            need(lang, missing, "echo_trait.nightmare_evolved." + trait.id());
        }

        for (Family family : Family.values()) {
            for (int i = 0; i < Memories.EPITHETS; i++) {
                need(lang, missing, "memory.nightmare_evolved.epithet." + family.id() + "." + i);
            }
        }

        for (MemoryDef def : Memories.all()) {
            if (!def.named) {
                for (int i = 0; i < Memories.NOUNS; i++) {
                    need(lang, missing, "memory.nightmare_evolved.noun." + def.id + "." + i);
                }
            }
        }

        for (var supplier : MemoryItems.all()) {
            Item item = supplier.get();
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            need(lang, missing, "item." + id.getNamespace() + "." + id.getPath());
            if (MemoryGameTests.class.getResource("/assets/nightmare_evolved/models/item/" + id.getPath() + ".json") == null) {
                missing.add("model " + id.getPath());
            }
        }

        helper.assertTrue(missing.isEmpty(), "Missing: " + String.join(", ", missing.subList(0, Math.min(12, missing.size()))));
        helper.succeed();
    }

    private static void need(JsonObject lang, List<String> missing, String key) {
        if (!lang.has(key)) {
            missing.add(key);
        }
    }

    private static JsonObject json(String path) {
        try (InputStream in = MemoryGameTests.class.getResourceAsStream(path)) {
            return in == null ? null : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
