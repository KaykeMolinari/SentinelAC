package com.sentinel.ac.manager;

import com.sentinel.ac.SentinelAC;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Envia alertas ao Discord sem bloquear a thread principal do servidor. */
public final class DiscordWebhookNotifier {

    private final SentinelAC plugin;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public DiscordWebhookNotifier(SentinelAC plugin) {
        this.plugin = plugin;
    }

    public void sendAlert(String message) {
        String webhookUrl = plugin.getConfig().getString("alerts.discord.webhook-url", "").trim();
        if (webhookUrl.isEmpty()) {
            return;
        }

        try {
            String payload = "{\"content\":\"" + escapeJson(message) + "\",\"allowed_mentions\":{\"parse\":[]}}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(response -> {
                        if (response.statusCode() < 200 || response.statusCode() >= 300) {
                            plugin.getLogger().warning("Discord webhook respondeu HTTP " + response.statusCode() + ".");
                        }
                    })
                    .exceptionally(error -> {
                        plugin.getLogger().warning("Falha ao enviar alerta ao Discord: " + error.getMessage());
                        return null;
                    });
        } catch (IllegalArgumentException error) {
            plugin.getLogger().warning("URL do webhook do Discord inválida em alerts.discord.webhook-url.");
        }
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
