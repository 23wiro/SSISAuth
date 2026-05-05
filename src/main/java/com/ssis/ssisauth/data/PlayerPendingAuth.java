package com.ssis.ssisauth.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerPendingAuth {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<AuthedPlayer>>(){}.getType();

    private static List<AuthedPlayer> entries = new ArrayList<>();
    private static Path savePath;

    public static void load(MinecraftServer server) {
        savePath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("data/ssisauth/PlayerPendingAuth.json");

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

    /*
    public static boolean contains(AuthedPlayer entry) {
        return entries.contains(entry);
    }

     */

    public static AuthedPlayer fetchByCode(String code) {
        return getAll().stream()
                .filter(ap -> ap.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    public static AuthedPlayer fetchByUUID(String UUID){
        return getAll().stream()
                .filter(ap -> ap.getUuid().equals(UUID))
                .findFirst()
                .orElse(null);
    }

    public static boolean playerPending(Player player) {
        return entries.stream().anyMatch(ap -> ap.getUuid().equals(player.getStringUUID()));
    }

    public static List<AuthedPlayer> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public static String generate6DigitCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (isCodeTaken(code));
        return code;
    }

    private static String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private static boolean isCodeTaken(String code) {
        return entries.stream().anyMatch(ap -> ap.getCode().equals(code));
    }
}