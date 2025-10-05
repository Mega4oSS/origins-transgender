package ru.alaverdyan.artem.originstransgender.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Hallucinate {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("hallucinate")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("target", EntityArgumentType.players())
                                .executes(Hallucinate::executeHallucinatePlayer))
        );
    }

    public static int executeHallucinatePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "target");

        for (ServerPlayerEntity player : targets) {
            player.sendMessage(Text.translatable("msg.reality_warp").formatted(Formatting.DARK_PURPLE), false);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 300, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 300, 2));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 300, 2));
            Originstransgender.countTicker.put(player.getUuid(), 0);
            player.addStatusEffect(new StatusEffectInstance(Originstransgender.SHADOW_GAME, 300, 2));
        }
        context.getSource().sendFeedback(() -> Text.translatable("msg.effect_targeted_applied"), true);
        return targets.size();
    }
}
