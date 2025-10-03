package ru.alaverdyan.artem.originstransgender.listeners;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alaverdyan.artem.originstransgender.registry.OTBlocks;
import ru.alaverdyan.artem.originstransgender.blocks.RitualTableBlock;

public class GoatHornListener {
    private static final Logger LOGGER = LogManager.getLogger("GoatHornListener");

    public static void register() {
        UseItemCallback.EVENT.register(GoatHornListener::onUseItem);
    }

    private static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        System.out.println("BEGIN");
        // Быстрая фильтрация по типу предмета
        if (!stack.isOf(Items.GOAT_HORN)) return TypedActionResult.pass(stack);
        System.out.println("GOAT_HORN");

        // Попробуем вытащить информацию о инструменте из NBT
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            System.out.println("NBT_NOT_NULL");
            // 2) Вариант с полем "instrument" — как compound { sound_event: "...", ... }
            if (nbt.contains("instrument")) {
                System.out.println("NBT_CONTAINS_INSTRUMENT");
                System.out.println("INSTRUMENT: " + nbt.getString("instrument"));

                if(nbt.getString("instrument").equals("minecraft:ponder_goat_horn") || nbt.getString("instrument").equals("ponder_goat_horn")) {
                    System.out.println("NBT_RIGHT");
                    if (!world.isClient) {
                        System.out.println("NBT_SERVER_SIDE");
                        BlockPos pos = isInsideRitualCircle(player, world, 3);
                        if (pos != null) {
                            System.out.println("NBT_IN_CIRCLE");
                            ((RitualTableBlock) world.getBlockState(pos).getBlock()).tryInitRitual(player, world, pos);
                        }
                    }
                }
            }
        }

        // дальше — либо pass, либо success в зависимости от желаемого поведения
        return TypedActionResult.pass(stack);
    }

    private static BlockPos isInsideRitualCircle(PlayerEntity player, World world, int radius) {
        BlockPos center = player.getBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > radius * radius) continue;

                    BlockPos pos = center.add(dx, dy, dz);
                    if (world.getBlockState(pos).getBlock() == OTBlocks.RITUAL_TABLE) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

}
