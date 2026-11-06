package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import com.sentinel.ac.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;

import java.util.Locale;

/** Gravidade: no ar, dy deve seguir (dy_anterior - 0.08) * 0.98. */
public final class FlyB extends Check {

    public FlyB(PlayerData data) {
        super(data, "Fly", "B");
    }

    @Override
    public void onMove(PlayerMoveEvent event, MoveInfo m) {
        Player p = data.player();
        if (data.isExempt()
                || data.recently(data.lastVelocityMs, 2000)
                || data.recently(data.lastDamageMs, 1000)
                || data.recently(data.lastSpecialMs, 1500)
                || m.onGround()
                || m.special()
                || data.airTicks < 4
                || m.dtMs() > 150
                || p.hasPotionEffect(PotionEffectType.LEVITATION)
                || p.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            buffer = 0;
            return;
        }

        // Bateu a cabeça no teto: a gravidade "quebra" nesse tick.
        BoundingBox box = Util.boxAt(p, m.to());
        BoundingBox above = new BoundingBox(box.getMinX(), box.getMaxY(), box.getMinZ(),
                box.getMaxX(), box.getMaxY() + 0.3, box.getMaxZ());
        if (Util.collides(p.getWorld(), above)) {
            return;
        }

        double predicted = (m.lastDeltaY() - 0.08) * 0.98;
        double diff = m.dy() - predicted;

        if (Math.abs(diff) > cfg("tolerance", 0.02)) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 4)) {
                flag(String.format(Locale.ROOT, "dy=%.4f esperado=%.4f", m.dy(), predicted));
                setback(event);
                buffer = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.5);
        }
    }
}
