package ru.alaverdyan.artem.originstransgender.client;

import io.github.apace100.origins.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualTableBlockEntity;

import static ru.alaverdyan.artem.originstransgender.client.RitualPedestalBlockEntityRenderer.smoothstep;

@Environment(EnvType.CLIENT)
public class RitualTableBlockEntityRenderer implements BlockEntityRenderer<RitualTableBlockEntity> {
    private static final float CENTER_RISE_HEIGHT = 1.5f;
    private static final float PEDESTAL_RISE_HEIGHT = 1.0f;
    private static final float CENTER_SPIRAL_RADIUS = 0.5f;
    private static final float CENTER_ROTATION_SPEED = 360f;
    private static final float PEDESTAL_ROTATION_SPEED = 45f;
    private static final int STAGE2_DURATION = 60;

    public RitualTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    //@Override
    public void render231(RitualTableBlockEntity entity, float tickDelta,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                       int light, int overlay) {
        int stage = entity.getRitualStage();
        int t = entity.getStageTicks();
        World world = entity.getWorld();
        if (world == null) return;

        matrices.push();

        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());

        matrices.translate(0.5f, 1.0f, 0.5f);

        if (stage == 2) {
            float progress = (t + tickDelta) / (STAGE2_DURATION / 2f);
            progress = Math.min(progress, 1f);
            float angle = (t + tickDelta) * CENTER_ROTATION_SPEED / 20f;
            float radius = CENTER_SPIRAL_RADIUS;
            float dy = progress * CENTER_RISE_HEIGHT;
            float dx = (float)Math.cos(Math.toRadians(angle)) * radius;
            float dz = (float)Math.sin(Math.toRadians(angle)) * radius;
            matrices.translate(dx, dy, dz);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 4) {
            float progress = (t + tickDelta) / 50f;
            float scale = Math.min(progress, 1f);
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 5) {
            float baseScale = 1.0f;
            float pulse = 0.05f * (float)Math.sin((t + tickDelta) / 5.0f);
            float scale = baseScale + pulse;
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 6) {
            float scale = 1.0f - (t + tickDelta) / 20f;
            scale = Math.max(scale, 0f);
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        matrices.translate(0.5f, 1.15f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);


        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));


        ItemStack centerStack = entity.getStack(0);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                centerStack,
                ModelTransformationMode.FIXED,
                lightAbove,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0
        );

        matrices.pop();
    }

    @Override
    public void render(RitualTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getStack(0);
        if (!stack.isEmpty() && !entity.isTableEmpty()) {
            matrices.push();

            matrices.translate(0.5f, 1.15f, 0.5f);
            matrices.scale(0.5f, 0.5f, 0.5f);


            if(entity.isAnimationStarted()) {
                double now = org.lwjgl.glfw.GLFW.glfwGetTime();
                float delta = (float) (now - entity.getLastTime());
                entity.setLastTime(now);
                entity.setDeltaTime(entity.getDeltaTime() + delta);
                //float secs = ticksToSeconds(entity.getDeltaTimeStart());
                float secs = entity.getDeltaTime();
                matrices.translate(0, (3 * smoothstep(0.0f, 3f, secs)), 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90*smoothstep(0.0f,1f,secs)+90));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(secs*90));
                matrices.scale(smoothstep(1, 0, secs-19), smoothstep(1, 0, secs-19), smoothstep(1, 0, secs-19));
                if(entity.getDeltaTime() > 20f) entity.setDeltaTime(0f);
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            }

            int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(
                    entity.getDeltaTime() > 6 ? ModItems.ORB_OF_ORIGIN.getDefaultStack() : stack,
                    ModelTransformationMode.FIXED,
                    lightAbove,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    vertexConsumers,
                    entity.getWorld(),
                    0
            );

            matrices.pop();
        }
    }

}
