package com.ssis.ssisauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import com.ssis.ssisauth.data.AuthedPlayerList;
import com.ssis.ssisauth.data.AuthedPlayer;

public class aban {

    private static final SuggestionProvider<CommandSourceStack> AUTHED_PLAYERS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    AuthedPlayerList.getAll().stream()
                            .map(AuthedPlayer::getReal_name)
                            .toList(),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("aban")
                        .requires(source -> source.hasPermission(3))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(AUTHED_PLAYERS)
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String realName = StringArgumentType.getString(ctx, "player");
                                            String reason = StringArgumentType.getString(ctx, "reason");

                                            String minecraftName = AuthedPlayerList.getAll().stream()
                                                    .filter(ap -> ap.getReal_name().equals(realName))
                                                    .map(AuthedPlayer::getName)
                                                    .findFirst()
                                                    .orElse(null);

                                            if (minecraftName == null) {
                                                ctx.getSource().sendFailure(Component.literal("No authed player found with real name: " + realName));
                                                return 0;
                                            }

                                            return dispatcher.execute("ban " + minecraftName + " " + reason, ctx.getSource());
                                        })
                                )
                                .executes(ctx -> {
                                    String realName = StringArgumentType.getString(ctx, "player");

                                    String minecraftName = AuthedPlayerList.getAll().stream()
                                            .filter(ap -> ap.getReal_name().equals(realName))
                                            .map(AuthedPlayer::getName)
                                            .findFirst()
                                            .orElse(null);

                                    if (minecraftName == null) {
                                        ctx.getSource().sendFailure(Component.literal("No authed player found with real name: " + realName));
                                        return 0;
                                    }

                                    return dispatcher.execute("ban " + minecraftName, ctx.getSource());
                                })
                        )
        );
    }
}