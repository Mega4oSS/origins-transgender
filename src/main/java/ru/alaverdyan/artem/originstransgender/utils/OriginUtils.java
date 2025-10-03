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

/**
 * Простая утилита для работы с Origins через публичный API (без reflection).
 */
public final class OriginUtils {

    private OriginUtils() {}

    /**
     * Установить origin игроку (синхронно, в том же потоке).
     * Бросает IllegalArgumentException, если layerId/originId некорректны.
     */
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
        // обычно sync() доступен — вызываем напрямую
        comp.sync();
    }

    /**
     * Установить origin игроку, но гарантированно выполняя на серверном потоке.
     * Используй, если вызываешь из асинхронного контекста.
     */
    public static void setPlayerOriginAsync(MinecraftServer server, ServerPlayerEntity player, Identifier layerId, Identifier originId) {
        Objects.requireNonNull(server, "server");
        server.execute(() -> setPlayerOrigin(player, layerId, originId));
    }

    /**
     * Вернуть текущий Origin игрока для указанного слоя.
     * Возвращает null, если компонент/слой/origin не доступны.
     */
    public static Origin getPlayerOrigin(ServerPlayerEntity player, Identifier layerId) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(layerId, "layerId");

        OriginComponent comp = ModComponents.ORIGIN.get(player);
        if (comp == null) return null;

        OriginLayer layer = OriginLayers.getLayer(layerId);
        if (layer == null) return null;

        // Предполагаем, что в данной версии API есть метод getOrigin(OriginLayer).
        // Если в твоей версии другое имя — замени на фактическое (getChosenOrigin / getSelectedOrigin и т.д.).
        return comp.getOrigin(layer);
    }

    /**
     * Установить origin по имени игрока (sync, если игрок найден).
     * Возвращает true, если игрок найден и origin установлен.
     */
    public static boolean setPlayerOriginByName(MinecraftServer server, String playerName, Identifier layerId, Identifier originId) {
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(playerName, "playerName");
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
        if (player == null) return false;
        setPlayerOrigin(player, layerId, originId);
        return true;
    }

    /**
     * Удобный wrapper для origins:human
     */
    public static void setHumanOrigin(ServerPlayerEntity player) {
        setPlayerOrigin(player, new Identifier("origins", "origin"), new Identifier("origins", "human"));
    }
}
