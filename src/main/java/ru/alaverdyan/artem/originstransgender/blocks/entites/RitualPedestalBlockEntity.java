package ru.alaverdyan.artem.originstransgender.blocks.entites;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.blocks.ImplementedInventory;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;

import static ru.alaverdyan.artem.originstransgender.blocks.entites.RitualTableBlockEntity.*;

public class RitualPedestalBlockEntity extends BlockEntity implements ImplementedInventory {
    private static final int INVENTORY_SIZE = 1;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private boolean tableEmpty = true;
    private boolean animationStarted = false;
    private boolean xAxis = false;
    private int ritualStage = 0;
    private int stageTicks = 0;
    private double lastTime = 0;
    private float deltaTime = 0;
    private BlockPos centerPosition = new BlockPos(0,0, 0);


    public RitualPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(OTBlocks.RITUAL_PEDESTAL_ENTITY, pos, state);
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

    // Сериализация данных
    @Override
    public void writeNbt(NbtCompound view) {
        super.writeNbt(view);
        Inventories.writeNbt(view, inventory); // сохраняем инвентарь
        view.putBoolean("tableEmpty", tableEmpty);
        view.putBoolean("animationStarted", animationStarted);
        view.putBoolean("xAxis", xAxis);
        view.putInt("ritualStage", ritualStage);
        view.putInt("stageTicks", stageTicks);
        //view.putFloat("deltaTime", deltaTime);
        view.putInt("ritualCenterX", centerPosition.getX());
        view.putInt("ritualCenterY", centerPosition.getY());
        view.putInt("ritualCenterZ", centerPosition.getZ());
    }

    @Override
    public void readNbt(NbtCompound view) {
        super.readNbt(view);
        Inventories.readNbt(view, inventory); // загружаем инвентарь
        tableEmpty = view.getBoolean("tableEmpty");
        xAxis = view.getBoolean("xAxis");
        ritualStage = view.getInt("ritualStage");
        stageTicks = view.getInt("stageTicks");
        centerPosition = new BlockPos(view.getInt("ritualCenterX"), view.getInt("ritualCenterY"), view.getInt("ritualCenterZ"));
        animationStarted = view.getBoolean("animationStarted");
        //deltaTime = view.getFloat("deltaTime");
    }

    public void setTableEmpty(boolean tableEmpty) {
        this.tableEmpty = tableEmpty;
    }

    public boolean isTableEmpty() {
        return tableEmpty;
    }

    public static void tick(World world, BlockPos pos, BlockState state, RitualPedestalBlockEntity be) {
        if (world == null || world.isClient) return; // логика только на сервере

        if (be.ritualStage <= 0) return; // неактивен

        be.stageTicks++;
        boolean changed = false;

        switch (be.ritualStage) {
            case 1:
                if (be.stageTicks >= STAGE1_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 2:
                if (be.stageTicks >= STAGE2_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 3:
                if (be.stageTicks >= STAGE3_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 4:
                if (be.stageTicks >= STAGE4_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 5:
                if (be.stageTicks >= STAGE5_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 6:
                if (be.stageTicks >= STAGE6_TICKS) {
                    be.advanceStage(); changed = true;
                }
                break;
            case 7:
                // STAGE7 — мгновенный: по желанию можно finishRitual() сразу
                //be.finishRitual();
                changed = true;
                break;
        }


        if (changed) {
            be.stageTicks = 0; // если advanceStage не сбросил
            be.markDirtyAndSync(world);
        } else {
            // иногда стоит синхронизировать stageTicks реже (например, каждые 5 тиков),
            // но для точной синхронизации анимаций можно шлём чаще.
            // Чтобы снизить нагрузку, можно шлём update каждые N тиков:
            if (be.stageTicks % 5 == 0) be.markDirtyAndSync(world);
        }
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
            case 7: ritualStage = 0; break; // закончилось
            default: ritualStage = 0; break;
        }
        stageTicks = 0;
        markDirtyAndSync(world);
    }

    public void markDirtyAndSync(World world) {
        if (world == null || world.isClient) return;
        this.markDirty(); // пометить для сохранения
        BlockState state = world.getBlockState(this.pos);
        world.updateListeners(this.pos, state, state, 3); // шлёт обновление блока и BE клиентам
    }

    public void setRitualStage(int ritualStage) {
        this.ritualStage = ritualStage;
    }

    public void setStageTicks(int stageTicks) {
        this.stageTicks = stageTicks;
    }

    // Синхронизация данных на клиенте
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public boolean isxAxis() {
        return xAxis;
    }

    public void setxAxis(boolean xAxis) {
        this.xAxis = xAxis;
    }

    public int getRitualStage() {
        return ritualStage;
    }

    public int getStageTicks() {
        return stageTicks;
    }

    public void setRitualCenter(BlockPos center) {
        centerPosition = center;
        markDirty();
    }

    public BlockPos getRitualCenter() {
        return centerPosition;
    }

    public boolean isAnimationStarted() {
        return animationStarted;
    }

    public void setAnimationStarted(boolean animationStarted) {
        this.animationStarted = animationStarted;
        markDirty();
    }

    public float getDeltaTimeStart() {
        return deltaTime;
    }

    public void setDeltaTimeStart(float deltaTimeStart) {
        this.deltaTime = deltaTimeStart;
    }

    public double getLastTime() {
        return lastTime;
    }

    public void setLastTime(double lastTime) {
        this.lastTime = lastTime;
    }
}
