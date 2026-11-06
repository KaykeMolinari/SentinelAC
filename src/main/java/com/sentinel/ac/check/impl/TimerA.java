package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.PlayerData;

import java.util.Locale;

/**
 * Timer: o cliente envia pacotes de movimento mais rápido que 20/s.
 * Cada pacote "gasta" 50ms; o tempo real "recarrega". Se o saldo passa do limite, flag.
 * O saldo negativo (lag) é limitado para não permitir um "banco" infinito de velocidade.
 * Thread de rede.
 */
public final class TimerA extends Check {

    private long lastNs;
    private double balance;
    private long seenTeleport;

    public TimerA(PlayerData data) {
        super(data, "Timer", "A");
    }

    @Override
    public void onFlyingPacket(FlyingInfo flying) {
        long now = System.nanoTime();
        long teleport = data.lastTeleportMs;

        if (lastNs == 0 || teleport != seenTeleport) {
            seenTeleport = teleport;
            lastNs = now;
            balance = 0;
            return;
        }

        double deltaMs = (now - lastNs) / 1_000_000.0;
        lastNs = now;

        balance += 50.0 - deltaMs;
        double bank = cfg("max-lag-bank-ms", 1500);
        if (balance < -bank) {
            balance = -bank;
        }

        if (balance > cfg("threshold-ms", 150)) {
            flag(String.format(Locale.ROOT, "saldo=%.0fms", balance));
            balance = 0;
        }
    }
}
