package ru.alaverdyan.artem.originstransgender.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

import static ru.alaverdyan.artem.originstransgender.Originstransgender.countTicker;

public class ShadowGameEffect extends StatusEffect {
    public ShadowGameEffect() {
        super(StatusEffectCategory.HARMFUL, 0xEECBAD); // цвет эффекта
        // Можно добавить дебафф к удаче ради юмора
        this.addAttributeModifier(EntityAttributes.GENERIC_LUCK,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                -2.0, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Обновление каждые 40 тиков (2 секунды)
        return duration % 5 == 0;

    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        World world = entity.getWorld();

        // Проверяем, что мы на серверной логической стороне :cite[2]:cite[5]
        if (world.isClient) {
            return;
        }

        double angle = 0;
        if(entity instanceof PlayerEntity player) {
            countTicker.put(player.getUuid(), countTicker.getOrDefault(player.getUuid(), 0) + 1);
             angle = Math.toRadians(countTicker.get(player.getUuid()) * 10); // 10 градусов за тик
        }
        // Вычисляем позицию на окружности (радиус 3 блока)
        double radius = 2 + world.random.nextDouble() * 3;
        double x = entity.getX() + radius * Math.cos(angle);
        double y = entity.getY() + 5;
        double z = entity.getZ() + radius * Math.sin(angle);
        BlockPos soundPos = new BlockPos((int) x, (int) y, (int) z);
        SoundEvent soundEvent = SoundEvents.BLOCK_STONE_STEP;
        for (int i = 0; i < 12; i++) {
            if(world.getBlockState(soundPos.down(i)).isAir()) continue;
            soundEvent = world.getBlockState(soundPos.down(i)).getSoundGroup().getStepSound();
            break;
        }
        // Выбираем случайный звук шагов:cite[9]
        double angle2 = world.random.nextDouble() * 2 * Math.PI;

        double x2 = entity.getX() + radius * Math.cos(angle2);
        double y2 = entity.getY();
        double z2 = entity.getZ() + radius * Math.sin(angle2);

        BlockPos soundPos2 = new BlockPos((int)x2, (int)y2, (int)z2);
        // Воспроизводим звук только для целевого игрока:cite[4]
        world.playSound(
                null, // source - без источника-сущности
                soundPos, // pos - позиция звука
                soundEvent, // sound - выбранный звук
                SoundCategory.AMBIENT, // category - категория "окружающие звуки":cite[4]
                0.7f, // volume - громкость
                0.9f + world.random.nextFloat() * 0.2f // pitch - высота тона
        );

        world.playSound(
                null, // source - без источника-сущности
                soundPos2, // pos - позиция звука
                Originstransgender.CAVE6_SOUND, // sound - выбранный звук
                SoundCategory.AMBIENT, // category - категория "окружающие звуки":cite[4]
                0.1f, // volume - громкость
                0.9f + world.random.nextFloat() * 0.2f // pitch - высота тона
        );
    }
}
