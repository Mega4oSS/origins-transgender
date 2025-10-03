// package: com.origins.transgender
package ru.alaverdyan.artem.originstransgender.fluids;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import ru.alaverdyan.artem.originstransgender.registry.OTFluids;

public class MemoryThreadFluid extends FlowableFluid {

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == OTFluids.STILL_MEMORY_THREAD || fluid == OTFluids.FLOWING_MEMORY_THREAD;
    }

    @Override
    public Item getBucketItem() {
        return null;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return OTFluids.MEMORY_THREAD_BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
    }

    @Override
    public boolean isStill(FluidState state) {
        return false;
    }

    @Override
    public int getTickRate(net.minecraft.world.WorldView world) {
        return 5;
    }

    @Override
    protected float getBlastResistance() {
        return 0;
    }

    @Override
    public Fluid getFlowing() {
        return OTFluids.FLOWING_MEMORY_THREAD;
    }

    @Override
    public Fluid getStill() {
        return OTFluids.STILL_MEMORY_THREAD;
    }

    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {

    }

    @Override
    protected int getFlowSpeed(WorldView world) {
        return 0;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView world) {
        return 0;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    // Вложенные классы для текущей и текучей варианта жидкости
    public static class Flowing extends MemoryThreadFluid {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState state) {
            return state.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState state) {
            return false;
        }
    }

    public static class Still extends MemoryThreadFluid {
        @Override
        public int getLevel(FluidState state) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState state) {
            return true;
        }
    }
}
