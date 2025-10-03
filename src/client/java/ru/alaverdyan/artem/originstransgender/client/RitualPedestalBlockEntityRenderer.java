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
    // ======= Конфигурируемые параметры (статические поля) =======
    // Обрати внимание: значения в градусах/секунду и блоках/тиках — удобно менять для балансировки.
    public static final float PEDESTAL_RISE_HEIGHT = 1.0f;         // на какую высоту поднимается предмет (блоки)
    public static final float PEDESTAL_ROTATION_SPEED = 45f;      // градусов в секунду (медленное вращение)
    public static final float CENTER_RAISE_HEIGHT = 1.5f;         // (только для понимания; тут не используется)
    public static final float EPS = 1e-6f;

    // Длительности этапов (в тиках)
    public static final int TICKS_PER_SECOND = 20;
    public static final int STAGE1_RING_TICKS = 1 * TICKS_PER_SECOND;           // 1 сек
    public static final int STAGE2_RISE_TICKS = 3 * TICKS_PER_SECOND;           // 3 сек (полный подъём = 60 тиков)
    public static final int STAGE3_LINES_TICKS = 2 * TICKS_PER_SECOND;          // 2 сек (1 сек линии + 1 сек движение)
    public static final int STAGE3_LINES_FIRST_TICKS = 1 * TICKS_PER_SECOND;    // первая секунда — линии (вне BER)
    public static final int STAGE3_LINES_SECOND_TICKS = 1 * TICKS_PER_SECOND;   // вторая секунда — движение в центр
    // остальные этапы: 4 (2.5s), 5 (1.5s), 6 (1s), 7 (мгновенно) — но пьедестал после этапа 3 уже не отображает предмет
    // =============================================================

    public RitualPedestalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    //@Override
    public void render2323(RitualPedestalBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        // Защита: мир может быть null (например при загрузке). Тогда ничего не делаем.
        World world = entity.getWorld();
        if (world == null) return;

        ItemStack stack = entity.getStack(0); // ожидается корректный геттер в BE
        if (stack == null || stack.isEmpty()) return;

        int stage = entity.getRitualStage();   // ожидаем значения 1..7 (см. описание стадий)
        int stageTicks = entity.getStageTicks();

        // Если стадия >=4 (сфера дыма и далее), предмет с пьедестала уже скрыт (по сценарию).
        // Отрисовываем только для стадий 1..3 (и в пределах второй части стадии 3 — до достижения центра).
        if (stage < 1 || stage > 3) {
            return; // ничего не рендерим
        }

        // Рассчитаем освещение с позиции над пьедесталом — даёт более корректный свет предмету.
        int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());

        // Пушим матрицу: все трансформации локальны для этого блока.
        matrices.push();

        // Базовая позиция предмета на пьедестале (локальные координаты блока)
        // Y-координату можно менять — 0.5..1.2 зависит от модели пьедестала.
        final float baseX = 0.5f;
        final float baseZ = 0.5f;
        final float baseY = 0.8f; // начальная высота предмета над полом блока (до подъёма)

        // Общее поведение по стадиям:
        // 1) STAGE 1 (Кольцо, 1 сек) -> предмет статичен на base position
        // 2) STAGE 2 (Подъём, 3 сек) -> предмет плавно поднимается на PEDESTAL_RISE_HEIGHT, медленно вращается
        // 3) STAGE 3 (Линии, 2 сек) -> первая секунда: статично (частицы вне BER)
        //    вторая секунда: предмет летит к центру (интерполяция) и исчезает при достижении
        //
        // Для всех вычислений используем (stageTicks + tickDelta) для плавности и детерминированности.

        if (stage == 1) {
            // Стадия 1: статичная отрисовка на пьедестале
            matrices.translate(baseX, baseY, baseZ);
            // Можно при желании добавить лёгкий наклон или slow rotation — но по сценарию предмет неподвижен.
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                    overlay, matrices, vertexConsumers, world, 0);
        } else if (stage == 2) {
            // Стадия 2: подъём за 3 секунды (полная длительность STAGE2_RISE_TICKS)
            float progress = (stageTicks + tickDelta) / (float) Math.max(STAGE2_RISE_TICKS, 1);
            if (progress > 1f) progress = 1f;
            // Линейный подъём по Y
            float dy = progress * PEDESTAL_RISE_HEIGHT;
            float y = baseY + dy;
            // Плавное вращение вокруг Y: угол в градусах = (секунды) * скорость (deg/sec)
            float seconds = (stageTicks + tickDelta) / (float) TICKS_PER_SECOND;
            float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
            matrices.translate(baseX, y, baseZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
            // Рендерим предмет
            MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                    overlay, matrices, vertexConsumers, world, 0);
        } else { // stage == 3
            // Стадия 3: 2 секунды — 1я секунда: линии (вне BER), 2я секунда: предметы летят в центр.
            // Если предмет уже "долетел" до центра — не рендерим.
            int ticks = stageTicks;
            float localTick = stageTicks + tickDelta;

            if (ticks < STAGE3_LINES_FIRST_TICKS) {
                // Первая секунда: рисуем как в конце подъёма (предмет остаётся поднятым)
                // Чтобы выглядеть плавно, можно считать что он остался на максимуме подъёма.
                float dy = PEDESTAL_RISE_HEIGHT; // уже поднят
                float y = baseY + dy;
                float seconds = localTick / (float) TICKS_PER_SECOND;
                float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
                matrices.translate(baseX, y, baseZ);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                        overlay, matrices, vertexConsumers, world, 0);
            } else {
                // Вторая секунда: предмет движется к центру ритуала.
                float moveTick = (localTick - STAGE3_LINES_FIRST_TICKS); // от 0 до ~20
                float moveProgress = moveTick / (float) Math.max(STAGE3_LINES_SECOND_TICKS, 1f);
                if (moveProgress >= 1f - EPS) {
                    // Достиг центра — не рендерим (предмет исчезает).
                    matrices.pop();
                    return;
                }
                // Начальная позиция (локальная относительно блока)
                float startX = baseX;
                float startY = baseY + PEDESTAL_RISE_HEIGHT; // предмет уже поднят когда летит
                float startZ = baseZ;

                // Целевая позиция — позиция центра ритуала в локальных координатах блока.
                // BE должен хранить BlockPos центра: blockCenter
                BlockPos center = entity.getRitualCenter(); // ожидается геттер в BE
                if (center == null) {
                    // Если центра нет — безопасно рисуем как статично в начале перемещения
                    float seconds = localTick / (float) TICKS_PER_SECOND;
                    float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
                    matrices.translate(startX, startY, startZ);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
                    MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                            overlay, matrices, vertexConsumers, world, 0);
                    matrices.pop();
                    return;
                }

                // Переводим центр в локальные координаты относительно текущего блока
                BlockPos selfPos = entity.getPos(); // позиция этого BE
                double deltaXBlocks = center.getX() - selfPos.getX();
                double deltaZBlocks = center.getZ() - selfPos.getZ();
                double deltaYBlocks = center.getY() - selfPos.getY();

                // Локальная целевая точка: (deltaXBlocks + 0.5, deltaYBlocks + 1.0, deltaZBlocks + 0.5)
                // Здесь предполагаем, что центр предмета над столом находится на высоте center.y + 1.0
                float targetX = (float) (deltaXBlocks + 0.5);
                float targetZ = (float) (deltaZBlocks + 0.5);
                float targetY = (float) (deltaYBlocks + 1.0f);

                // Интерполируем позицию
                float curX = lerp(startX, targetX, moveProgress);
                float curY = lerp(startY, targetY, moveProgress);
                float curZ = lerp(startZ, targetZ, moveProgress);

                // Немного поворачиваем предмет по пути (можно сохранить вращение или добавить ускорение)
                float seconds = localTick / (float) TICKS_PER_SECOND;
                float angleDeg = seconds * PEDESTAL_ROTATION_SPEED * 1.5f; // чуть быстрее в полёте

                matrices.translate(curX, curY, curZ);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));

                // Можно также уменьшать масштаб при приближении к центру (опционально)
                // float scale = 1.0f - 0.5f * moveProgress;
                // matrices.scale(scale, scale, scale);

                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightAbove,
                        overlay, matrices, vertexConsumers, world, 0);
            }
        }
        matrices.translate(0.5f, 1.15f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);


        // Поворачиваем, чтобы предмет лежал на столе
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        // Центрируем предмет относительно модели (GROUND трансформация рисует от угла)

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

    /**
     * Линейная интерполяция
     */
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
        return null; // если позиции совпадают
    }
    // Регистрация: при инициализации клиента регистрируй renderer:
    // BlockEntityRendererRegistry.register(ModBlockEntities.RITUAL_PEDESTAL, RitualPedestalBlockEntityRenderer::new);

    @Override
    public void render(RitualPedestalBlockEntity entity, float tickDelta, MatrixStack matrices,
                          VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getStack(0);
        boolean hideVisible = false;
        if (!stack.isEmpty() && !entity.isTableEmpty()) {
            matrices.push();

            // Ставим в центр блока
            matrices.translate(0.5f, 1.15f, 0.5f);
            matrices.scale(0.5f, 0.5f, 0.5f);
            if(entity.isAnimationStarted()) {
                double now = org.lwjgl.glfw.GLFW.glfwGetTime();
                float delta = (float) (now - entity.getLastTime()); // секунды, обычно 0.001..0.05 и т.д.
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
            // Поворачиваем, чтобы предмет лежал на столе

            //int stage = entity.getRitualStage();   // ожидаем значения 1..7 (см. описание стадий)
            //int stageTicks = entity.getStageTicks();

            //if (stage == 2) {
            //    // Стадия 2: подъём за 3 секунды (полная длительность STAGE2_RISE_TICKS)
            //    float progress = (stageTicks + tickDelta) / (float) Math.max(STAGE2_RISE_TICKS, 1);
            //    if (progress > 1f) progress = 1f;
            //    // Линейный подъём по Y
            //    float dy = progress * PEDESTAL_RISE_HEIGHT;
            //    float y = 1.15f + dy;
            //    // Плавное вращение вокруг Y: угол в градусах = (секунды) * скорость (deg/sec)
            //    float seconds = (stageTicks + tickDelta) / (float) TICKS_PER_SECOND;
            //    float angleDeg = seconds * PEDESTAL_ROTATION_SPEED;
            //    matrices.translate(0.5, y, 0.5);
            //    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angleDeg));
            //    // Рендерим предмет
            //}
            // Центрируем предмет относительно модели (GROUND трансформация рисует от угла)
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
