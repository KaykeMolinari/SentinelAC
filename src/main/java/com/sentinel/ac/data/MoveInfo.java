package com.sentinel.ac.data;

import org.bukkit.Location;

/** Resumo de um movimento processado no PlayerMoveEvent. */
public record MoveInfo(
        Location from,
        Location to,
        double dx,
        double dy,
        double dz,
        double horizontal,
        /** Estimativa do servidor: há chão sob o jogador em "to". */
        boolean onGround,
        /** Estimativa do servidor no movimento anterior. */
        boolean wasOnGround,
        /** Dentro de água, lava, teia, escada, slime, cama etc. */
        boolean special,
        /** Milissegundos desde o movimento anterior (Long.MAX_VALUE se não há). */
        long dtMs,
        double lastDeltaY,
        /** onGround que o cliente declarou neste pacote (null se não foi possível casar). */
        Boolean claimedGround) {
}
