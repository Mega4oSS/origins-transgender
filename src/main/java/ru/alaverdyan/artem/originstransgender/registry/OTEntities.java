package ru.alaverdyan.artem.originstransgender.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.Originstransgender;
import ru.alaverdyan.artem.originstransgender.entities.FakeExperienceOrbEntity;

public class OTEntities {
    public static final EntityType<FakeExperienceOrbEntity> FAKE_XP_ORB = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Originstransgender.MOD_ID, "fake_xp_orb"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, (EntityType<FakeExperienceOrbEntity> type, World world) ->
                    new FakeExperienceOrbEntity(type, world)
            ).dimensions(EntityDimensions.fixed(0.25F, 0.25F)).build()
    );

    public static void init() {
    }
}
