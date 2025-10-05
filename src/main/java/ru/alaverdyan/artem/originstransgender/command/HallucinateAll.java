package ru.alaverdyan.artem.originstransgender.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HallucinateAll {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("hallucinateAll")
                        .requires( cs -> cs.hasPermissionLevel(2))
                        .executes(HallucinateAll::executeHallucinateAll)
        );
    }

    public static int executeHallucinateAll(CommandContext<ServerCommandSource>  context) {
        try {
            for (ServerPlayerEntity player : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                player.sendMessage(Text.translatable("msg.reality_warp").formatted(Formatting.DARK_PURPLE), false);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 2));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 300, 2));
                Originstransgender.countTicker.put(player.getUuid(), 0);
                player.addStatusEffect(new StatusEffectInstance(Originstransgender.SHADOW_GAME, 300, 2));
            }
            context.getSource().sendFeedback(() -> Text.translatable("msg.effect_applied"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.translatable("msg.effect_failed"));
            return 0;
        }
    }
}
