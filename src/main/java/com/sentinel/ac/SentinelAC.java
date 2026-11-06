package com.sentinel.ac;

import com.github.retrooper.packetevents.PacketEvents;
import com.sentinel.ac.command.SentinelCommand;
import com.sentinel.ac.data.PlayerManager;
import com.sentinel.ac.listener.BukkitListener;
import com.sentinel.ac.listener.PacketListener;
import com.sentinel.ac.manager.ViolationManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SentinelAC extends JavaPlugin {

    private PlayerManager players;
    private ViolationManager violations;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .reEncodeByDefault(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        players = new PlayerManager(this);
        violations = new ViolationManager(this);

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(this));
        getServer().getPluginManager().registerEvents(new BukkitListener(this), this);

        PluginCommand cmd = getCommand("sentinel");
        if (cmd != null) {
            SentinelCommand handler = new SentinelCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(p);
        }
        violations.startDecayTask();
        getLogger().info("SentinelAC ativado.");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public void reload() {
        reloadConfig();
        players.rebuildAll();
    }

    public PlayerManager players() {
        return players;
    }

    public ViolationManager violations() {
        return violations;
    }
}
