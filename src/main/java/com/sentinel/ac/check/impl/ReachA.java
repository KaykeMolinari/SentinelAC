package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import com.sentinel.ac.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Locale;

/** Distância do olho do atacante até a caixa de colisão do alvo maior que o alcance permitido. */
public final class ReachA extends Check {

    public ReachA(PlayerData data) {
        super(data, "Reach", "A");
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event) {
        Player attacker = data.player();
        GameMode gm = attacker.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR || attacker.isInsideVehicle()) {
            return;
        }
        Entity target = event.getEntity();
        if (target.getWorld() != attacker.getWorld()) {
            return;
        }

        Location eye = attacker.getEyeLocation();
        BoundingBox box = target.getBoundingBox();
        Vector closest = new Vector(
                Util.clamp(eye.getX(), box.getMinX(), box.getMaxX()),
                Util.clamp(eye.getY(), box.getMinY(), box.getMaxY()),
                Util.clamp(eye.getZ(), box.getMinZ(), box.getMaxZ()));
        double dist = eye.toVector().distance(closest);

        AttributeInstance attr = attacker.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        double range = attr != null ? attr.getValue() : 3.0;

        int ping = Math.min(attacker.getPing(), (int) cfg("max-ping", 250));
        double tolerance = cfg("tolerance", 0.4) + (ping / 50.0) * cfg("ping-factor", 0.1);

        if (dist > range + tolerance) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 2)) {
                flag(String.format(Locale.ROOT, "dist=%.2f max=%.2f ping=%d", dist, range + tolerance, ping));
                buffer = 0;
            }
            if (cfgBool("cancel-hit", true)) {
                event.setCancelled(true);
            }
        } else {
            buffer = Math.max(0, buffer - 0.25);
        }
    }
}
