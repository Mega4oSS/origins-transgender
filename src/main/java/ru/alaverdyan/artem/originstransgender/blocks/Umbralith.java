package ru.alaverdyan.artem.originstransgender.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

public class Umbralith extends Block {

    public Umbralith(Settings settings) {
        super(settings);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (world.isClient) return;
        if ((entity instanceof PlayerEntity living)) {
            if (!((PlayerEntity) entity).hasStatusEffect(Originstransgender.SHADOW_GAME)) {
                Originstransgender.countTicker.put(living.getUuid(), 0);
                living.addStatusEffect(new StatusEffectInstance(Originstransgender.SHADOW_GAME, 100, 0, false, true, true));
            }
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 2, false, true, true));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 2, false, true, true));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2, false, true, true));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, false, true, true));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 2, false, true, true));
        } else {
            entity.damage(world.getDamageSources().magic(), 1.0F);
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.FAIL;
        if (player.hasPermissionLevel(2)) {
            Originstransgender.infectionManager.addInfection(pos);
            player.sendMessage(Text.translatable("INFECTION_START_SUCCESS"));
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}