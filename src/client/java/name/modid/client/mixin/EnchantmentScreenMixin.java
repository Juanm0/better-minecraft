package name.modid.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import name.modid.LapisTiers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Tooltip de la mesa: vanilla muestra "1/2/3 lapis y niveles" segun la fila. Con 4+ lapis el costo real
 * es otro (hoja + 2), asi que se reemplaza el numero de esas dos lineas. Es solo visual.
 *
 * Se engancha en cualquier metodo de la pantalla (regex) para no depender del nombre del metodo de
 * render en 26.3, y con require = 0: si no engancha, el juego arranca igual (solo queda el texto vanilla).
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
	@WrapOperation(
		method = "/.*/",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
		),
		require = 0
	)
	private MutableComponent bm$realCost(String key, Object[] args, Operation<MutableComponent> original) {
		boolean lapis = key.equals("container.enchant.lapis.one") || key.equals("container.enchant.lapis.many");
		boolean level = key.equals("container.enchant.level.one") || key.equals("container.enchant.level.many");
		if (lapis || level) {
			int real = bm$realCost();
			if (real > 0) {
				String fixed = lapis ? "container.enchant.lapis.many" : "container.enchant.level.many";
				return original.call(fixed, new Object[] {real});
			}
		}
		return original.call(key, args);
	}

	/** Costo real (lapis = niveles) segun el stack de lapis de la mesa abierta; -1 = dejar vanilla. */
	private static int bm$realCost() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || !(mc.player.containerMenu instanceof EnchantmentMenu menu)) {
			return -1;
		}
		ItemStack stack = menu.getSlot(1).getItem();
		int count = stack.is(Items.LAPIS_LAZULI) ? stack.getCount() : 0;
		if (!LapisTiers.isActive(count)) {
			return -1;
		}
		return LapisTiers.lapisCost(LapisTiers.sheet(count));
	}
}
