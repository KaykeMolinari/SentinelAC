package com.sentinel.ac.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.util.BoundingBox;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/** Utilidades de mundo/colisão. Só chamar na thread principal. */
public final class Util {

    private static final Set<Material> SPECIAL = EnumSet.of(
            Material.WATER, Material.LAVA, Material.COBWEB, Material.BUBBLE_COLUMN,
            Material.POWDER_SNOW, Material.SCAFFOLDING, Material.SWEET_BERRY_BUSH,
            Material.HONEY_BLOCK, Material.SLIME_BLOCK, Material.SEAGRASS,
            Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT);

    private Util() {
    }

    /** Caixa de colisão do jogador (largura/altura atuais) centrada em outra posição. */
    public static BoundingBox boxAt(Player p, org.bukkit.Location l) {
        BoundingBox bb = p.getBoundingBox();
        double hw = bb.getWidthX() / 2.0;
        double hd = bb.getWidthZ() / 2.0;
        double h = bb.getHeight();
        return new BoundingBox(l.getX() - hw, l.getY(), l.getZ() - hd, l.getX() + hw, l.getY() + h, l.getZ() + hd);
    }

    public static boolean anyBlock(World w, BoundingBox r, Predicate<Block> test) {
        int minX = (int) Math.floor(r.getMinX());
        int minY = (int) Math.floor(r.getMinY());
        int minZ = (int) Math.floor(r.getMinZ());
        int maxX = (int) Math.floor(r.getMaxX());
        int maxY = (int) Math.floor(r.getMaxY());
        int maxZ = (int) Math.floor(r.getMaxZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (!w.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (test.test(w.getBlockAt(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** Algum bloco sólido colide com esta região? */
    public static boolean collides(World w, BoundingBox region) {
        return anyBlock(w, region, b -> {
            if (b.getType().isAir() || b.isPassable()) {
                return false;
            }
            for (BoundingBox bb : b.getCollisionShape().getBoundingBoxes()) {
                BoundingBox shifted = bb.clone().shift(b.getX(), b.getY(), b.getZ());
                if (shifted.overlaps(region)) {
                    return true;
                }
            }
            return false;
        });
    }

    /** Há chão (bloco, barco ou shulker) logo abaixo da caixa? Tolerância de 0.5 bloco. */
    public static boolean onGround(Player p, BoundingBox box) {
        BoundingBox r = new BoundingBox(box.getMinX(), box.getMinY() - 0.5, box.getMinZ(),
                box.getMaxX(), box.getMinY() + 0.01, box.getMaxZ());
        if (collides(p.getWorld(), r)) {
            return true;
        }
        return !p.getWorld().getNearbyEntities(r, e -> e instanceof Boat || e instanceof Shulker).isEmpty();
    }

    public static boolean isSpecial(Block b) {
        Material m = b.getType();
        if (m.isAir()) {
            return false;
        }
        if (SPECIAL.contains(m) || Tag.CLIMBABLE.isTagged(m) || Tag.BEDS.isTagged(m) || b.isLiquid()) {
            return true;
        }
        return b.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged();
    }

    /** Líquido, escada, teia, slime, cama etc. dentro/abaixo da caixa. */
    public static boolean inSpecialMedium(World w, BoundingBox box) {
        BoundingBox r = box.clone().expand(0.1, 0.0, 0.1).expandDirectional(0.0, -0.6, 0.0);
        return anyBlock(w, r, Util::isSpecial);
    }

    public static boolean onIce(World w, BoundingBox box) {
        BoundingBox r = box.clone().expandDirectional(0.0, -0.6, 0.0);
        return anyBlock(w, r, b -> Tag.ICE.isTagged(b.getType()));
    }

    public static boolean isOre(Material m) {
        return m.name().endsWith("_ORE") || m == Material.ANCIENT_DEBRIS;
    }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
