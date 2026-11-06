package com.sentinel.ac.data;

import com.sentinel.ac.SentinelAC;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerManager {

    private final SentinelAC plugin;
    private final Map<UUID, PlayerData> map = new ConcurrentHashMap<>();

    public PlayerManager(SentinelAC plugin) {
        this.plugin = plugin;
    }

    public PlayerData add(Player player) {
        PlayerData data = new PlayerData(plugin, player);
        map.put(player.getUniqueId(), data);
        return data;
    }

    public void remove(UUID id) {
        map.remove(id);
    }

    public PlayerData get(UUID id) {
        return map.get(id);
    }

    public Collection<PlayerData> all() {
        return map.values();
    }

    public void rebuildAll() {
        for (PlayerData data : map.values()) {
            data.refreshBypass();
            data.rebuildChecks();
        }
    }
}
