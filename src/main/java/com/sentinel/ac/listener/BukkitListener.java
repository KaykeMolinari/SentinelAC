package com.sentinel.ac.listener;

import com.sentinel.ac.SentinelAC;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import com.sentinel.ac.util.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.BoundingBox;

public final class BukkitListener implements Listener {

    private final SentinelAC plugin;

    public BukkitListener(SentinelAC plugin) {
        this.plugin = plugin;
    }

    private PlayerData data(Player p) {
        return plugin.players().get(p.getUniqueId());
    }

    // ---------------- Ciclo de vida ----------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        plugin.players().add(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        plugin.players().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameMode(PlayerGameModeChangeEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d != null) {
            d.markTeleport(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null) {
            return;
        }
        // UNKNOWN é a causa usada quando um PlayerMoveEvent cancelado devolve o jogador (setback);
        // não pode dar imunidade, senão o trapaceiro ganha uma janela após cada setback.
        d.markTeleport(e.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d != null) {
            d.markTeleport(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d != null) {
            d.markTeleport(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVelocity(PlayerVelocityEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d != null) {
            d.lastVelocityMs = System.currentTimeMillis();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamaged(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            PlayerData d = data(p);
            if (d != null) {
                d.lastDamageMs = System.currentTimeMillis();
            }
        }
    }

    // ---------------- Movimento ----------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData d = data(p);
        if (d == null || !e.hasChangedPosition()) {
            return;
        }
        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null || from.getWorld() != to.getWorld()) {
            return;
        }

        long now = System.currentTimeMillis();
        BoundingBox toBox = Util.boxAt(p, to);

        FlyingInfo claim = d.matchFlying(to);
        boolean onGround = Util.onGround(p, toBox);
        boolean special = Util.inSpecialMedium(p.getWorld(), toBox);
        if (special) {
            d.lastSpecialMs = now;
        }
        if (Util.onIce(p.getWorld(), toBox)) {
            d.lastIceMs = now;
        }

        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double horizontal = Math.hypot(dx, dz);
        long dt = d.lastMoveMs == 0 ? Long.MAX_VALUE : now - d.lastMoveMs;

        d.airTicks = onGround ? 0 : d.airTicks + 1;

        MoveInfo move = new MoveInfo(from, to, dx, dy, dz, horizontal,
                onGround, d.lastServerGround, special, dt, d.lastDeltaY,
                claim == null ? null : claim.onGround());

        d.each(c -> c.onMove(e, move));

        if (e.isCancelled()) {
            d.resetMovement();
        } else {
            d.lastDeltaY = dy;
            d.lastHorizontal = horizontal;
            d.lastServerGround = onGround;
            d.lastMoveMs = now;
        }
    }

    // ---------------- Combate ----------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) {
            return;
        }
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        PlayerData d = data(p);
        if (d == null || d.bypass) {
            return;
        }
        d.each(c -> c.onAttack(e));
    }

    // ---------------- Blocos ----------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamage(BlockDamageEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null || d.bypass) {
            return;
        }
        d.each(c -> c.onBlockDamage(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        PlayerData d = data(e.getPlayer());
        if (d == null || d.bypass) {
            return;
        }
        d.each(c -> c.onBlockBreak(e));
    }
}
