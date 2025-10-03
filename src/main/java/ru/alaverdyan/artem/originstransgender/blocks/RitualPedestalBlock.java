package ru.alaverdyan.artem.originstransgender.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualPedestalBlockEntity;
import ru.alaverdyan.artem.originstransgender.blocks.entites.RitualTableBlockEntity;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;

public class RitualPedestalBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 18, 15);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public RitualPedestalBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualPedestalBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof RitualPedestalBlockEntity tableEntity)) {
            return super.onUse(state, world, pos, player, hand, hit);
        }
        ItemStack held = player.getStackInHand(hand);

        if (!world.isClient) {
            // Положить предмет (игрок не в Shift и держит предмет, стол пуст)
            if (!player.isSneaking() && !held.isEmpty() && tableEntity.isEmpty()) {
                tableEntity.getItems().set(0, held.split(1));
                tableEntity.setTableEmpty(false);
                tableEntity.markDirty();
                world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                return ActionResult.SUCCESS;
            }
            // Забрать предмет (игрок в Shift и стол не пуст)
            if (player.isSneaking() && held.isEmpty() && !tableEntity.isEmpty()) {
                ItemStack stack = tableEntity.removeStack(0);
                tableEntity.setTableEmpty(true);
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5, stack);
                tableEntity.markDirty();
                world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RitualPedestalBlockEntity tableEntity) {
                // Сброс предмета при разрушении
                if(tableEntity.isAnimationStarted()) return;
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), tableEntity.removeStack(0));
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, OTBlocks.RITUAL_PEDESTAL_ENTITY, RitualPedestalBlockEntity::tick);
    }
}
