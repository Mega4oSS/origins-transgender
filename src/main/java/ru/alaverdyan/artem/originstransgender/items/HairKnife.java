package ru.alaverdyan.artem.originstransgender.items;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import ru.alaverdyan.artem.originstransgender.registry.OTItems;
import ru.alaverdyan.artem.originstransgender.Originstransgender;

public class HairKnife extends Item {
    public HairKnife(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            if(user.hasStatusEffect(Originstransgender.BALDNESS)) {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
            Originstransgender.usageMap.put(user.getUuid(), Originstransgender.usageMap.getOrDefault(user.getUuid(), 0) + 1);
            user.giveItemStack(new ItemStack(OTItems.TUFT_OF_HAIR));
            if(Originstransgender.usageMap.get(user.getUuid()) > 5) {
                user.sendMessage(Text.translatable("msg.baldness.started"), true);
                user.addStatusEffect(new StatusEffectInstance(
                        Originstransgender.BALDNESS,
                        20 * 30,
                        0,
                        false,
                        true
                ));
                Originstransgender.usageMap.put(user.getUuid(), 0);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }

}
