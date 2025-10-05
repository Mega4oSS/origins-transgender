package ru.alaverdyan.artem.originstransgender.blocks.entites;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.blocks.ImplementedInventory;
import ru.alaverdyan.artem.originstransgender.entities.FakeExperienceOrbEntity;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;
import ru.alaverdyan.artem.originstransgender.registry.OTDamageTypes;
import ru.alaverdyan.artem.originstransgender.registry.OTEntities;
import ru.alaverdyan.artem.originstransgender.utils.OriginUtils;
import ru.alaverdyan.artem.originstransgender.utils.ParticleUtils;

public class RitualTableBlockEntity extends BlockEntity implements ImplementedInventory {
    private static final int INVENTORY_SIZE = 1;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private boolean tableEmpty = true;
    private int ritualStage = 0;
    private int ritualStage1 = 0;
    private int stepIndex = 0;
    private final int maxSteps = 20;
    private int stageTicks = 0;
    private double lastTime = 0;
    private float deltaTime = 0;
    private Identifier futureOrigin;
    private boolean animationStarted = false;
    private PlayerEntity playerUsed;

    public static final int TICKS_PER_SECOND = 20;
    public static final int STAGE1_TICKS = 1 * TICKS_PER_SECOND;
    public static final int STAGE2_TICKS = 3 * TICKS_PER_SECOND;
    public static final int STAGE3_TICKS = 2 * TICKS_PER_SECOND;
    public static final int STAGE3_FIRST_TICKS = 1 * TICKS_PER_SECOND;
    public static final int STAGE3_SECOND_TICKS = 1 * TICKS_PER_SECOND;
    public static final int STAGE4_TICKS = (int)(2.5f * TICKS_PER_SECOND);
    public static final int STAGE5_TICKS = (int)(1.5f * TICKS_PER_SECOND);
    public static final int STAGE6_TICKS = 1 * TICKS_PER_SECOND;


    public RitualTableBlockEntity(BlockPos pos, BlockState state) {
        super(OTBlocks.RITUAL_TABLE_ENTITY, pos, state);
    }

    @Override
    public void markDirty() {
        assert world != null;
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        super.markDirty();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void writeNbt(NbtCompound view) {
        super.writeNbt(view);
        Inventories.writeNbt(view, inventory);
        view.putBoolean("tableEmpty", tableEmpty);
        view.putBoolean("animationStarted", animationStarted);
        view.putInt("ritualStage", ritualStage);
        view.putInt("stageTicks", stageTicks);
    }

    @Override
    public void readNbt(NbtCompound view) {
        super.readNbt(view);
        Inventories.readNbt(view, inventory);
        tableEmpty = view.getBoolean("tableEmpty");
        ritualStage = view.getInt("ritualStage");
        stageTicks = view.getInt("stageTicks");
        animationStarted = view.getBoolean("animationStarted");
    }

    public void setTableEmpty(boolean tableEmpty) {
        this.tableEmpty = tableEmpty;
    }

    public boolean isTableEmpty() {
        return tableEmpty;
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public static void tick(World world, BlockPos pos, BlockState state, RitualTableBlockEntity be) {
        if (world == null || world.isClient) return;
        if(be.animationStarted) {
            be.ritualStage++;
            if (be.ritualStage > 3) {
                be.ritualStage = 0;
                ParticleUtils.spawnRedDustCircle((ServerWorld) world, pos, 4, 80);
            }
            be.ritualStage1++;
            if(be.ritualStage1 > 60 && be.stepIndex < be.maxSteps) {
                Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5);

                Vec3d[] starts = new Vec3d[]{
                        center.add(2.7, -0.3, 0),  // +X
                        center.add(-2.7, -0.3, 0), // -X
                        center.add(0, -0.3, 2.7),  // +Z
                        center.add(0, -0.3, -2.7)  // -Z
                };

                for (Vec3d start : starts) {
                    spawnStepParticle((ServerWorld) world, start, center, ParticleTypes.END_ROD, be.stepIndex, be.maxSteps);
                }
                if(be.stepIndex < be.maxSteps) be.stepIndex++;

            }
            if(be.ritualStage1 > 85 && be.stepIndex - 20 < 50) {
                Vec3d center = new Vec3d(pos.getX() + 0.5, pos.getY() + 2.3, pos.getZ() + 0.5);

                double t = (be.stepIndex - 20) / (double) 50;

                double phi = Math.acos(1 - 2 * t);
                double theta = Math.PI * (1 + Math.sqrt(5)) * (be.stepIndex - 20);

                double x = center.x + 0.5 * Math.sin(phi) * Math.cos(theta);
                double y = center.y + 0.5 * Math.cos(phi);
                double z = center.z + 0.5 * Math.sin(phi) * Math.sin(theta);

                ((ServerWorld) world).spawnParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0, 0, 0);

                if(be.stepIndex - 20 < 50) be.stepIndex++;

            }
            if(be.ritualStage1 > 120 && be.stepIndex -70 < 20) {
                if(be.playerUsed != null) {
                    spawnFakeOrb((ServerWorld) world, be.playerUsed, pos.add(0, 2, 0));
                    if (be.stepIndex - 70 < 20) be.stepIndex++;
                }
            }
            if(be.ritualStage1 > 400) {
                be.ritualStage1 = 0;
                be.stepIndex = 0;
                be.ritualStage = 0;
                be.setStack(0, ItemStack.EMPTY);
                DamageSource damageSource = new DamageSource(
                        world.getRegistryManager()
                                .get(RegistryKeys.DAMAGE_TYPE)
                                .entryOf(OTDamageTypes.RITUAL_DAMAGE));
                be.playerUsed.damage(damageSource, Float.MAX_VALUE);
                if(be.futureOrigin != null) {
                    OriginUtils.setPlayerOrigin((ServerPlayerEntity) be.playerUsed, Identifier.of("origins", "origin"), be.futureOrigin);
                }
                be.setAnimationStarted(false);
                be.setTableEmpty(true);
                BlockPos[] offsets = {
                        pos.east(3),
                        pos.west(3),
                        pos.north(3),
                        pos.south(3)
                };

                for (int i = 0; i < offsets.length; i++) {
                    BlockEntity bep = world.getBlockEntity(offsets[i]);
                    if (bep instanceof RitualPedestalBlockEntity container) {
                        container.setAnimationStarted(false);
                        container.setTableEmpty(true);
                        container.markDirtyAndSync(world);
                        container.setStack(0, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public static void spawnFakeOrb(ServerWorld world, PlayerEntity player, BlockPos targetPos) {
        Random random = world.random;

        double spawnX = player.getX() + (random.nextDouble() - 0.5) * 0.5; // ±0.25
        double spawnY = player.getY() + 1.0 + (random.nextDouble() - 0.5) * 0.5; // ±0.25
        double spawnZ = player.getZ() + (random.nextDouble() - 0.5) * 0.5; // ±0.25

        double targetX = targetPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5; // ±0.25
        double targetY = targetPos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.5; // ±0.25
        double targetZ = targetPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5; // ±0.25

        FakeExperienceOrbEntity orb = new FakeExperienceOrbEntity(
                OTEntities.FAKE_XP_ORB,
                world,
                new Vec3d(targetX, targetY, targetZ),
                spawnX,
                spawnY,
                spawnZ
        );
        world.spawnEntity(orb);
    }

    public void setRitualStage(int ritualStage) {
        this.ritualStage = ritualStage;
    }

    public void setStageTicks(int stageTicks) {
        this.stageTicks = stageTicks;
    }

    public void advanceStage() {
        if (world == null) {
            ritualStage++;
            stageTicks = 0;
            return;
        }

        switch (ritualStage) {
            case 1: ritualStage = 2; break;
            case 2: ritualStage = 3; break;
            case 3: ritualStage = 4; break;
            case 4: ritualStage = 5; break;
            case 5: ritualStage = 6; break;
            case 6: ritualStage = 7; break;
            case 7: ritualStage = 0; break;
            default: ritualStage = 0; break;
        }
        stageTicks = 0;
        markDirtyAndSync(world);
    }

    public void markDirtyAndSync(World world) {
        if (world == null || world.isClient) return;
        this.markDirty();
        BlockState state = world.getBlockState(this.pos);
        world.updateListeners(this.pos, state, state, 3);
    }

    public int getRitualStage() {
        return ritualStage;
    }

    public int getStageTicks() {
        return stageTicks;
    }

    public double getLastTime() {
        return lastTime;
    }

    public void setLastTime(double lastTime) {
        this.lastTime = lastTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    public boolean isAnimationStarted() {
        return animationStarted;
    }

    public void setAnimationStarted(boolean animationStarted) {
        this.animationStarted = animationStarted;
    }

    public static void spawnStepParticle(ServerWorld world, Vec3d start, Vec3d end, ParticleEffect particle, int stepIndex, int maxSteps) {
        if(stepIndex > maxSteps) return;
        double t = (double) stepIndex / (double) maxSteps;
        Vec3d pos = start.lerp(end, t);
        world.spawnParticles(particle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    public void setPlayerUsed(PlayerEntity playerUsed) {
        this.playerUsed = playerUsed;
    }

    public void setFutureOrigin(Identifier futureOrigin) {
        this.futureOrigin = futureOrigin;
    }
}
