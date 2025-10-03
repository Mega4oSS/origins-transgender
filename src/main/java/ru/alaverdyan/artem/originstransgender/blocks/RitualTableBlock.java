package ru.alaverdyan.artem.originstransgender.blocks;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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
import ru.alaverdyan.artem.originstransgender.registry.OTItems;
import ru.alaverdyan.artem.originstransgender.utils.DNAUtils;
import ru.alaverdyan.artem.originstransgender.utils.OriginUtils;
import ru.alaverdyan.artem.originstransgender.utils.ParticleUtils;
import ru.alaverdyan.artem.originstransgender.utils.RitualDataManager;

import java.util.Objects;

import static net.minecraft.util.StringIdentifiable.createCodec;

public class RitualTableBlock extends BlockWithEntity {
    private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 19, 15);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public RitualTableBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RitualTableBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof RitualTableBlockEntity tableEntity)) {
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

    public void tryInitRitual(PlayerEntity player, World world, BlockPos center) {
        if(!checkRitual(world, center, player)) return;

        RitualTableBlockEntity bec = (RitualTableBlockEntity) world.getBlockEntity(center);
        bec.setPlayerUsed(player);
        bec.setRitualStage(1);
        bec.setStageTicks(0);
        bec.setAnimationStarted(true);
        bec.markDirtyAndSync(world);

        // Проверяем пьедесталы по осям
        BlockPos[] offsets = {
                center.east(3),
                center.west(3),
                center.north(3),
                center.south(3)
        };

        for (int i = 0; i < offsets.length; i++) {
            BlockEntity be = world.getBlockEntity(offsets[i]);
            if (be instanceof RitualPedestalBlockEntity container) {
                if(i < 2) container.setxAxis(true);
                container.setRitualCenter(center);
                container.setRitualStage(1);
                container.setAnimationStarted(true);
                container.markDirtyAndSync(world);
            }
        }
        ParticleUtils.spawnRedDustCircle((ServerWorld) world, center, 4, 80);
    }

    public static boolean checkRitual(World world, BlockPos center, PlayerEntity player) {
        boolean hasPedestals = true;

        // Проверяем пьедесталы по осям
        BlockPos[] offsets = {
                center.east(3),
                center.west(3),
                center.north(3),
                center.south(3)
        };

        for (BlockPos pos : offsets) {
            if (!world.getBlockState(pos).isOf(OTBlocks.RITUAL_PEDESTAL)) {
                hasPedestals = false;
                break;
            }
        }

        if (!hasPedestals) return false;

        // Булены
        boolean currentRace = false;
        boolean futureRace = false;
        boolean soulSphere = false;
        boolean vesselMemory = false;
        RitualTableBlockEntity blockEntity = (RitualTableBlockEntity) world.getBlockEntity(center);
        boolean emptySphere = blockEntity.getStack(0).getItem() == OTItems.EMPTY_SPHERE;

        for (BlockPos pos : offsets) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RitualPedestalBlockEntity container) {
                ItemStack stack = container.getStack(0);
                if (stack.isEmpty()) continue;

                Item item = stack.getItem();

                if (isDNA(item)) {
                    //System.out.println("SDADS: " + Objects.requireNonNull(OriginUtils.getPlayerOrigin((ServerPlayerEntity) player, Identifier.of("origins", "origin"))).getIdentifier());
                    System.out.println(DNAUtils.itemToOrigin(item) + ";" + Objects.requireNonNull(OriginUtils.getPlayerOrigin((ServerPlayerEntity) player, Identifier.of("origins", "origin"))).getIdentifier());
                    if(Objects.requireNonNull(OriginUtils.getPlayerOrigin((ServerPlayerEntity) player, Identifier.of("origins", "origin"))).getIdentifier().getPath().equals(DNAUtils.itemToOrigin(item).getPath())) {
                        currentRace = true;
                    } else {
                        futureRace = true;
                        blockEntity.setFutureOrigin(DNAUtils.itemToOrigin(item));
                    }
                } else if (item == OTItems.SOUL_SPHERE) {
                    soulSphere = true;
                } else if (item == OTItems.VESSEL_MEMORY) {
                    vesselMemory = true;
                }
            }
        }
        System.out.println(currentRace + ";" +  futureRace  + ";" +  soulSphere  + ";" +  vesselMemory + ";" + emptySphere);
        if (currentRace && futureRace && soulSphere && vesselMemory && emptySphere) {
            if(tryRitual((ServerPlayerEntity) player, RitualDataManager.getRituals(player.getUuid()))) {
                RitualDataManager.addRitual(player.getUuid());
                player.sendMessage(Text.literal("§aРитуал успешно собран!"), false);
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean tryRitual(ServerPlayerEntity player, int ritualsCompleted) {
        int cost = (int)Math.ceil(25 * Math.pow(2.5, ritualsCompleted));
        if (player.experienceLevel < cost) return false;
        player.addExperienceLevels(-cost);
        return true;
    }

    private static boolean isDNA(Item item) {
        return item == OTItems.PEOPLE_DNA ||
                item == OTItems.ENDERMAN_DNA ||
                item == OTItems.FISH_DNA ||
                item == OTItems.PHANTOM_DNA ||
                item == OTItems.ELYTRA_DNA ||
                item == OTItems.BLAZE_DNA ||
                item == OTItems.CHICKEN_DNA ||
                item == OTItems.SPIDER_DNA ||
                item == OTItems.SHULKER_DNA ||
                item == OTItems.FELINE_DNA;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof RitualTableBlockEntity tableEntity) {
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
        return checkType(type, OTBlocks.RITUAL_TABLE_ENTITY, RitualTableBlockEntity::tick);
    }
}
