package name.modid.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import name.modid.LapisTiers;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
	@Shadow
	@Final
	private Container enchantSlots;

	@Unique
	private boolean bm$pending = false;
	@Unique
	private int bm$lapisBefore;
	@Unique
	private int bm$extraLevels;
	@Unique
	private int bm$extraLapis;

	@Unique
	private int bm$lapisCount() {
		ItemStack lapis = this.enchantSlots.getItem(1);
		return lapis.is(Items.LAPIS_LAZULI) ? lapis.getCount() : 0;
	}

	/** Re-roll de los niveles requeridos: la hoja se mezcla en la semilla. */
	@ModifyArg(
		method = "lambda$slotsChanged$0",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"),
		index = 0
	)
	private long bm$seedForCosts(long seed) {
		return LapisTiers.seedFor(seed, LapisTiers.sheet(bm$lapisCount()));
	}

	/** Re-roll de los encantamientos de cada fila. */
	@ModifyArg(
		method = "getEnchantmentList",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"),
		index = 0
	)
	private long bm$seedForList(long seed) {
		return LapisTiers.seedFor(seed, LapisTiers.sheet(bm$lapisCount()));
	}

	/**
	 * Sube el nivel requerido (el numero grande que muestra la mesa) en cada hoja:
	 * 30, 31, 32... Las filas que vanilla deja vacias (valor menor a slot + 1) siguen vacias.
	 * El valor sincronizado con el cliente es este, asi que el rojo/blanco de la mesa es correcto.
	 */
	@WrapOperation(
		method = "lambda$slotsChanged$0",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"
		)
	)
	private int bm$scaleRequiredLevel(RandomSource random, int slot, int bookcases, ItemStack stack, Operation<Integer> original) {
		int cost = original.call(random, slot, bookcases, stack);
		int count = bm$lapisCount();
		if (!LapisTiers.isActive(count) || cost < slot + 1) {
			return cost;
		}
		return LapisTiers.requiredLevel(cost, LapisTiers.sheet(count));
	}

	/** Antes de encantar: hacen falta tantos niveles como lapis se van a gastar. */
	@Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
	private void bm$checkPayment(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
		this.bm$pending = false;
		if (id < 0 || id > 2 || player.hasInfiniteMaterials()) {
			return;
		}
		int count = bm$lapisCount();
		if (!LapisTiers.isActive(count)) {
			return; // 3 lapis o menos: vanilla puro
		}
		int consumed = LapisTiers.lapisCost(LapisTiers.sheet(count));
		if (player.experienceLevel < consumed) {
			cir.setReturnValue(false);
			return;
		}
		// Vanilla cobra (id + 1) niveles y (id + 1) lapis. La diferencia se cobra al final.
		this.bm$pending = true;
		this.bm$lapisBefore = count;
		this.bm$extraLevels = consumed - (id + 1);
		this.bm$extraLapis = consumed - (id + 1);
	}

	@Inject(method = "clickMenuButton", at = @At("RETURN"))
	private void bm$chargeExtra(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
		if (!this.bm$pending) {
			return;
		}
		this.bm$pending = false;
		if (!cir.getReturnValueZ()) {
			return;
		}
		if (bm$lapisCount() >= this.bm$lapisBefore) {
			return; // vanilla no cobro nada: no se encanto
		}
		if (this.bm$extraLevels > 0) {
			player.giveExperienceLevels(-this.bm$extraLevels);
		}
		ItemStack lapis = this.enchantSlots.getItem(1);
		if (this.bm$extraLapis > 0 && !lapis.isEmpty()) {
			lapis.shrink(Math.min(this.bm$extraLapis, lapis.getCount()));
		}
		this.enchantSlots.setChanged();
	}
}
