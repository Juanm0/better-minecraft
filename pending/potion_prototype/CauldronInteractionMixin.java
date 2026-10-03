package name.modid.mixin;

import name.modid.potion.PotionCauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronInteraction.Dispatcher.class)
public abstract class CauldronInteractionMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void betterMinecraft$potionInteraction(ItemStack stack, CallbackInfoReturnable<CauldronInteraction> cir) {
        if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            cir.setReturnValue(PotionCauldronInteraction::interact);
        }
    }
}
