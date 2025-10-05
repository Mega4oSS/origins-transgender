package ru.alaverdyan.artem.originstransgender.utils;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.registry.OTItems;

import java.util.HashMap;
import java.util.Map;

public final class DNAUtils {

    private static final Map<Item, Identifier> ITEM_TO_ORIGIN = new HashMap<>();
    private static final Map<Identifier, Item> ORIGIN_TO_ITEM = new HashMap<>();

    static {
        register(OTItems.PEOPLE_DNA, new Identifier("origins", "human"));
        register(OTItems.ENDERMAN_DNA, new Identifier("origins", "enderian"));
        register(OTItems.FISH_DNA, new Identifier("origins", "merling"));
        register(OTItems.PHANTOM_DNA, new Identifier("origins", "phantom"));
        register(OTItems.ELYTRA_DNA, new Identifier("origins", "elytrian"));
        register(OTItems.BLAZE_DNA, new Identifier("origins", "blazeborn"));
        register(OTItems.CHICKEN_DNA, new Identifier("origins", "avian"));
        register(OTItems.SPIDER_DNA, new Identifier("origins", "arachnid"));
        register(OTItems.SHULKER_DNA, new Identifier("origins", "shulk"));
        register(OTItems.FELINE_DNA, new Identifier("origins", "feline"));
    }

    private DNAUtils() {}

    private static void register(Item item, Identifier originId) {
        ITEM_TO_ORIGIN.put(item, originId);
        ORIGIN_TO_ITEM.put(originId, item);
    }

    public static boolean isDNA(Item item) {
        return ITEM_TO_ORIGIN.containsKey(item);
    }

    public static Identifier itemToOrigin(Item item) {
        return ITEM_TO_ORIGIN.get(item);
    }

    public static Item originToItem(Identifier originId) {
        return ORIGIN_TO_ITEM.get(originId);
    }
}
