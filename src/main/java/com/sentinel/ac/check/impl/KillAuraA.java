package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.FlyingInfo;
import com.sentinel.ac.data.PlayerData;

/**
 * Ataque sem balançar o braço. O cliente vanilla sempre manda o pacote de animação
 * no mesmo tick do ataque (antes ou depois dele, dependendo da versão).
 * Roda inteiro na thread de rede.
 */
public final class KillAuraA extends Check {

    private boolean swungThisTick;
    private boolean pendingAttack;

    public KillAuraA(PlayerData data) {
        super(data, "KillAura", "A");
    }

    @Override
    public void onAnimationPacket(long nowMs) {
        swungThisTick = true;
        pendingAttack = false;
    }

    @Override
    public void onAttackPacket(int entityId) {
        if (!swungThisTick) {
            pendingAttack = true;
        }
    }

    @Override
    public void onFlyingPacket(FlyingInfo flying) {
        if (pendingAttack) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 3)) {
                flag("ataque sem animação de braço");
                buffer = 0;
            }
        } else {
            buffer = Math.max(0, buffer - 0.05);
        }
        pendingAttack = false;
        swungThisTick = false;
    }
}
