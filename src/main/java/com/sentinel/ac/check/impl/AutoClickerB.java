package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;

import java.util.ArrayDeque;
import java.util.Locale;

/** Cliques com intervalos consistentes demais (desvio padrão muito baixo). Thread de rede. */
public final class AutoClickerB extends Check {

    private final ArrayDeque<Long> deltas = new ArrayDeque<>();
    private long last;

    public AutoClickerB(PlayerData data) {
        super(data, "AutoClicker", "B");
    }

    @Override
    public void onAnimationPacket(long nowMs) {
        if (last != 0) {
            long delta = nowMs - last;
            if (delta < 500) {
                deltas.addLast(delta);
                int samples = (int) cfg("samples", 25);
                while (deltas.size() > samples) {
                    deltas.pollFirst();
                }
                if (deltas.size() == samples) {
                    evaluate();
                }
            } else {
                deltas.clear();
            }
        }
        last = nowMs;
    }

    private void evaluate() {
        double sum = 0;
        for (long d : deltas) {
            sum += d;
        }
        double mean = sum / deltas.size();

        double var = 0;
        for (long d : deltas) {
            var += (d - mean) * (d - mean);
        }
        double std = Math.sqrt(var / deltas.size());

        if (mean < cfg("max-mean-ms", 160) && std < cfg("min-std-ms", 10.0)) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 2)) {
                flag(String.format(Locale.ROOT, "média=%.0fms desvio=%.1fms", mean, std));
                buffer = 0;
                deltas.clear();
            }
        } else {
            buffer = Math.max(0, buffer - 0.5);
        }
    }
}
