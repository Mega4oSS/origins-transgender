package ru.alaverdyan.artem.originstransgender.registry;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

public class OTPotions {
    public static final Potion SINS_OBLIVION_POTION =
            Registry.register(
                    Registries.POTION,
                    Identifier.of(Originstransgender.MOD_ID, "sins_oblivion"),
                    new Potion("sins_oblivion",
                            new StatusEffectInstance(
                                    Originstransgender.SINS_OBLIVION,
                                    600,
                                    0)));
    public static void init() {
    }
}
