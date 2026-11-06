package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.PlayerData;

/** Multi-aura: atacar entidades diferentes dentro do mesmo tick. Thread de rede. */
public final class KillAuraB extends Check {

    private int firstTarget = -1;

    public KillAuraB(PlayerData data) {
        super(data, "KillAura", "B");
    }

    @Override
    public void onAttackPacket(int entityId) {
        if (firstTarget == -1) {
            firstTarget = entityId;
        } else if (firstTarget != entityId) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 2)) {
                flag("vários alvos no mesmo tick");
                buffer = 0;
            }
        }
    }

    @Override
    public void onFlyingPacket(FlyingInfo flying) {
        firstTarget = -1;
        buffer = Math.max(0, buffer - 0.02);
    }
}
