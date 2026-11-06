package com.sentinel.ac.check.impl;

import com.sentinel.ac.check.Check;
import com.sentinel.ac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Locale;

/** Bateu em alguém muito fora da direção para onde está olhando. */
public final class KillAuraC extends Check {

    public KillAuraC(PlayerData data) {
        super(data, "KillAura", "C");
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event) {
        Player attacker = data.player();
        Entity target = event.getEntity();
        if (target.getWorld() != attacker.getWorld()) {
            return;
        }

        Location eye = attacker.getEyeLocation();
        Vector toTarget = target.getBoundingBox().getCenter().subtract(eye.toVector());
        if (toTarget.lengthSquared() < 2.25) { // muito perto: o ângulo não é confiável
            return;
        }

        double angle = Math.toDegrees(eye.getDirection().angle(toTarget));
        if (Double.isNaN(angle)) {
            return;
        }

        if (angle > cfg("max-angle", 80.0)) {
            buffer++;
            if (buffer >= cfg("buffer-limit", 3)) {
                flag(String.format(Locale.ROOT, "ângulo=%.1f°", angle));
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
