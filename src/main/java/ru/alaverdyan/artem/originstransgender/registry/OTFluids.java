package ru.alaverdyan.artem.originstransgender.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.Originstransgender;
import ru.alaverdyan.artem.originstransgender.fluids.MemoryThreadFluid;

public class OTFluids {
    public static FlowableFluid STILL_MEMORY_THREAD = Registry.register(Registries.FLUID, new Identifier(Originstransgender.MOD_ID, "memory_thread"), new MemoryThreadFluid.Still());
    public static FlowableFluid FLOWING_MEMORY_THREAD = Registry.register(Registries.FLUID, new Identifier(Originstransgender.MOD_ID, "flowing_memory_thread"), new MemoryThreadFluid.Flowing());

    // Block representing the fluid in-world
    public static Block MEMORY_THREAD_BLOCK = Registry.register(Registries.BLOCK, new Identifier(Originstransgender.MOD_ID, "memory_thread"),
                new FluidBlock(STILL_MEMORY_THREAD, FabricBlockSettings.copy(Blocks.WATER)) {});

    // Bucket item
   //public static Item MEMORY_THREAD_BUCKET = Registry.register(Registries.ITEM, new Identifier(Originstransgender.MOD_ID, "memory_thread_bucket"),
   //            new BucketItem(STILL_MEMORY_THREAD, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));


    public static void register() {}
}
