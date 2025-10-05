package ru.alaverdyan.artem.originstransgender.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.ExperienceOrbEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;
import ru.alaverdyan.artem.originstransgender.registry.OTEntities;
import ru.alaverdyan.artem.originstransgender.registry.OTFluids;

public class OriginstransgenderClient implements ClientModInitializer {
    public static ItemStack fakeOriginSphere;
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(OTEntities.FAKE_XP_ORB, ExperienceOrbEntityRenderer::new);
        BlockEntityRendererFactories.register(OTBlocks.RITUAL_TABLE_ENTITY, RitualTableBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(OTBlocks.RITUAL_PEDESTAL_ENTITY, RitualPedestalBlockEntityRenderer::new);

        Identifier stillTex = new Identifier("originstransgender", "block/memory_thread_still");
        Identifier flowTex  = new Identifier("originstransgender", "block/memory_thread_flow");

        FluidRenderHandlerRegistry.INSTANCE.register(OTFluids.STILL_MEMORY_THREAD, OTFluids.FLOWING_MEMORY_THREAD,
                new SimpleFluidRenderHandler(stillTex, flowTex, 0x88CCFF));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), OTFluids.STILL_MEMORY_THREAD, OTFluids.FLOWING_MEMORY_THREAD);
        fakeOriginSphere = new ItemStack(Items.NETHER_STAR, 1);
    }
}
