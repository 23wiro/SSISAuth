package com.ssis.ssisauth.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ssis.ssisauth.data.AuthedPlayer;
import com.ssis.ssisauth.data.AuthedPlayerList;
import com.ssis.ssisauth.data.PlayerPendingAuth;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Api {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String API_KEY = "your-secret-key";
    private static HttpServer server;

    public static void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/api/player", Api::handlePlayer);
            server.createContext("/api/auth", Api::handleAuth); // new
            server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
            server.start();
            LOGGER.info("Auth API server started on port 8080");
        } catch (IOException e) {
            LOGGER.error("Failed to start API server", e);
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("Auth API server stopped");
        }
    }

    private static boolean checkAuth(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer " + API_KEY)) {
            sendResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return false;
        }
        return true;
    }

    private static void handlePlayer(HttpExchange exchange) throws IOException {
        if (!checkAuth(exchange)) return;

        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            handleGetPlayer(exchange);
        } else if (method.equals("POST")) {
            handleUpdatePlayer(exchange);
        } else {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
        }
    }

    // GET /api/player?uuid=xxxx
    private static void handleGetPlayer(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("uuid=")) {
            sendResponse(exchange, 400, "{\"error\":\"Missing uuid parameter\"}");
            return;
        }

        String uuid = query.substring(5);
        Optional<AuthedPlayer> player = AuthedPlayerList.getAll().stream()
                .filter(ap -> ap.getUuid().equals(uuid))
                .findFirst();

        if (player.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            return;
        }

        sendResponse(exchange, 200, GSON.toJson(player.get()));
    }

    // POST /api/player
    // Body: { "uuid": "...", "real_name": "...", "user_class": "..." }
    private static void handleUpdatePlayer(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}");
            return;
        }

        if (!json.has("uuid")) {
            sendResponse(exchange, 400, "{\"error\":\"Missing uuid\"}");
            return;
        }

        String uuid = json.get("uuid").getAsString();
        Optional<AuthedPlayer> playerOpt = AuthedPlayerList.getAll().stream()
                .filter(ap -> ap.getUuid().equals(uuid))
                .findFirst();

        if (playerOpt.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            return;
        }

        AuthedPlayer player = playerOpt.get();
        if (json.has("real_name")) player.setReal_name(json.get("real_name").getAsString());
        if (json.has("user_class")) player.setUser_class(json.get("user_class").getAsString());

        AuthedPlayerList.save();
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    // POST /api/auth
    // Body: { "uuid": "...", "real_name": "...", "user_class": "..." }
    private static void handleAuth(HttpExchange exchange) throws IOException {
        if (!checkAuth(exchange)) return;

        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}");
            return;
        }

        if (!json.has("uuid") || !json.has("real_name") || !json.has("user_class")) {
            sendResponse(exchange, 400, "{\"error\":\"Missing required fields: uuid, real_name, user_class\"}");
            return;
        }

        String uuid = json.get("uuid").getAsString();
        String realName = json.get("real_name").getAsString();
        String userClass = json.get("user_class").getAsString();

        AuthedPlayer pending = PlayerPendingAuth.fetchByUUID(uuid);
        if (pending == null) {
            sendResponse(exchange, 404, "{\"error\":\"No pending auth for this UUID\"}");
            return;
        }

        pending.setReal_name(realName);
        pending.setUser_class(userClass);

        PlayerPendingAuth.remove(pending);
        AuthedPlayerList.add(pending);

        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}