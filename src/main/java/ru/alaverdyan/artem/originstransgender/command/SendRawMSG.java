package ru.alaverdyan.artem.originstransgender.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SendRawMSG {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("send_message")
                        .requires(source -> ((ServerCommandSource)source).hasPermissionLevel(2)) // Operator only
                        .then(argument("target", EntityArgumentType.players())
                                .then(argument("message", MessageArgumentType.message())
                                        .executes(SendRawMSG::executeSendMessage)))
        );
    }

    public static int executeSendMessage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> targets = EntityArgumentType.getPlayers(context, "target");
        Text message = MessageArgumentType.getMessage(context, "message"); // This Text object can contain styling

        for (ServerPlayerEntity player : targets) {
            player.sendMessage(formatColorCodesText(message), false);
        }
        context.getSource().sendFeedback(() -> Text.translatable("msg.message_sended"), true);
        return targets.size(); // Return number of players who received the message
    }

    private static Text formatColorCodesText(Text message) {
        // Replace '&' with the section symbol '§'
        String formattedText = message.getString().replaceAll("&", "§");
        System.out.println(formattedText);
        return Text.of(formattedText);
    }
}
