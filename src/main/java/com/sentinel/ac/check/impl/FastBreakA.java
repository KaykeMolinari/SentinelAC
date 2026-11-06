package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.Locale;

/**
 * Quebrar bloco rápido demais para a ferramenta/efeitos do jogador.
 * Compara o tempo entre o início da quebra e a quebra com o progresso esperado (Block#getBreakSpeed).
 */
public final class FastBreakA extends Check {

    private Location digPos;
    private long digStartMs;

    public FastBreakA(PlayerData data) {
        super(data, "FastBreak", "A");
    }

    @Override
    public void onBlockDamage(BlockDamageEvent event) {
        digPos = event.getBlock().getLocation();
        digStartMs = System.currentTimeMillis();
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            return;
        }

        Block block = event.getBlock();
        if (digPos == null || !digPos.equals(block.getLocation())) {
            return;
        }

        float speed = block.getBreakSpeed(p);
        if (speed <= 0f || speed >= 1.0f) { // inquebrável ou quebra instantânea
            digPos = null;
            return;
        }

        double expectedMs = (cfg("min-progress", 0.85) / speed) * 50.0;
        long elapsed = System.currentTimeMillis() - digStartMs;
        digPos = null;

        if (elapsed + cfg("tolerance-ms", 100) < expectedMs) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 2)) {
                flag(String.format(Locale.ROOT, "%s em %dms (mín. ~%.0fms)",
                        block.getType().name().toLowerCase(Locale.ROOT), elapsed, expectedMs));
                buffer = 0;
            }
            if (cfgBool("cancel-break", true)) {
                event.setCancelled(true);
            }
        } else {
            buffer = Math.max(0, buffer - 0.5);
        }
    }
}
