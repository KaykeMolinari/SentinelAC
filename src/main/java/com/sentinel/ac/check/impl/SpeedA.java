package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Locale;

/** Velocidade horizontal acima do limite derivado do atributo de movimento. */
public final class SpeedA extends Check {

    public SpeedA(PlayerData data) {
        super(data, "Speed", "A");
    }

    @Override
    public void onMove(PlayerMoveEvent event, MoveInfo m) {
        if (data.isExempt()
                || data.recently(data.lastVelocityMs, 1500)
                || data.recently(data.lastDamageMs, 1000)
                || m.special()
                || data.recently(data.lastSpecialMs, 500)) {
            buffer = 0;
            return;
        }

        Player p = data.player();
        AttributeInstance attr = p.getAttribute(Attribute.MOVEMENT_SPEED);
        double base = attr != null ? attr.getValue() : 0.1;

        // Andando no chão: ~2.158 blocos/tick por unidade do atributo (já inclui sprint e Speed).
        double limit = base * 2.16 * cfg("multiplier", 1.0);
        if (!m.onGround() || !m.wasOnGround()) {
            limit += cfg("air-bonus", 0.22);
        }
        if (data.recently(data.lastIceMs, 1000)) {
            limit *= cfg("ice-multiplier", 2.5);
        }
        limit += 0.02;

        if (m.horizontal() > limit) {
            buffer++;
            if (buffer > cfg("buffer-limit", 3)) {
                flag(String.format(Locale.ROOT, "speed=%.3f limite=%.3f", m.horizontal(), limit));
                setback(event);
                buffer = cfg("buffer-limit", 3) / 2.0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.25);
        }
    }
}
