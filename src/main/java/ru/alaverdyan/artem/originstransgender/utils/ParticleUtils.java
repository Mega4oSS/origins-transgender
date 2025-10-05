// ParticleUtils.java
package ru.alaverdyan.artem.originstransgender.utils;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class ParticleUtils {
    public static void spawnRedDustCircle(ServerWorld world, BlockPos center, float radius, int points) {
        if (world == null) return;

        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.2;
        double cz = center.getZ() + 0.5;

        int pts = Math.max(8, points);
        DustParticleEffect effect = new DustParticleEffect(new Vector3f(1f, 0f, 0f), 1.0f);
        for (int i = 0; i < pts; i++) {
            double angle = 2.0 * Math.PI * i / pts;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double y = cy;

            world.spawnParticles(effect, x, y, z, 1, 0, 0, 0, 0);
            world.spawnParticles(effect, x, y, z, 1, 0, 0.5, 0, 0);
        }
    }
}
