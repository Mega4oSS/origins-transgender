package ru.alaverdyan.artem.originstransgender.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

public class SinsOblivionEffect extends StatusEffect {
    public SinsOblivionEffect() {
        super(StatusEffectCategory.HARMFUL, 0x252525);
        this.addAttributeModifier(EntityAttributes.GENERIC_LUCK,
                "7107DE5E-7CE8-4030-940E-514C1F160890",
                -2.0, EntityAttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return;
        }
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
    }
}
