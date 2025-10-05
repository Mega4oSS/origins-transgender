package ru.alaverdyan.artem.originstransgender.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

import java.util.*;

public class InfectionManager extends PersistentState {

    private final Deque<BlockPos> queue = new LinkedList<>();
    private final Set<Long> inQueue = new HashSet<>();
    private final Block infectBlock;
    private final MinecraftServer server;
    private long tickCounter = 0;
    private boolean started = false;
    int infected = 0;
    int requeuedBecauseChunkNull = 0;
    int processed = 0;

    private static final String SAVE_KEY = "infection_manager";

    public InfectionManager(MinecraftServer server, Block infectBlock) {
        this.server = server;
        this.infectBlock = infectBlock;
    }

    public static InfectionManager get(ServerWorld world, Block infectBlock) {
        PersistentStateManager manager = world.getPersistentStateManager();

        return manager.getOrCreate(
                nbt -> InfectionManager.fromNbt(world.getServer(), infectBlock, nbt),
                () -> new InfectionManager(world.getServer(), infectBlock),
                SAVE_KEY
        );
    }

    private static InfectionManager fromNbt(MinecraftServer server, Block infectBlock, NbtCompound nbt) {
        InfectionManager mgr = new InfectionManager(server, infectBlock);
        NbtList list = nbt.getList("Queue", 4);
        for (int i = 0; i < list.size(); i++) {
            long packed = ((NbtLong) list.get(i)).longValue();
            BlockPos pos = BlockPos.fromLong(packed);
            mgr.queue.addLast(pos);
            mgr.inQueue.add(packed);
        }
        return mgr;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (BlockPos pos : queue) {
            list.add(NbtLong.of(pos.asLong()));
        }
        nbt.put("Queue", list);
        return nbt;
    }

    public void start() {
        if (started) return;
        started = true;

        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> this.markDirty());
    }
    public void addInfection(BlockPos pos) {
        long key = pos.asLong();
        if (inQueue.add(key)) {
            queue.addLast(pos.toImmutable());
            markDirty();
        }
    }
    public void addInfectionToFront(BlockPos pos) {
        long key = pos.asLong();
        if (inQueue.add(key)) {
            queue.addFirst(pos.toImmutable());
            markDirty();
        }
    }

    private void tick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter % 100 != 0) return;
        if (queue.isEmpty()) return;
        ServerWorld world = server.getOverworld();

        int infectionsPerTick = 64;

        for (int i = 0; i < infectionsPerTick && !queue.isEmpty(); i++) {
            BlockPos pos = queue.pollFirst();
            if (pos == null) continue;

            processed++;
            long key = pos.asLong();
            inQueue.remove(key);

            Chunk chunk = world.getChunkManager().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
            if (chunk == null) {
                if (inQueue.add(key)) queue.addLast(pos);
                requeuedBecauseChunkNull++;
                continue;
            }

            BlockState state = world.getBlockState(pos);

            if (state.isOf(infectBlock)) {
                spreadNext(world, pos);
                markDirty();
                continue;
            }

            if (!canInfect(state)) continue;

            world.setBlockState(pos, infectBlock.getDefaultState());
            infected++;
            spreadNext(world, pos);
            markDirty();
        }

        if (tickCounter % 1200 != 0) {
            Originstransgender.LOGGER.info(String.format("Infection tick: processed=%d infected=%d requeued=%d queueSize=%d",
                    processed, infected, requeuedBecauseChunkNull, queue.size()));
            processed = 0;
            infected = 0;
            requeuedBecauseChunkNull = 0;
        }
    }

    private void spreadNext(ServerWorld world, BlockPos origin) {
        for (BlockPos offset : BlockPos.iterate(origin.add(-1, -1, -1), origin.add(1, 1, 1))) {
            if (offset.equals(origin)) continue;
            BlockState s = world.getBlockState(offset);
            if (!canInfect(s)) continue;

            long key = offset.asLong();
            if (inQueue.add(key)) {
                queue.addLast(offset.toImmutable());
            }
        }
    }

    private boolean canInfect(BlockState state) {
        if (state.isAir()) return false;
        Block block = state.getBlock();

        return !(block == Blocks.BEDROCK
                || block == Blocks.END_PORTAL
                || block == Blocks.END_PORTAL_FRAME
                || block == infectBlock);
    }

    public MinecraftServer getServer() {
        return server;
    }
}
