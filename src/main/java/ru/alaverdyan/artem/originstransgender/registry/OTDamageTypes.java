package ru.alaverdyan.artem.originstransgender.registry;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

public class OTDamageTypes {
    public static final RegistryKey<DamageType> RITUAL_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Originstransgender.MOD_ID, "ritual"));

    public static void init() {

    }
}
