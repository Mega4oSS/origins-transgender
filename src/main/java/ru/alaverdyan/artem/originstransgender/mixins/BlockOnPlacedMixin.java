package ru.alaverdyan.artem.originstransgender.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;
import ru.alaverdyan.artem.originstransgender.utils.InfectionManager;

@Mixin(Block.class)
public class BlockOnPlacedMixin {

    @Inject(method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"), cancellable = false)
    private void onPlacedInject(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (world == null) return;
        if (world.isClient) return;

        if (!(world instanceof ServerWorld serverWorld)) return;

        for (BlockPos nearby : BlockPos.iterate(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (serverWorld.getBlockState(nearby).getBlock() == OTBlocks.UMBRALITH) {
                InfectionManager mgr = InfectionManager.get(serverWorld, OTBlocks.UMBRALITH);
                mgr.addInfectionToFront(pos);
                mgr.addInfectionToFront(nearby);
                break;
            }
        }
    }
}