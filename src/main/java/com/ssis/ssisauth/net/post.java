package com.ssis.ssisauth.net;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import com.mojang.logging.LogUtils;
import com.ssis.ssisauth.secrets.secrets;
import org.slf4j.Logger;
import net.minecraft.world.entity.player.Player;

public class post {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static String EXTERNAL_SERVER_URL = "temp";
    private static String API_KEY = secrets.ApiKey;

    public static void postAuthCode(Player player, String code){
        try {
            String payload = "{\"uuid\":\"" + player.getStringUUID() + "\",\"code\":\"" + code + "\",\"timestamp\":" + System.currentTimeMillis() + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EXTERNAL_SERVER_URL))
                    .header("Content-Type", "application/jsoon")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .timeout(Duration.ofMillis(1000))
                    .build();

            HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            LOGGER.info("✓ Posted auth code for UUID: {}", player.getStringUUID());
                        } else {
                            LOGGER.warn("Failed to post auth code. Status: {}", response.statusCode());
                        }
                    })
                    .exceptionally(ex -> {
                        LOGGER.error("Error posting auth code", ex);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.error("Error preparing auth request", e);
        }
    }

}
