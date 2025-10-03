// ParticleUtils.java
package ru.alaverdyan.artem.originstransgender.utils;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

public class ParticleUtils {


    /**
     * Спавнит круг из красных dust-частиц вокруг центра блока.
     * @param world клиентский мир (ServerWorld)
     * @param center центр блока
     * @param radius радиус круга
     * @param points количество точек (чем больше, тем круг ровнее)
     */
    public static void spawnRedDustCircle(ServerWorld world, BlockPos center, float radius, int points) {
        if (world == null) return;

        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.2; // чуть выше блока
        double cz = center.getZ() + 0.5;

        int pts = Math.max(8, points);
        // создаём "dust" эффект с красным цветом
        DustParticleEffect effect = new DustParticleEffect(new Vector3f(1f, 0f, 0f), 1.0f);
        for (int i = 0; i < pts; i++) {
            double angle = 2.0 * Math.PI * i / pts;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double y = cy;

            // скорость = 0, частица статична
            world.spawnParticles(effect, x, y, z, 1, 0, 0, 0, 0);
            world.spawnParticles(effect, x, y, z, 1, 0, 0.5, 0, 0);
        }
    }
}
