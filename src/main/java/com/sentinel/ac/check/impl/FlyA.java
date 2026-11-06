package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

/** Flutuar: ficar no ar com variação vertical quase nula por vários ticks. */
public final class FlyA extends Check {

    private int hoverTicks;

    public FlyA(PlayerData data) {
        super(data, "Fly", "A");
    }

    @Override
    public void onMove(PlayerMoveEvent event, MoveInfo m) {
        Player p = data.player();
        if (data.isExempt()
                || data.recently(data.lastVelocityMs, 1500)
                || data.recently(data.lastDamageMs, 1000)
                || data.recently(data.lastSpecialMs, 1500)
                || m.onGround()
                || m.special()
                || p.hasPotionEffect(PotionEffectType.LEVITATION)
                || p.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            hoverTicks = 0;
            return;
        }

        if (Math.abs(m.dy()) < 0.005) {
            hoverTicks++;
        } else {
            hoverTicks = 0;
        }

        if (hoverTicks >= cfg("hover-ticks", 8)) {
            flag(String.format(Locale.ROOT, "flutuando por %d ticks (dy=%.4f)", hoverTicks, m.dy()));
            setback(event);
            hoverTicks = 0;
        }
    }
}
