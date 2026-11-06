package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.PlayerData;

import java.util.Locale;

/** Valores impossíveis em pacotes de movimento (pitch fora de ±90, NaN, infinito). */
public final class BadPacketsA extends Check {

    public BadPacketsA(PlayerData data) {
        super(data, "BadPackets", "A");
    }

    @Override
    public void onFlyingPacket(FlyingInfo f) {
        if (f.hasRot()) {
            if (Float.isNaN(f.pitch()) || Float.isInfinite(f.pitch()) || Math.abs(f.pitch()) > 90.0f) {
                flag(String.format(Locale.ROOT, "pitch inválido: %s", f.pitch()));
                return;
            }
            if (Float.isNaN(f.yaw()) || Float.isInfinite(f.yaw())) {
                flag("yaw inválido");
                return;
            }
        }
        if (f.hasPos()) {
            if (!Double.isFinite(f.x()) || !Double.isFinite(f.y()) || !Double.isFinite(f.z())) {
                flag("posição não finita");
            }
        }
    }
}
