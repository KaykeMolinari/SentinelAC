package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import com.sentinel.ac.util.Util;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayDeque;

/** Muitos minérios quebrados em pouco tempo (mineração automática / "ore nuker"). */
public final class NukerB extends Check {

    private final ArrayDeque<Long> ores = new ArrayDeque<>();

    public NukerB(PlayerData data) {
        super(data, "Nuker", "B");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            return;
        }
        if (!Util.isOre(event.getBlock().getType())) {
            return;
        }

        long now = System.currentTimeMillis();
        long window = (long) (cfg("window-seconds", 5) * 1000);
        ores.addLast(now);
        while (!ores.isEmpty() && now - ores.peekFirst() > window) {
            ores.pollFirst();
        }

        if (ores.size() > cfg("max-ores", 14)) {
            flag("minérios=" + ores.size() + " em " + (window / 1000) + "s");
            ores.clear();
        }
    }
}
