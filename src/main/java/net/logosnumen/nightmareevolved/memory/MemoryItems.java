package net.logosnumen.nightmareevolved.memory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.logosnumen.nightmareevolved.NightmareEvolved;
import net.logosnumen.nightmareevolved.memory.item.MemoryArmorItem;
import net.logosnumen.nightmareevolved.memory.item.MemoryArrowItem;
import net.logosnumen.nightmareevolved.memory.item.MemoryBowItem;
import net.logosnumen.nightmareevolved.memory.item.MemoryShieldItem;
import net.logosnumen.nightmareevolved.memory.item.MemoryThingItem;
import net.logosnumen.nightmareevolved.memory.item.MemoryWeaponItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The items Memories take: one for each named Memory of the novel (and each layer of an armour Memory), one for each
 * shape the Spell weaves for anonymous prey, the Oath Keys, and the data that makes any of them a Memory. Also the
 * game rule that lets a world keep Memories through death.
 */
public final class MemoryItems {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NightmareEvolved.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NightmareEvolved.MOD_ID);
    private static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, NightmareEvolved.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MemoryData>> DATA = COMPONENTS.register(
        "memory", () -> DataComponentType.<MemoryData>builder().persistent(MemoryData.CODEC).networkSynchronized(MemoryData.STREAM_CODEC).build()
    );
    /** As in the novel, Memories die with their owners; a world can choose to keep them. */
    public static final GameRules.Key<GameRules.BooleanValue> KEEP_MEMORIES = GameRules.register(
        "keepSoulMemories", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false)
    );
    private static final Map<String, List<DeferredItem<? extends Item>>> BY_DEF = new LinkedHashMap<>();
    private static final String[] LAYER_NAMES = {"helmet", "chestplate", "leggings", "boots"};
    public static final DeferredItem<Item> OATH_KEY = ITEMS.register("oath_key", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    /** Worn unseen by an undead Echo, so the sun does not burn what is only essence. */
    public static final DeferredItem<Item> ECHO_VEIL = ITEMS.register("echo_veil", () -> new Item(new Item.Properties().stacksTo(1)));
    /** Cruel Sight's spear form. */
    public static final DeferredItem<MemoryWeaponItem> CRUEL_SIGHT_SPEAR;

    static {
        for (MemoryDef def : Memories.all()) {
            BY_DEF.put(def.id, register(def));
        }

        CRUEL_SIGHT_SPEAR = ITEMS.register("cruel_sight_spear", () -> new MemoryWeaponItem(Memories.CRUEL_SIGHT.id, properties()));
    }

    private MemoryItems() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        MATERIALS.register(modBus);
        ITEMS.register(modBus);
    }

    /** The registry name of a Memory's item: its own name if the novel names it, "memory_" and its shape if not. */
    public static String itemName(MemoryDef def) {
        return def.named ? def.id : "memory_" + def.id;
    }

    private static Item.Properties properties() {
        return new Item.Properties().stacksTo(1).fireResistant();
    }

    private static List<DeferredItem<? extends Item>> register(MemoryDef def) {
        String name = itemName(def);
        List<DeferredItem<? extends Item>> items = new ArrayList<>();
        switch (def.shape) {
            case ARMOR -> {
                Holder<ArmorMaterial> material = MATERIALS.register(name, () -> new ArmorMaterial(
                    new EnumMap<>(Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 6, ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 2,
                        ArmorItem.Type.BODY, 6)),
                    0, SoundEvents.ARMOR_EQUIP_CHAIN, () -> Ingredient.EMPTY, List.of(new ArmorMaterial.Layer(NightmareEvolved.id(name))), 0.0F, 0.0F
                ));
                for (EquipmentSlot slot : def.layers) {
                    ArmorItem.Type type = switch (slot) {
                        case HEAD -> ArmorItem.Type.HELMET;
                        case CHEST -> ArmorItem.Type.CHESTPLATE;
                        case LEGS -> ArmorItem.Type.LEGGINGS;
                        default -> ArmorItem.Type.BOOTS;
                    };
                    items.add(ITEMS.register(name + "_" + LAYER_NAMES[3 - (slot.getIndex())], () -> new MemoryArmorItem(def.id, material, type,
                        properties().durability(type.getDurability(15)))));
                }
            }
            case BOW -> items.add(ITEMS.register(name, () -> new MemoryBowItem(def.id, properties().durability(384))));
            case ARROW -> items.add(ITEMS.register(name, () -> new MemoryArrowItem(def.id, properties())));
            case SHIELD -> items.add(ITEMS.register(name, () -> new MemoryShieldItem(def.id, properties().durability(336))));
            case THING -> items.add(ITEMS.register(name, () -> new MemoryThingItem(def.id, properties())));
            default -> items.add(ITEMS.register(name, () -> new MemoryWeaponItem(def.id, properties())));
        }

        return items;
    }

    /** Every Memory item, for the creative tab and for checks. */
    public static List<Supplier<? extends Item>> all() {
        List<Supplier<? extends Item>> out = new ArrayList<>();
        BY_DEF.values().forEach(out::addAll);
        out.add(CRUEL_SIGHT_SPEAR);
        return out;
    }

    public static Item item(MemoryDef def, int piece) {
        List<DeferredItem<? extends Item>> items = BY_DEF.get(def.id);
        return items.get(Math.max(0, Math.min(items.size() - 1, piece))).get();
    }

    /** A Memory's piece as an item, its strength set by its Rank and Tier. */
    public static ItemStack stack(MemoryDef def, MemoryData data, int piece) {
        ItemStack stack = new ItemStack(item(def, piece));
        stack.set(DATA.get(), data);
        shape(stack, def, def.shape, piece);
        return stack;
    }

    /** Which piece of its Memory a stack is: its armour layer, or 0. */
    public static int layerOf(ItemStack stack) {
        MemoryData data = stack.get(DATA.get());
        MemoryDef def = data == null ? null : Memories.byId(data.def());
        if (def == null || !def.armor() || !(stack.getItem() instanceof ArmorItem armor)) {
            return 0;
        }

        return Math.max(0, def.layers.indexOf(armor.getEquipmentSlot()));
    }

    /** Cruel Sight in its other form, or empty for any other Memory. */
    public static ItemStack otherForm(ItemStack stack) {
        MemoryData data = stack.get(DATA.get());
        if (data == null || !Memories.CRUEL_SIGHT.id.equals(data.def())) {
            return ItemStack.EMPTY;
        }

        boolean spear = stack.is(CRUEL_SIGHT_SPEAR.get());
        ItemStack other = stack.transmuteCopy(spear ? item(Memories.CRUEL_SIGHT, 0) : CRUEL_SIGHT_SPEAR.get(), 1);
        shape(other, Memories.CRUEL_SIGHT, spear ? MemoryDef.Shape.LONGSWORD : MemoryDef.Shape.SPEAR, 0);
        return other;
    }

    /** Sets how hard a Memory strikes, how much it protects, and how long it lasts, by its Rank, Tier and enchantments. */
    public static void shape(ItemStack stack, MemoryDef def, MemoryDef.Shape shape, int piece) {
        MemoryData data = stack.get(DATA.get());
        if (data == null) {
            return;
        }

        int rank = data.rank();
        int tier = data.tier();
        double lasting = 1.0;
        for (String id : data.enchants()) {
            Enchant enchant = Enchant.byId(id);
            if (enchant != null) {
                lasting *= enchant.durability();
            }
        }

        int durability = (int)Math.min(Integer.MAX_VALUE / 2, Math.round(250.0 * (1 + rank) * (1.0 + 0.25 * (tier - 1)) * lasting));
        if (shape.melee()) {
            double damage = shape.damage + 1.5 * rank + 0.3 * (tier - 1);
            ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_damage"), damage,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_speed"), shape.speed,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            if (shape.reach > 0.0) {
                modifiers.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(NightmareEvolved.id("memory_reach"), shape.reach,
                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            }

            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers.build());
            stack.set(DataComponents.MAX_DAMAGE, durability);
        } else if (shape == MemoryDef.Shape.ARMOR && stack.getItem() instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getEquipmentSlot();
            double share = switch (slot) {
                case HEAD, FEET -> 0.15;
                case CHEST -> 0.4;
                default -> 0.3;
            };
            double set = 12.0 + 3.0 * rank + 0.5 * (tier - 1);
            ResourceLocation id = NightmareEvolved.id("memory_armor." + slot.getName());
            EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, Math.round(set * share * 2.0) / 2.0, AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, 0.5 * rank + 0.15 * (tier - 1), AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(id, 0.025 * rank, AttributeModifier.Operation.ADD_VALUE), group)
                .build());
            stack.set(DataComponents.MAX_DAMAGE, durability / 2);
        } else if (shape == MemoryDef.Shape.BOW || shape == MemoryDef.Shape.SHIELD) {
            stack.set(DataComponents.MAX_DAMAGE, durability);
        }

        if (stack.getDamageValue() >= stack.getMaxDamage() && stack.isDamageableItem()) {
            stack.setDamageValue(0);
        }
    }

    /** A plain item of a definition for display (the creative tab, the runes): ownerless, as the novel has it. */
    public static ItemStack display(MemoryDef def) {
        return stack(def, Memories.data(def, MemoryData.NOBODY), def.armor() ? Math.min(1, def.pieces() - 1) : 0);
    }
}
