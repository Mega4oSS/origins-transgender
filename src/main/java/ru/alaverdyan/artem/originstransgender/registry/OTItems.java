package ru.alaverdyan.artem.originstransgender.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.Originstransgender;
import ru.alaverdyan.artem.originstransgender.items.HairKnife;

import java.util.function.Function;

// People
// Enderian
// Merling
// Phantom
// Elytrian
// Blazeborn
// Avian
// Arachnid
// Shulk
// Feline

public class OTItems {
    public static final Item PEOPLE_DNA = register("people_dna", Item::new, new Item.Settings());
    public static final Item ENDERMAN_DNA = register("enderman_dna", Item::new, new Item.Settings());
    public static final Item FISH_DNA = register("fish_dna", Item::new, new Item.Settings());
    public static final Item PHANTOM_DNA = register("phantom_dna", Item::new, new Item.Settings());
    public static final Item ELYTRA_DNA = register("elytra_dna", Item::new, new Item.Settings());
    public static final Item BLAZE_DNA = register("blaze_dna", Item::new, new Item.Settings());
    public static final Item CHICKEN_DNA = register("chicken_dna", Item::new, new Item.Settings());
    public static final Item SPIDER_DNA = register("spider_dna", Item::new, new Item.Settings());
    public static final Item SHULKER_DNA = register("shulker_dna", Item::new, new Item.Settings());
    public static final Item FELINE_DNA = register("feline_dna", Item::new, new Item.Settings());
    public static final Item EMPTY_SPHERE = register("empty_sphere", Item::new, new Item.Settings());
    public static final Item SOUL_SPHERE = register("soul_sphere", Item::new, new Item.Settings().fireproof());
    public static final Item VESSEL_MEMORY = register("vessel_memory", Item::new, new Item.Settings());
    public static final Item TUFT_OF_HAIR = register("tuft_of_hair", Item::new, new Item.Settings());
    public static final Item HAIR_KNIFE = register("hair_knife", HairKnife::new, new Item.Settings());
    public static final Item MEMORY_SHARD = register("memory_shard", Item::new, new Item.Settings());

    public static void initialize() {
    }

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Originstransgender.MOD_ID, name));
        Item item = itemFactory.apply(settings);
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }
}
