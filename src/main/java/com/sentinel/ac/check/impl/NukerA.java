package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayDeque;

/** Nuker: blocos demais por segundo ou mais de um bloco quebrado no mesmo tick. */
public final class NukerA extends Check {

    private final ArrayDeque<Long> times = new ArrayDeque<>();
    private int lastTick = -1;
    private int sameTick;

    public NukerA(PlayerData data) {
        super(data, "Nuker", "A");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            return;
        }

        long now = System.currentTimeMillis();
        times.addLast(now);
        while (!times.isEmpty() && now - times.peekFirst() > 1000) {
            times.pollFirst();
        }

        int tick = Bukkit.getCurrentTick();
        if (tick == lastTick) {
            sameTick++;
        } else {
            lastTick = tick;
            sameTick = 1;
        }

        boolean tooMany = times.size() > cfg("max-per-second", 14);
        boolean multi = sameTick > cfg("max-per-tick", 1);
        if (tooMany || multi) {
            flag(multi ? "vários blocos no mesmo tick (" + sameTick + ")"
                    : "blocos/s=" + times.size());
            if (cfgBool("cancel-break", true)) {
                event.setCancelled(true);
            }
        }
    }
}
