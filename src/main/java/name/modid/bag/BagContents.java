package name.modid.bag;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/** Logica de contenido de los sacos (lista de ItemStack en el componente BagMod.CONTENTS). */
public final class BagContents {
	private BagContents() {
	}

	public static List<ItemStack> read(ItemStack bag) {
		List<ItemStack> stored = bag.get(BagMod.CONTENTS);
		List<ItemStack> out = new ArrayList<>();
		if (stored != null) {
			for (ItemStack s : stored) {
				out.add(s.copy());
			}
		}
		return out;
	}

	public static void write(ItemStack bag, BagTier tier, List<ItemStack> list) {
		List<ItemStack> copy = new ArrayList<>();
		for (ItemStack s : list) {
			copy.add(s.copy());
		}
		bag.set(BagMod.CONTENTS, copy);
		refreshLore(bag, tier, copy);
	}

	/** Peso de un item suelto: 64 / tamano de stack (minimo 1). */
	public static int unitWeight(ItemStack stack) {
		return Math.max(1, 64 / Math.max(1, stack.getMaxStackSize()));
	}

	public static int weight(List<ItemStack> list) {
		int w = 0;
		for (ItemStack s : list) {
			w += s.getCount() * unitWeight(s);
		}
		return w;
	}

	/** No se meten sacos, bundles ni shulker boxes dentro de un saco. */
	public static boolean canInsert(ItemStack stack) {
		return !stack.isEmpty() && !(stack.getItem() instanceof BagItem) && !stack.is(ItemTags.BUNDLES) && !stack.is(ItemTags.SHULKER_BOXES);
	}

	/** Mete hasta source.getCount() items en la lista (sin tocar source). Devuelve cuantos entraron. */
	public static int insert(BagTier tier, List<ItemStack> list, ItemStack source) {
		if (!canInsert(source)) {
			return 0;
		}
		int unit = unitWeight(source);
		int weight = weight(list);
		int moved = 0;
		for (int i = 0; i < source.getCount(); i++) {
			if (tier.maxWeight() > 0 && weight + unit > tier.maxWeight()) {
				break;
			}
			boolean placed = false;
			for (ItemStack s : list) {
				if (ItemStack.isSameItemSameComponents(s, source) && s.getCount() < s.getMaxStackSize()) {
					s.grow(1);
					placed = true;
					break;
				}
			}
			if (!placed) {
				if (tier.maxStacks() > 0 && list.size() >= tier.maxStacks()) {
					break;
				}
				list.add(source.copyWithCount(1));
			}
			weight += unit;
			moved++;
		}
		return moved;
	}

	/** Saca el ultimo stack de la lista (vacio si no hay). */
	public static ItemStack removeLast(List<ItemStack> list) {
		if (list.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return list.remove(list.size() - 1);
	}

	public static void refreshLore(ItemStack bag, BagTier tier, List<ItemStack> list) {
		List<Component> lines = new ArrayList<>();
		String capacity = tier.maxStacks() > 0 ? list.size() + "/" + tier.maxStacks() + " slots" : weight(list) + "/" + tier.maxWeight();
		lines.add(Component.literal("Capacidad: " + capacity).withStyle(ChatFormatting.GRAY));
		int shown = 0;
		for (ItemStack s : list) {
			if (shown++ >= 6) {
				lines.add(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY));
				break;
			}
			lines.add(Component.literal("- ").append(s.getHoverName()).append(" x" + s.getCount()).withStyle(ChatFormatting.DARK_GRAY));
		}
		bag.set(DataComponents.LORE, new ItemLore(lines));
	}
}
