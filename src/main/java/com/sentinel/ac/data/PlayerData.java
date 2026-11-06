package com.sentinel.ac.data;

import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.check.Check;
import com.sentinel.ac.check.CheckRegistry;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class PlayerData {

    private static final int MAX_QUEUE = 200;

    private final SentinelAC plugin;
    private final Player player;
    private volatile List<Check> checks = List.of();
    private final Map<String, Double> violations = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<FlyingInfo> flyingQueue = new ConcurrentLinkedDeque<>();

    public volatile boolean alerts = true;
    /** Permissão sentinel.bypass (lida pelas threads de pacote também). */
    public volatile boolean bypass;

    // --- estado de movimento (thread principal) ---
    public double lastDeltaY;
    public double lastHorizontal;
    public int airTicks;
    public long lastMoveMs;
    public boolean lastServerGround = true;
    public long lastIceMs;
    public long lastSpecialMs;

    // --- marcas de tempo (escritas na thread principal, lidas em qualquer uma) ---
    public volatile long lastTeleportMs = System.currentTimeMillis();
    public volatile long lastVelocityMs;
    public volatile long lastDamageMs;

    public PlayerData(SentinelAC plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        refreshBypass();
        rebuildChecks();
    }

    public SentinelAC plugin() {
        return plugin;
    }

    public Player player() {
        return player;
    }

    public List<Check> checks() {
        return checks;
    }

    public void rebuildChecks() {
        this.checks = List.copyOf(CheckRegistry.createFor(this));
    }

    public void refreshBypass() {
        this.bypass = player.hasPermission("sentinel.bypass");
    }

    /** Executa uma ação em cada check ativo, isolando exceções. */
    public void each(Consumer<Check> action) {
        for (Check check : checks) {
            if (!check.enabled()) {
                continue;
            }
            try {
                action.accept(check);
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Erro no check " + check.configKey(), t);
            }
        }
    }

    // ---------------- Exceções ----------------

    public boolean recently(long timestamp, long windowMs) {
        return System.currentTimeMillis() - timestamp < windowMs;
    }

    /** Somente thread principal. */
    public boolean isExempt() {
        if (bypass) {
            return true;
        }
        GameMode gm = player.getGameMode();
        return gm == GameMode.CREATIVE
                || gm == GameMode.SPECTATOR
                || player.getAllowFlight()
                || player.isFlying()
                || player.isGliding()
                || player.isRiptiding()
                || player.isInsideVehicle()
                || player.isDead()
                || recently(lastTeleportMs, 1500);
    }

    public void markTeleport(boolean exemptWindow) {
        if (exemptWindow) {
            lastTeleportMs = System.currentTimeMillis();
        }
        resetMovement();
    }

    public void resetMovement() {
        airTicks = 0;
        lastDeltaY = 0;
        lastHorizontal = 0;
        lastMoveMs = 0;
        lastServerGround = true;
    }

    // ---------------- Fila de pacotes de movimento ----------------

    public void offerFlying(FlyingInfo info) {
        flyingQueue.offerLast(info);
        while (flyingQueue.size() > MAX_QUEUE) {
            flyingQueue.pollFirst();
        }
    }

    /** Encontra o pacote que gerou este movimento (compara coordenadas). */
    public FlyingInfo matchFlying(Location to) {
        FlyingInfo f;
        while ((f = flyingQueue.pollFirst()) != null) {
            if (f.x() == to.getX() && f.y() == to.getY() && f.z() == to.getZ()) {
                return f;
            }
        }
        return null;
    }

    // ---------------- Violações ----------------

    public double addVl(String key, double amount) {
        return violations.merge(key, amount, Double::sum);
    }

    public void resetVl(String key) {
        violations.remove(key);
    }

    public void decayVl(double amount) {
        for (Map.Entry<String, Double> e : violations.entrySet()) {
            double next = e.getValue() - amount;
            if (next <= 0) {
                violations.remove(e.getKey());
            } else {
                violations.put(e.getKey(), next);
            }
        }
    }

    public Map<String, Double> vlSnapshot() {
        return new HashMap<>(violations);
    }
}
