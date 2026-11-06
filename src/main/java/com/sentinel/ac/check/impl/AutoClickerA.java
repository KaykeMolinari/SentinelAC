package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;

import java.util.ArrayDeque;

/** CPS acima do limite (janela de 1 segundo). Thread de rede. */
public final class AutoClickerA extends Check {

    private final ArrayDeque<Long> clicks = new ArrayDeque<>();

    public AutoClickerA(PlayerData data) {
        super(data, "AutoClicker", "A");
    }

    @Override
    public void onAnimationPacket(long nowMs) {
        clicks.addLast(nowMs);
        while (!clicks.isEmpty() && nowMs - clicks.peekFirst() > 1000) {
            clicks.pollFirst();
        }

        int cps = clicks.size();
        if (cps > cfg("max-cps", 20)) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 3)) {
                flag("cps=" + cps);
                buffer = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.1);
        }
    }
}
