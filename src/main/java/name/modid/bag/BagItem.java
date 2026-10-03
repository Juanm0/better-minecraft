package name.modid.bag;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Saco con mas capacidad que el bundle. Misma interaccion que el bundle de vanilla:
 * - Clic derecho con un item en el cursor sobre el saco: lo mete. Clic derecho sobre el saco con el cursor vacio: saca un stack.
 * - Con el saco en el cursor, clic derecho sobre un item: lo mete; sobre un slot vacio: suelta un stack ahi.
 * - Clic derecho en el aire: vuelca todo al inventario.
 */
public class BagItem extends Item {
	private final BagTier tier;

	public BagItem(Properties properties, BagTier tier) {
		super(properties);
		this.tier = tier;
	}

	public BagTier tier() {
		return tier;
	}

	/** El saco esta en el cursor y se hace clic (derecho) sobre un slot. */
	@Override
	public boolean overrideStackedOnOther(ItemStack bag, Slot slot, ClickAction action, Player player) {
		if (action != ClickAction.SECONDARY) {
			return false;
		}
		List<ItemStack> list = BagContents.read(bag);
		ItemStack slotItem = slot.getItem();
		if (slotItem.isEmpty()) {
			if (list.isEmpty() || !slot.allowModification(player)) {
				return false;
			}
			ItemStack out = BagContents.removeLast(list);
			ItemStack leftover = slot.safeInsert(out);
			if (!leftover.isEmpty()) {
				list.add(leftover);
			}
			BagContents.write(bag, tier, list);
			player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F);
			return true;
		}
		if (!slot.allowModification(player)) {
			return false;
		}
		int moved = BagContents.insert(tier, list, slotItem);
		if (moved <= 0) {
			return false;
		}
		slotItem.shrink(moved);
		slot.setChanged();
		BagContents.write(bag, tier, list);
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F);
		return true;
	}

	/** El saco esta en un slot y se hace clic (derecho) con un item (o la mano vacia) en el cursor. */
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
			return false;
		}
		List<ItemStack> list = BagContents.read(bag);
		if (other.isEmpty()) {
			if (list.isEmpty()) {
				return false;
			}
			access.set(BagContents.removeLast(list));
			BagContents.write(bag, tier, list);
			player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F);
			return true;
		}
		int moved = BagContents.insert(tier, list, other);
		if (moved <= 0) {
			return false;
		}
		other.shrink(moved);
		BagContents.write(bag, tier, list);
		player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F);
		return true;
	}

	/** Clic derecho en el aire: vuelca todo el contenido al inventario (lo que no entra cae al piso). */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack bag = player.getItemInHand(hand);
		List<ItemStack> list = BagContents.read(bag);
		if (list.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			for (ItemStack stack : list) {
				player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
			}
			BagContents.write(bag, tier, List.of());
			player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F);
		}
		return InteractionResult.SUCCESS;
	}
}
