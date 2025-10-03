package ru.alaverdyan.artem.originstransgender.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.ItemStack;

public class BaldnessEffect extends StatusEffect {
    public BaldnessEffect() {
        super(StatusEffectCategory.HARMFUL, 0xEECBAD); // цвет эффекта
        // Можно добавить дебафф к удаче ради юмора
        this.addAttributeModifier(EntityAttributes.GENERIC_LUCK,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                -2.0, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // Обновление каждые 40 тиков (2 секунды)
        return duration % 40 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Проверяем, надет ли шлем
        ItemStack helmet = entity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            // Снимаем шлем (выбрасываем на землю)
            entity.dropStack(helmet);
            entity.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
            entity.sendMessage(net.minecraft.text.Text.translatable("msg.losthelmet"));
        }
    }
}
