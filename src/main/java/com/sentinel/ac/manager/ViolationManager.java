package com.sentinel.ac.manager;

import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class ViolationManager {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final SentinelAC plugin;
    private final DiscordWebhookNotifier discord;

    public ViolationManager(SentinelAC plugin) {
        this.plugin = plugin;
        this.discord = new DiscordWebhookNotifier(plugin);
    }

    /** Pode ser chamado de qualquer thread. Alertas e punições rodam na thread principal. */
    public void flag(PlayerData data, Check check, String info) {
        if (data.bypass || !plugin.isEnabled()) {
            return;
        }
        final String key = check.configKey();
        final double vl = data.addVl(key, 1.0);
        final String playerName = data.player().getName();
        final String display = check.displayName();

        Bukkit.getScheduler().runTask(plugin, () -> {
            int max = check.maxVl(plugin.getConfig().getInt("punishments.default-max-vl", 20));
            String format = plugin.getConfig().getString("alerts.format",
                    "&8[&cSentinel&8] &f%player% &7falhou &c%check% &8(&7VL %vl%/%max%&8) &7%info%");
            String msg = format
                    .replace("%player%", playerName)
                    .replace("%check%", display)
                    .replace("%vl%", String.format(Locale.ROOT, "%.1f", vl))
                    .replace("%max%", String.valueOf(max))
                    .replace("%info%", info);
            Component component = LEGACY.deserialize(msg);

            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (!staff.hasPermission("sentinel.alerts")) {
                    continue;
                }
                PlayerData staffData = plugin.players().get(staff.getUniqueId());
                if (staffData == null || staffData.alerts) {
                    staff.sendMessage(component);
                }
            }
            Bukkit.getConsoleSender().sendMessage(component);
            discord.sendAlert(msg);

            if (vl >= max) {
                punish(data, check, playerName, display);
            }
        });
    }

    private void punish(PlayerData data, Check check, String playerName, String display) {
        List<String> commands = plugin.getConfig().getStringList("checks." + check.configKey() + ".commands");
        if (commands.isEmpty()) {
            commands = plugin.getConfig().getStringList("punishments.default-commands");
        }
        data.resetVl(check.configKey());
        for (String command : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", playerName).replace("%check%", display));
        }
    }

    public void startDecayTask() {
        long interval = Math.max(1, plugin.getConfig().getLong("vl-decay.interval-seconds", 60)) * 20L;
        double amount = plugin.getConfig().getDouble("vl-decay.amount", 1.0);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : plugin.players().all()) {
                data.decayVl(amount);
            }
        }, interval, interval);
    }
}
