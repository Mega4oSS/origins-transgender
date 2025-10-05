package ru.alaverdyan.artem.originstransgender.utils;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class OriginUtils {

    private OriginUtils() {}

    public static void setPlayerOrigin(ServerPlayerEntity player, Identifier layerId, Identifier originId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(layerId, "layerId");
        Objects.requireNonNull(originId, "originId");

        OriginComponent comp = ModComponents.ORIGIN.get(player);
        if (comp == null) {
            throw new IllegalStateException("Origin component is null for player " + player.getName());
        }

        OriginLayer layer = OriginLayers.getLayer(layerId);
        Origin origin = OriginRegistry.get(originId);

        if (layer == null) {
            throw new IllegalArgumentException("Unknown origin layer: " + layerId);
        }
        if (origin == null) {
            throw new IllegalArgumentException("Unknown origin: " + originId);
        }

        comp.setOrigin(layer, origin);
        comp.sync();
    }

    public static void setPlayerOriginAsync(MinecraftServer server, ServerPlayerEntity player, Identifier layerId, Identifier originId) {
        Objects.requireNonNull(server, "server");
        server.execute(() -> setPlayerOrigin(player, layerId, originId));
    }

    public static Origin getPlayerOrigin(ServerPlayerEntity player, Identifier layerId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(layerId, "layerId");

        OriginComponent comp = ModComponents.ORIGIN.get(player);
        if (comp == null) return null;

        OriginLayer layer = OriginLayers.getLayer(layerId);
        if (layer == null) return null;

        return comp.getOrigin(layer);
    }

    public static boolean setPlayerOriginByName(MinecraftServer server, String playerName, Identifier layerId, Identifier originId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerName, "playerName");
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
        if (player == null) return false;
        setPlayerOrigin(player, layerId, originId);
        return true;
    }

    public static void setHumanOrigin(ServerPlayerEntity player) {
        setPlayerOrigin(player, new Identifier("origins", "origin"), new Identifier("origins", "human"));
    }
}
