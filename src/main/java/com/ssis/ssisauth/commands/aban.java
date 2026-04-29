package com.ssis.ssisauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import com.ssis.ssisauth.data.AuthedPlayerList;
import com.ssis.ssisauth.data.AuthedPlayer;

public class aban {


    private static final SuggestionProvider<CommandSourceStack> AUTHED_PLAYERS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    AuthedPlayerList.getAll().stream()
                            .map(AuthedPlayer::getName)
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
                                            String player = StringArgumentType.getString(ctx, "player");
                                            String reason = StringArgumentType.getString(ctx, "reason");
                                            return dispatcher.execute("ban " + player + " " + reason, ctx.getSource());
                                        })
                                )
                                .executes(ctx -> {
                                    String player = StringArgumentType.getString(ctx, "player");
                                    return dispatcher.execute("ban " + player, ctx.getSource());
                                })
                        )
        );
    }
}
