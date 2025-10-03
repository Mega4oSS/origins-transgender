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
    private static final float CENTER_ROTATION_SPEED = 360f; // градусов в секунду
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

        // Подготовка рендеринга: пушим матрицу
        matrices.push();

        // Получим параметры освещения над столом
        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());

        // Исходное смещение: центр стола
        matrices.translate(0.5f, 1.0f, 0.5f);

        if (stage == 2) {
            // Этап 2: Подъем центрального предмета
            float progress = (t + tickDelta) / (STAGE2_DURATION / 2f); // 0..1 за 30 тиков
            progress = Math.min(progress, 1f);
            float angle = (t + tickDelta) * CENTER_ROTATION_SPEED / 20f; // градусы
            float radius = CENTER_SPIRAL_RADIUS;
            float dy = progress * CENTER_RISE_HEIGHT;
            float dx = (float)Math.cos(Math.toRadians(angle)) * radius;
            float dz = (float)Math.sin(Math.toRadians(angle)) * radius;
            matrices.translate(dx, dy, dz);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
            // Рисуем центральный предмет
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 4) {
            // Этап 4: Появление нового предмета (масштаб)
            float progress = (t + tickDelta) / 50f; // 0..1 за 50 тиков
            float scale = Math.min(progress, 1f);
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 5) {
            // Этап 5: Пульсация центрального предмета
            float baseScale = 1.0f;
            float pulse = 0.05f * (float)Math.sin((t + tickDelta) / 5.0f);
            float scale = baseScale + pulse;
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        else if (stage == 6) {
            // Этап 6: Коллапс (снижение масштаба до 0)
            float scale = 1.0f - (t + tickDelta) / 20f; // 0..1 за 20 тиков
            scale = Math.max(scale, 0f);
            matrices.scale(scale, scale, scale);
            ItemStack centerStack = entity.getStack(0);
            MinecraftClient.getInstance().getItemRenderer().renderItem(centerStack, ModelTransformationMode.FIXED, lightAbove,
                    OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, 0);
        }
        // Другие этапы (1,3,7) здесь обрабатываются вне BER или не рисуют центральный предмет.
        // Ставим в центр блока
        matrices.translate(0.5f, 1.15f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);


        // Поворачиваем, чтобы предмет лежал на столе
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        // Центрируем предмет относительно модели (GROUND трансформация рисует от угла)

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

            // Ставим в центр блока
            matrices.translate(0.5f, 1.15f, 0.5f);
            matrices.scale(0.5f, 0.5f, 0.5f);


            // Поворачиваем, чтобы предмет лежал на столе
            if(entity.isAnimationStarted()) {
                double now = org.lwjgl.glfw.GLFW.glfwGetTime();
                float delta = (float) (now - entity.getLastTime()); // секунды, обычно 0.001..0.05 и т.д.
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

            // Центрируем предмет относительно модели (GROUND трансформация рисует от угла)

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
