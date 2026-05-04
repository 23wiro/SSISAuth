package com.ssis.ssisauth;

import com.ssis.ssisauth.data.AuthedPlayer;
import com.ssis.ssisauth.data.PlayerPendingAuth;
import com.ssis.ssisauth.deps.UUIDEncoder;
import com.ssis.ssisauth.net.Api;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import com.ssis.ssisauth.data.AuthedPlayerList;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@net.neoforged.fml.common.Mod(Mod.MODID)
public class Mod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ssisauth";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();



    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Mod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        // In your constructor
        // In your constructor, make sure this line exists:
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();

        if (AuthedPlayerList.playerAuthed(player)){

            player.refreshTabListName();


            AuthedPlayerList.getAll().stream()
                    .filter(ap -> ap.getUuid().equals(player.getStringUUID()))
                    .findFirst()
                    .ifPresent(ap -> {
                        if (ap.getReal_name() != null) {
                            Scoreboard scoreboard = player.getServer().getScoreboard();
                            String teamName = "auth_" + player.getStringUUID().substring(0, 8);

                            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                            if (team == null) {
                                team = scoreboard.addPlayerTeam(teamName);
                            }

                            team.setPlayerPrefix(Component.literal(ap.getReal_name() + " [" + ap.getUser_class() + "] "));
                            team.setNameTagVisibility(Team.Visibility.ALWAYS);
                            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
                        }
                    });

        } else if (PlayerPendingAuth.playerPending(player))  {
        AuthedPlayer pendingPlayer = PlayerPendingAuth.fetchByUUID(player.getStringUUID());
        player.connection.disconnect(Component.literal("Du måste logga in med din skolmejl på mc.ssis.nu. Skriv in denna kod: " + pendingPlayer.getCode()));

        } else {
            String code = UUIDEncoder.encode(player.getStringUUID());

            LOGGER.info("Generated auth code for {}: {}", player.getStringUUID(), code);



            player.connection.disconnect(Component.literal("Du måste logga in med din skolmejl på mc.ssis.nu. Skriv in denna kod: " + code));
            PlayerPendingAuth.add(new AuthedPlayer(player, null, null, code));
        }

    }


    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        AuthedPlayerList.load(event.getServer());
        PlayerPendingAuth.load(event.getServer());
        Api.start();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event){
        AuthedPlayerList.save();
        PlayerPendingAuth.save();
        Api.stop();
    }


    // Tab list
    @SubscribeEvent
    public void onTabListName(PlayerEvent.TabListNameFormat event) {
        AuthedPlayerList.getAll().stream()
                .filter(ap -> ap.getUuid().equals(event.getEntity().getStringUUID()))
                .findFirst()
                .ifPresent(ap -> {
                    if (ap.getReal_name() != null) {
                        event.setDisplayName(Component.literal(ap.getReal_name() + " [" + ap.getUser_class() + "]"));
                    }
                });
    }

    private void onRegisterCommands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        com.ssis.ssisauth.commands.aban.register(event.getDispatcher());
    }

}



