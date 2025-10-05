package ru.alaverdyan.artem.originstransgender.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class OTParticles {
    public static final DefaultParticleType RED_DUST = FabricParticleTypes.simple();

    public static void register() {
        Registry.register(Registries.PARTICLE_TYPE, new Identifier("originstransgender", "red_dust"), RED_DUST);
    }
}
