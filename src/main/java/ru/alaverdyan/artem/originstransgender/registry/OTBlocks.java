package ru.alaverdyan.artem.originstransgender.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Instrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.*;
import ru.alaverdyan.artem.originstransgender.blocks.RitualPedestalBlock;
import ru.alaverdyan.artem.originstransgender.blocks.Umbralith;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualPedestalBlockEntity;
import ru.alaverdyan.artem.originstransgender.blocks.RitualTableBlock;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualTableBlockEntity;

import java.util.function.Function;

public class OTBlocks {
    public static final Block RITUAL_TABLE = register(
            "ritual_table",
            RitualTableBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).hardness(2f).requiresTool(),
            true
    );
    public static final Block RITUAL_PEDESTAL = register(
            "ritual_pedestal",
            RitualPedestalBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).hardness(2f).requiresTool(),
            true
    );
    public static final Block UMBRALITH = register(
            "umbralith",
            Umbralith::new,
            AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).strength(-1.0F, 3600000.0F).dropsNothing().allowsSpawning(Blocks::never),
            true
    );

    public static final Identifier RITUAL_BLOCK_ID = new Identifier("originstransgender", "ritual_table");
    public static final BlockEntityType<RitualTableBlockEntity> RITUAL_TABLE_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, RITUAL_BLOCK_ID,
                    FabricBlockEntityTypeBuilder.create(RitualTableBlockEntity::new, RITUAL_TABLE).build());

    public static final Identifier RITUAL_PEDESTAL_ID = new Identifier("originstransgender", "ritual_pedestal");
    public static final BlockEntityType<RitualPedestalBlockEntity> RITUAL_PEDESTAL_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, RITUAL_PEDESTAL_ID,
                    FabricBlockEntityTypeBuilder.create(RitualPedestalBlockEntity::new, RITUAL_PEDESTAL).build());


    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings);
        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }
        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Originstransgender.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Originstransgender.MOD_ID, name));
    }
    public static void initialize() {

    }
}
