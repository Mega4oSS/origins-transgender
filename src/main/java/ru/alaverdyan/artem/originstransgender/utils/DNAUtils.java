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
        // Человек
        register(OTItems.PEOPLE_DNA, new Identifier("origins", "human"));
        // Эндермен
        register(OTItems.ENDERMAN_DNA, new Identifier("origins", "enderian"));
        // Мерлинг (рыб)
        register(OTItems.FISH_DNA, new Identifier("origins", "merling"));
        // Фантом
        register(OTItems.PHANTOM_DNA, new Identifier("origins", "phantom"));
        // Элитриан
        register(OTItems.ELYTRA_DNA, new Identifier("origins", "elytrian"));
        // Блейзборн
        register(OTItems.BLAZE_DNA, new Identifier("origins", "blazeborn"));
        // Авиан (курица)
        register(OTItems.CHICKEN_DNA, new Identifier("origins", "avian"));
        // Арахнид (паук)
        register(OTItems.SPIDER_DNA, new Identifier("origins", "arachnid"));
        // Шалкер
        register(OTItems.SHULKER_DNA, new Identifier("origins", "shulk"));
        // Фелайн (кошка)
        register(OTItems.FELINE_DNA, new Identifier("origins", "feline"));
    }

    private DNAUtils() {}

    private static void register(Item item, Identifier originId) {
        ITEM_TO_ORIGIN.put(item, originId);
        ORIGIN_TO_ITEM.put(originId, item);
    }

    /**
     * Проверка: является ли предмет ДНК любой из рас Origins.
     */
    public static boolean isDNA(Item item) {
        return ITEM_TO_ORIGIN.containsKey(item);
    }

    /**
     * Преобразовать DNA item → id origin’а (или null, если не DNA).
     */
    public static Identifier itemToOrigin(Item item) {
        return ITEM_TO_ORIGIN.get(item);
    }

    /**
     * Преобразовать id origin’а → DNA item (или null, если не зарегистрирован).
     */
    public static Item originToItem(Identifier originId) {
        return ORIGIN_TO_ITEM.get(originId);
    }
}
