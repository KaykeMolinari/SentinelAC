package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.MoveInfo;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Locale;

/** O cliente diz que está no chão (onGround=true), mas o servidor não vê chão por perto. */
public final class NoFallA extends Check {

    public NoFallA(PlayerData data) {
        super(data, "NoFall", "A");
    }

    @Override
    public void onMove(PlayerMoveEvent event, MoveInfo m) {
        Boolean claimed = m.claimedGround();
        if (claimed == null
                || data.isExempt()
                || data.recently(data.lastSpecialMs, 1500)
                || m.special()) {
            buffer = 0;
            return;
        }

        if (claimed && !m.onGround()) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 3)) {
                flag(String.format(Locale.ROOT, "onGround falso (dy=%.3f)", m.dy()));
                setback(event);
                buffer = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.5);
        }
    }
}
