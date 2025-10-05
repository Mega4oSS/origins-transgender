package ru.alaverdyan.artem.originstransgender.client;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualPedestalBlockEntity;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class RitualPedestalBlockEntityRenderer implements BlockEntityRenderer<RitualPedestalBlockEntity> {
    public static final float PEDESTAL_RISE_HEIGHT = 1.0f;
    public static final float PEDESTAL_ROTATION_SPEED = 45f;
    public static final float CENTER_RAISE_HEIGHT = 1.5f;
    public static final float EPS = 1e-6f;

    public static final int TICKS_PER_SECOND = 20;
    public static final int STAGE1_RING_TICKS = 1 * TICKS_PER_SECOND;
    public static final int STAGE2_RISE_TICKS = 3 * TICKS_PER_SECOND;
    public static final int STAGE3_LINES_TICKS = 2 * TICKS_PER_SECOND;
    public static final int STAGE3_LINES_FIRST_TICKS = 1 * TICKS_PER_SECOND;
    public static final int STAGE3_LINES_SECOND_TICKS = 1 * TICKS_PER_SECOND;

    public RitualPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    //@Override
    public void render2323(RitualPedestalBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        World world = entity.getWorld();
        if (world == null) return;

        ItemStack stack = entity.getStack(0);
        if (stack == null || stack.isEmpty()) return;

        int stage = entity.getRitualStage();
        int stageTicks = entity.getStageTicks();
        if (stage < 1 || stage > 3) {
            return;
        }

        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());

        matrices.push();
        final float baseX = 0.5f;
        final float baseZ = 0.5f;
        final float baseY = 0.8f;

        if (stage == 1) {
            matrices.translate(baseX, baseY, baseZ);
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                    overlay, matrices, vertexConsumers, world, 0);
        } else if (stage == 2) {
            float progress = (stageTicks + tickDelta) / (float) Math.max(STAGE2_RISE_TICKS, 1);
            if (progress > 1f) progress = 1f;
            float dy = progress * PEDESTAL_RISE_HEIGHT;
            float y = baseY + dy;
            float seconds = (stageTicks + tickDelta) / (float) TICKS_PER_SECOND;
            float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
            matrices.translate(baseX, y, baseZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                    overlay, matrices, vertexConsumers, world, 0);
        } else {
            int ticks = stageTicks;
            float localTick = stageTicks + tickDelta;

            if (ticks < STAGE3_LINES_FIRST_TICKS) {
                float dy = PEDESTAL_RISE_HEIGHT;
                float y = baseY + dy;
                float seconds = localTick / (float) TICKS_PER_SECOND;
                float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
                matrices.translate(baseX, y, baseZ);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                        overlay, matrices, vertexConsumers, world, 0);
            } else {
                float moveTick = (localTick - STAGE3_LINES_FIRST_TICKS); // от 0 до ~20
                float moveProgress = moveTick / (float) Math.max(STAGE3_LINES_SECOND_TICKS, 1f);
                if (moveProgress >= 1f - EPS) {
                    matrices.pop();
                    return;
                }
                float startX = baseX;
                float startY = baseY + PEDESTAL_RISE_HEIGHT;
                float startZ = baseZ;
                BlockPos center = entity.getRitualCenter();
                if (center == null) {
                    float seconds = localTick / (float) TICKS_PER_SECOND;
                    float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
                    matrices.translate(startX, startY, startZ);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
                    MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                            overlay, matrices, vertexConsumers, world, 0);
                    matrices.pop();
                    return;
                }

                BlockPos selfPos = entity.getPos();
                double deltaXBlocks = center.getX() - selfPos.getX();
                double deltaZBlocks = center.getZ() - selfPos.getZ();
                double deltaYBlocks = center.getY() - selfPos.getY();

                float targetX = (float) (deltaXBlocks + 0.5);
                float targetZ = (float) (deltaZBlocks + 0.5);
                float targetY = (float) (deltaYBlocks + 1.0f);

                float curX = lerp(startX, targetX, moveProgress);
                float curY = lerp(startY, targetY, moveProgress);
                float curZ = lerp(startZ, targetZ, moveProgress);

                float seconds = localTick / (float) TICKS_PER_SECOND;
                float angleDeg = seconds * PEDESTAL_ROTATION_SPEED * 1.5f;

                matrices.translate(curX, curY, curZ);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));

                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                        overlay, matrices, vertexConsumers, world, 0);
            }
        }
        matrices.translate(0.5f, 1.15f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                stack,
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

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static Direction getDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();

        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (Math.abs(dz) > 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    @Override
    public void render(RitualPedestalBlockEntity entity, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getStack(0);
        boolean hideVisible = false;
        if (!stack.isEmpty() && !entity.isTableEmpty()) {
            matrices.push();

            matrices.translate(0.5f, 1.15f, 0.5f);
            matrices.scale(0.5f, 0.5f, 0.5f);
            if(entity.isAnimationStarted()) {
                double now = org.lwjgl.glfw.GLFW.glfwGetTime();
                float delta = (float) (now - entity.getLastTime());
                entity.setLastTime(now);
                entity.setDeltaTimeStart(entity.getDeltaTimeStart() + delta);
                //float secs = ticksToSeconds(entity.getDeltaTimeStart());
                float secs = entity.getDeltaTimeStart();
                //System.out.println("SECS: " + (entity.getDeltaTimeStart()));

                matrices.translate(0, 2 * smoothstep(0.0f, 3f, secs), 0);
                //matrices.translate(0, Math.min(2, ((Math.pow(secs, 6))*smoothstep(0.0f,0.2f,secs))), 0);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90*smoothstep(0.0f,1f,secs)+90));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.isxAxis() ? 450 : 360)*smoothstep(0.1f,3f,secs)));

                matrices.translate(0, 0, 0);
                switch (Objects.requireNonNull(getDirection(entity.getPos(), entity.getRitualCenter()))) {
                    case SOUTH -> {
                        matrices.translate(0, 1*smoothstep(0,1,secs-3-0.5f), 5.5*smoothstep(0,1,secs-3-0.5f));
                    }
                    case NORTH -> {
                        matrices.translate(0, 1*smoothstep(0,1,secs-3-0.5f), -(5.5*smoothstep(0,1,secs-3-0.5f)));
                    }
                    case EAST -> {
                        matrices.translate(0, 1*smoothstep(0,1,secs-3-0.5f), 5.5*smoothstep(0,1,secs-3-0.5f));
                    }
                    case WEST -> {
                        matrices.translate(0, 1*smoothstep(0,1,secs-3-0.5f), -(5.5*smoothstep(0,1,secs-3-0.5f)));
                    }
                }
                if(entity.getDeltaTimeStart() > 20f) {
                    entity.setDeltaTimeStart(0f);
                    hideVisible = false;
                }
                if(entity.getDeltaTimeStart() > 5f) hideVisible = true;
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            }

            //int stage = entity.getRitualStage();
            //int stageTicks = entity.getStageTicks();

            //if (stage == 2) {
            //    float progress = (stageTicks + tickDelta) / (float) Math.max(STAGE2_RISE_TICKS, 1);
            //    if (progress > 1f) progress = 1f;
            //    float dy = progress * PEDESTAL_RISE_HEIGHT;
            //    float y = 1.15f + dy;
            //    float seconds = (stageTicks + tickDelta) / (float) TICKS_PER_SECOND;
            //    float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
            //    matrices.translate(0.5, y, 0.5);
            //    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
            //}
            if (!hideVisible) {
                int lightAbove = WorldRenderer.getLightmapCoordinates(entity.getWorld(), entity.getPos().up());
                MinecraftClient.getInstance().getItemRenderer().renderItem(
                        stack,
                        ModelTransformationMode.FIXED,
                        lightAbove,
                        OverlayTexture.DEFAULT_UV,
                        matrices,
                        vertexConsumers,
                        entity.getWorld(),
                        0
                );
            }
            matrices.pop();
        }
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float ticksToSeconds(float ticks) {
        return ticks / 20.0F;
    }

}
