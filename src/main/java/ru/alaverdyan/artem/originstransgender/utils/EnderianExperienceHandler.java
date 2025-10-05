package ru.alaverdyan.artem.originstransgender.utils;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class EnderianExperienceHandler {

    private static final String SAVE_KEY = "originstransgender_enderian_state_v1";
    private static EnderianState STATE;
    private static int tickCounter = 0;

    public static void register(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        STATE = overworld.getPersistentStateManager()
                .getOrCreate(EnderianState::fromNbt, EnderianState::new, SAVE_KEY);

        ServerTickEvents.END_SERVER_TICK.register(srv -> {
            tickCounter++;
            if (tickCounter % 20 == 0) {
                checkAdvancements(srv);
            }
            for (ServerWorld world : srv.getWorlds()) {
                if (world.getRegistryKey() == World.END) {
                    for (ServerPlayerEntity player : world.getPlayers()) {
                        tryHandlePlayerEnterEnd(player);
                    }
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, dmgSource) -> {
            if (entity instanceof EnderDragonEntity) {
                handleDragonDeath(entity.getServer());
            }
        });

        for (ServerWorld world : server.getWorlds()) {
            if (world.getRegistryKey() == World.END) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    tryHandlePlayerEnterEnd(player);
                }
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register(srv -> {
            if (STATE != null) STATE.markDirty();
        });
    }

    private static void tryHandlePlayerEnterEnd(ServerPlayerEntity player) {
        if (!isEnderian(player)) return;
        UUID uuid = player.getUuid();
        if (!STATE.entered.contains(uuid)) {
            STATE.entered.add(uuid);
            STATE.markDirty();
            player.sendMessage(Text.literal("Вы ощущаете лёгкое ощущение эйфории и комфорта. Вы чувствуете себя в безопасности и понимаете, что вы защищены."), false);
        }
    }

    private static void handleDragonDeath(MinecraftServer server) {
        if (STATE == null || STATE.dragonAnnounced) return;
        STATE.dragonAnnounced = true;
        STATE.markDirty();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isEnderian(player)) {
                player.sendMessage(Text.literal("Вы чувствуете резкую боль в груди. Это место больше не кажется вам таким же защищённым и комфортным, как прежде. Вы чувствуете, что вы совершенно одни в этой пустоте."), false);
            }
        }
    }

    private static void checkAdvancements(MinecraftServer server) {
        if (STATE == null) return;
        ServerWorld anyWorld = server.getOverworld();
        if (anyWorld == null) return;
        try {
            Advancement enterGateway = server.getAdvancementLoader().get(new Identifier("minecraft", "end/respawn_dragon"));
            Advancement dragonEgg = server.getAdvancementLoader().get(new Identifier("minecraft", "end/dragon_egg"));

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!isEnderian(player)) continue;
                if (!(player.getWorld().getRegistryKey() == World.END)) continue;
                UUID u = player.getUuid();

                if (enterGateway != null && !STATE.dejavuNotified.contains(u)) {
                    AdvancementProgress prog = player.getAdvancementTracker().getProgress(enterGateway);
                    if (prog != null && prog.isDone()) {
                        STATE.dejavuNotified.add(u);
                        STATE.markDirty();
                        player.sendMessage(Text.literal("Новый дракон никак не влияет на твоё ощущение. Окружающий тебя энд до сих пор пустой."), false);
                    }
                }

                if (dragonEgg != null && !STATE.newGenNotified.contains(u)) {
                    AdvancementProgress prog2 = player.getAdvancementTracker().getProgress(dragonEgg);
                    if (prog2 != null && prog2.isDone()) {
                        STATE.newGenNotified.add(u);
                        STATE.markDirty();
                        player.sendMessage(Text.literal("Что-то неуловимо родное и приятное ты ощущаешь от этого яйца... Правда, очень слабое..."), false);
                    }
                }
            }
        } catch (Throwable t) {
        }
    }

    private static boolean isEnderian(ServerPlayerEntity player) {
        try {
            Origin origin = OriginUtils.getPlayerOrigin(player, Identifier.of("origins", "origin"));
            if (origin != null && "enderian".equals(origin.getIdentifier().getPath())) return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static final class EnderianState extends PersistentState {
        final Set<UUID> entered = new HashSet<>();
        boolean dragonAnnounced = false;
        final Set<UUID> dejavuNotified = new HashSet<>();
        final Set<UUID> newGenNotified = new HashSet<>();

        public EnderianState() {
        }

        private EnderianState(NbtCompound nbt) {
            if (nbt.contains("entered")) {
                NbtList list = nbt.getList("entered", 8);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        entered.add(UUID.fromString(list.getString(i)));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (nbt.contains("dejavu")) {
                NbtList list = nbt.getList("dejavu", 8);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        dejavuNotified.add(UUID.fromString(list.getString(i)));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (nbt.contains("newgen")) {
                NbtList list = nbt.getList("newgen", 8);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        newGenNotified.add(UUID.fromString(list.getString(i)));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (nbt.contains("dragonAnnounced")) {
                dragonAnnounced = nbt.getBoolean("dragonAnnounced");
            }
        }

        public static EnderianState fromNbt(NbtCompound nbt) {
            return new EnderianState(nbt);
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt) {
            NbtList l1 = new NbtList();
            for (UUID u : entered) l1.add(NbtString.of(u.toString()));
            nbt.put("entered", l1);

            NbtList l2 = new NbtList();
            for (UUID u : dejavuNotified) l2.add(NbtString.of(u.toString()));
            nbt.put("dejavu", l2);

            NbtList l3 = new NbtList();
            for (UUID u : newGenNotified) l3.add(NbtString.of(u.toString()));
            nbt.put("newgen", l3);

            nbt.putBoolean("dragonAnnounced", dragonAnnounced);
            return nbt;
        }
    }
}
