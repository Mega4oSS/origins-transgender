package ru.alaverdyan.artem.originstransgender;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.apace100.origins.Origins;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alaverdyan.artem.originstransgender.command.Hallucinate;
import ru.alaverdyan.artem.originstransgender.command.HallucinateAll;
import ru.alaverdyan.artem.originstransgender.command.SendRawMSG;
import ru.alaverdyan.artem.originstransgender.effects.BaldnessEffect;
import ru.alaverdyan.artem.originstransgender.effects.ShadowGameEffect;
import ru.alaverdyan.artem.originstransgender.effects.SinsOblivionEffect;
import ru.alaverdyan.artem.originstransgender.listeners.GoatHornListener;
import ru.alaverdyan.artem.originstransgender.registry.*;
import ru.alaverdyan.artem.originstransgender.utils.EnderianExperienceHandler;
import ru.alaverdyan.artem.originstransgender.utils.InfectionManager;
import ru.alaverdyan.artem.originstransgender.utils.RitualDataManager;

import java.util.*;

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
    public static final StatusEffect SINS_OBLIVION = new SinsOblivionEffect();
    public static final Map<UUID, Integer> usageMap = new HashMap<>();
    public static final Map<UUID, Integer> countTicker = new HashMap<>();
    private int ticks = 0;
    public static final SoundEvent CAVE6_SOUND = SoundEvent.of(new Identifier(MOD_ID, "caveam"));
    private static final String TARGET_PLAYER = "Laiko_Cat";
    private static final int DAY_TICKS = 24000;
    private static final int[] ENDERMAN_TIMES = {2000, 10000, 18000};
    private static final int[] CHECK_TIMES = {1000, 3000, 5000, 7000, 9000, 12000, 15000, 17000, 20000, 22000};
    public static InfectionManager infectionManager;

    private int lastDay = -1;


    @Override
    public void onInitialize() {
        OTItems.initialize();
        OTBlocks.initialize();
        OTEntities.init();
        OTParticles.register();
        OTFluids.register();
        OTPotions.init();
        OTDamageTypes.init();
        Registry.register(Registries.ITEM_GROUP, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
        GoatHornListener.register();

        RitualDataManager.load();

        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(OTBlocks.RITUAL_TABLE.asItem());
            itemGroup.add(OTBlocks.RITUAL_PEDESTAL.asItem());
            itemGroup.add(OTBlocks.UMBRALITH.asItem());
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

            if (!falling.getBlockState().isOf(net.minecraft.block.Blocks.ANVIL)) return true;

            if (player.experienceLevel >= 1) {
                player.addExperienceLevels(-1);
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
                    if (player.experienceLevel >= 10) {
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
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier("originstransgender", "baldness"), BALDNESS);
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier("originstransgender", "shadow_game"), SHADOW_GAME);
        Registry.register(Registries.STATUS_EFFECT,
                new Identifier("originstransgender", "sins_oblivion"), SINS_OBLIVION);

        BrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Items.CARVED_PUMPKIN, OTPotions.SINS_OBLIVION_POTION);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            HallucinateAll.register(dispatcher);
            Hallucinate.register(dispatcher);
            SendRawMSG.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            EnderianExperienceHandler.register(server);
            infectionManager = InfectionManager.get(server.getOverworld(), OTBlocks.UMBRALITH);
            infectionManager.start();
        });
    }

    private void onWorldTick(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay() % DAY_TICKS;
        int currentDay = (int) (world.getTimeOfDay() / DAY_TICKS);

        for (int trigger : ENDERMAN_TIMES) {
            if (timeOfDay == trigger && lastDay != currentDay) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(TARGET_PLAYER);
                if (player != null && player.isAlive()) {
                    spawnEndermanAroundPlayer(world, player);
                }
            }
        }

        for (int trigger : CHECK_TIMES) {
            if (timeOfDay == trigger) {
                if(world.random.nextBoolean()) {
                    checkPlayers(world);
                }
            }
        }

        if (timeOfDay == 0) {
            lastDay = currentDay;
        }


    }

    private void spawnEndermanAroundPlayer(ServerWorld world, ServerPlayerEntity player) {

        Random random = world.random;
        BlockPos basePos = player.getBlockPos();

        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 30;
            double offsetZ = (random.nextDouble() - 0.5) * 30;
            BlockPos spawnPos = basePos.add((int) offsetX, 0, (int) offsetZ);

            EndermanEntity enderman = EntityType.ENDERMAN.create(world);
            if (enderman != null) {
                enderman.refreshPositionAndAngles(spawnPos, random.nextFloat() * 360F, 0.0F);
                world.spawnEntity(enderman);
            }
        }
    }


    private void checkPlayers(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        for (ServerPlayerEntity player : players) {
            int nearby = countNearbyPlayers(players, player, 100);
            if (nearby >= 4) {
                player.sendMessage(Text.translatable("msg.reality_warp").formatted(Formatting.DARK_PURPLE), false);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 500, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 300, 2));
                Originstransgender.countTicker.put(player.getUuid(), 0);
                player.addStatusEffect(new StatusEffectInstance(Originstransgender.SHADOW_GAME, 600, 2));
            }
        }
    }

    private int countNearbyPlayers(List<ServerPlayerEntity> players, ServerPlayerEntity center, double radius) {
        Vec3d centerPos = center.getPos();
        int count = 0;
        for (ServerPlayerEntity other : players) {
            if (other == center) continue;
            if (centerPos.isInRange(other.getPos(), radius)) {
                count++;
            }
        }
        return count;
    }
}
