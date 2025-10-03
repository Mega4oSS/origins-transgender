package ru.alaverdyan.artem.originstransgender;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alaverdyan.artem.originstransgender.command.Hallucinate;
import ru.alaverdyan.artem.originstransgender.command.HallucinateAll;
import ru.alaverdyan.artem.originstransgender.command.SendRawMSG;
import ru.alaverdyan.artem.originstransgender.effects.BaldnessEffect;
import ru.alaverdyan.artem.originstransgender.effects.ShadowGameEffect;
import ru.alaverdyan.artem.originstransgender.listeners.GoatHornListener;
import ru.alaverdyan.artem.originstransgender.registry.*;
import ru.alaverdyan.artem.originstransgender.utils.RitualDataManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class Originstransgender implements ModInitializer {
    public static final String MOD_ID = "originstransgender";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final RegistryKey<ItemGroup> CUSTOM_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Originstransgender.MOD_ID, "item_group"));
    public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(OTItems.EMPTY_SPHERE))
            .displayName(Text.translatable("itemGroup.originstransgender"))
            .build();
    public static final StatusEffect BALDNESS = new BaldnessEffect();
    public static final StatusEffect SHADOW_GAME = new ShadowGameEffect();
    // Сюда кладём счётчики для игроков
    public static final Map<UUID, Integer> usageMap = new HashMap<>();
    public static final Map<UUID, Integer> countTicker = new HashMap<>();
    private int ticks = 0;
    public static final SoundEvent CAVE6_SOUND = SoundEvent.of(new Identifier(MOD_ID, "caveam"));
    @Override
    public void onInitialize() {
        OTItems.initialize();
        OTBlocks.initialize();
        OTEntities.init();
        OTParticles.register();
        OTFluids.register();
        OTDamageTypes.init();
        Registry.register(Registries.ITEM_GROUP, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
        GoatHornListener.register();

        RitualDataManager.load();

        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(OTBlocks.RITUAL_TABLE.asItem());
            itemGroup.add(OTBlocks.RITUAL_PEDESTAL.asItem());
            itemGroup.add(OTItems.PEOPLE_DNA);
            itemGroup.add(OTItems.ENDERMAN_DNA);
            itemGroup.add(OTItems.FISH_DNA);
            itemGroup.add(OTItems.PHANTOM_DNA);
            itemGroup.add(OTItems.ELYTRA_DNA);
            itemGroup.add(OTItems.BLAZE_DNA);
            itemGroup.add(OTItems.CHICKEN_DNA);
            itemGroup.add(OTItems.SPIDER_DNA);
            itemGroup.add(OTItems.SHULKER_DNA);
            itemGroup.add(OTItems.FELINE_DNA);
            itemGroup.add(OTItems.EMPTY_SPHERE);
            itemGroup.add(OTItems.SOUL_SPHERE);
            itemGroup.add(OTItems.VESSEL_MEMORY);
            itemGroup.add(OTItems.TUFT_OF_HAIR);
            itemGroup.add(OTItems.HAIR_KNIFE);
            itemGroup.add(OTItems.MEMORY_SHARD);
        });
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return true;
            if (!(source.getSource() instanceof FallingBlockEntity falling)) return true;

            // Проверяем, что падает именно наковальня
            if (!falling.getBlockState().isOf(net.minecraft.block.Blocks.ANVIL)) return true;

            // Проверяем уровень опыта
            if (player.experienceLevel >= 1) {
                // Обнуляем уровень
                player.addExperienceLevels(-1);

                // Дропаем предмет
                ItemStack shard = new ItemStack(
                        OTItems.MEMORY_SHARD
                );
                player.dropItem(shard, true);
            }
            return true;
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (source.getAttacker() == null && source.getName().equals("inFire")) {
                if (player.getWorld().getBlockState(player.getBlockPos().down()).isOf(Blocks.SOUL_SAND) || player.getWorld().getBlockState(player.getBlockPos().down()).isOf(Blocks.SOUL_SOIL) || player.getWorld().getBlockState(player.getBlockPos()).isOf(Blocks.SOUL_SAND) || player.getWorld().getBlockState(player.getBlockPos()).isOf(Blocks.SOUL_SOIL)) {
                    // Проверяем уровень
                    if (player.experienceLevel >= 10) {
                        // Дропаем сферу
                        ItemStack sphere = new ItemStack(OTItems.SOUL_SPHERE);
                        System.out.println("TRUE");
                        player.dropItem(sphere, true);
                    }
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ticks++;
            if (ticks >= 1200) {
                usageMap.clear();
                ticks = 0;
            }
        });
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier("originstransgender", "baldness"), BALDNESS);
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier("originstransgender", "shadow_game"), SHADOW_GAME);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Command 1: Hallucinations for all online players
            HallucinateAll.register(dispatcher);

            // Command 2: Hallucinations for a specific player
            Hallucinate.register(dispatcher);

            // Command 3: Send a styled message to a player
            SendRawMSG.register(dispatcher);
        });
    }
}
