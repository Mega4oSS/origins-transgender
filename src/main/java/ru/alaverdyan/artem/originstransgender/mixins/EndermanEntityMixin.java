package ru.alaverdyan.artem.originstransgender.mixins;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

import java.util.List;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void forceAggressionToPlayer(CallbackInfo ci) {
        EndermanEntity enderman = (EndermanEntity) (Object) this;
        World world = enderman.getWorld();
        if(world.isClient) return;
        PlayerEntity targetPlayer = null;
        for (PlayerEntity player : world.getPlayers()) {
            if ("Laiko_Cat".equals(player.getGameProfile().getName()) || "Fredes_Ent".equals(player.getGameProfile().getName())) {
                targetPlayer = player;
                break;
            }
        }

        if (targetPlayer != null) {
            if (!targetPlayer.isCreative() && enderman.canSee(targetPlayer) && (!targetPlayer.hasStatusEffect(Originstransgender.SINS_OBLIVION))) {
                enderman.setTarget(targetPlayer);
            }
        }
    }
}