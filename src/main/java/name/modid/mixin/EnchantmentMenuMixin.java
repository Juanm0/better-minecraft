package name.modid.mixin;

import name.modid.LapisTiers;
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

	/*
	 * Re-roll: la mesa siembra su generador aleatorio con la semilla del menu.
	 * Mezclamos la hoja en esa semilla, tanto al calcular los niveles requeridos
	 * como al elegir los encantamientos, asi cada cantidad de lapis da otro catalogo.
	 * (Fabric API apunta a este mismo lambda en 26.3, asi que el nombre es el correcto.)
	 */
	@ModifyArg(
		method = "lambda$slotsChanged$0",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"),
		index = 0
	)
	private long bm$seedForCosts(long seed) {
		return LapisTiers.seedFor(seed, LapisTiers.sheet(bm$lapisCount()));
	}

	@ModifyArg(
		method = "getEnchantmentList",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;setSeed(J)V"),
		index = 0
	)
	private long bm$seedForList(long seed) {
		return LapisTiers.seedFor(seed, LapisTiers.sheet(bm$lapisCount()));
	}

	/** Antes de encantar: verifica que el jugador tenga los niveles del costo de la hoja. */
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
		int sheet = LapisTiers.sheet(count);
		int levelCost = LapisTiers.levelCost(sheet, id);
		if (player.experienceLevel < levelCost) {
			cir.setReturnValue(false);
			return;
		}
		// Vanilla cobra (id + 1) niveles y (id + 1) lapis. La diferencia la cobramos al final.
		this.bm$pending = true;
		this.bm$lapisBefore = count;
		this.bm$extraLevels = levelCost - (id + 1);
		this.bm$extraLapis = LapisTiers.lapisCost(sheet) - (id + 1);
	}

	/** Despues de encantar: cobra la diferencia de niveles y de lapis. */
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
