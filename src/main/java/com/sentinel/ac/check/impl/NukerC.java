package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Locale;

/** Quebrou um bloco longe demais do jogador. */
public final class NukerC extends Check {

    public NukerC(PlayerData data) {
        super(data, "Nuker", "C");
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        GameMode gm = p.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) {
            return;
        }

        Block block = event.getBlock();
        if (block.getWorld() != p.getWorld()) {
            return;
        }

        AttributeInstance attr = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        double range = attr != null ? attr.getValue() : 4.5;
        double allowed = range + cfg("tolerance", 2.0);

        Location eye = p.getEyeLocation();
        double dist = eye.distance(block.getLocation().add(0.5, 0.5, 0.5));
        if (dist > allowed) {
            flag(String.format(Locale.ROOT, "dist=%.2f max=%.2f", dist, allowed));
            if (cfgBool("cancel-break", true)) {
                event.setCancelled(true);
            }
        }
    }
}
