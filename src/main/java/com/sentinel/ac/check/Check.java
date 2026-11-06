package com.sentinel.ac.check;

import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base de todos os checks. Há uma instância por jogador.
 *
 * Threads: os hooks onFlyingPacket / onAnimationPacket / onAttackPacket rodam na thread
 * de rede (netty) e só devem usar dados próprios. Os demais rodam na thread principal.
 */
public abstract class Check {

    protected final PlayerData data;
    protected final SentinelAC plugin;
    private final String type;
    private final String variant;
    private final boolean enabled;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    protected double buffer;

    protected Check(PlayerData data, String type, String variant) {
        this.data = data;
        this.plugin = data.plugin();
        this.type = type;
        this.variant = variant;
        this.enabled = plugin.getConfig().getBoolean("checks." + configKey() + ".enabled", true);
    }

    public final String configKey() {
        return type + variant;
    }

    public final String displayName() {
        return type + " (" + variant + ")";
    }

    public final boolean enabled() {
        return enabled;
    }

    public final int maxVl(int def) {
        return (int) cfg("max-vl", def);
    }

    // ---- hooks (todos opcionais) ----

    /** Thread principal. */
    public void onMove(PlayerMoveEvent event, MoveInfo move) {
    }

    /** Thread de rede. */
    public void onFlyingPacket(FlyingInfo flying) {
    }

    /** Thread de rede. */
    public void onAnimationPacket(long nowMs) {
    }

    /** Thread de rede. */
    public void onAttackPacket(int entityId) {
    }

    /** Thread principal (dano jogador -> entidade por ataque corpo a corpo). */
    public void onAttack(EntityDamageByEntityEvent event) {
    }

    /** Thread principal. */
    public void onBlockDamage(BlockDamageEvent event) {
    }

    /** Thread principal. */
    public void onBlockBreak(BlockBreakEvent event) {
    }

    // ---- utilidades ----

    protected final void flag(String info) {
        plugin.violations().flag(data, this, info);
    }

    protected final void setback(PlayerMoveEvent event) {
        if (cfgBool("setback", true)) {
            event.setCancelled(true);
        }
    }

    private Object raw(String key, Object def) {
        return cache.computeIfAbsent(key,
                k -> plugin.getConfig().get("checks." + configKey() + "." + k, def));
    }

    protected final double cfg(String key, double def) {
        Object v = raw(key, def);
        return v instanceof Number n ? n.doubleValue() : def;
    }

    protected final boolean cfgBool(String key, boolean def) {
        Object v = raw(key, def);
        return v instanceof Boolean b ? b : def;
    }
}
