package com.ssis.ssisauth.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import net.minecraft.world.entity.player.Player;
import com.ssis.ssisauth.data.AuthedPlayer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuthedPlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<AuthedPlayer>>(){}.getType();

    private static List<AuthedPlayer> entries = new ArrayList<>();
    private static Path savePath;

    public static void load(MinecraftServer server) {
        savePath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("data/ssisauth/AuthedPlayers.json");

        if (!Files.exists(savePath)) {
            entries = new ArrayList<>();
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            entries = GSON.fromJson(reader, LIST_TYPE);
            LOGGER.info("Loaded {} entries from persistent list", entries.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load persistent list", e);
            entries = new ArrayList<>();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(savePath.getParent());
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(entries, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save persistent list", e);
        }
    }

    public static void add(AuthedPlayer entry) {
        entries.add(entry);
        save();
    }

    public static void remove(AuthedPlayer entry) {
        entries.remove(entry);
        save();
    }

    public static boolean contains(AuthedPlayer entry) {
        return entries.contains(entry);
    }

    public static boolean playerAuthed(Player player) {
        return entries.stream().anyMatch(ap -> ap.getUuid().equals(player.getStringUUID()));
    }

    public static List<AuthedPlayer> getAll() {
        return Collections.unmodifiableList(entries);
    }
}