package name.modid.bag;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * Logica de contenido de los sacos (lista de ItemStack en el componente BagMod.CONTENTS).
 * Igual que el bundle de vanilla: el ultimo stack tocado queda primero (indice 0) y es el que
 * sale con clic derecho / se suelta, salvo que haya uno seleccionado con la rueda del mouse.
 */
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

	/** Guarda el contenido. Cualquier cambio de contenido borra la seleccion y la lore vieja. */
	public static void write(ItemStack bag, BagTier tier, List<ItemStack> list) {
		List<ItemStack> copy = new ArrayList<>();
		for (ItemStack s : list) {
			copy.add(s.copy());
		}
		bag.set(BagMod.CONTENTS, copy);
		bag.remove(BagMod.SELECTED);
		bag.remove(DataComponents.LORE); // los sacos de versiones anteriores traian una lista de texto
	}

	/** Indice seleccionado con la rueda (-1 = ninguno). */
	public static int selected(ItemStack bag) {
		Integer v = bag.get(BagMod.SELECTED);
		return v == null ? -1 : v;
	}

	public static void setSelected(ItemStack bag, int index) {
		if (index < 0) {
			bag.remove(BagMod.SELECTED);
		} else {
			bag.set(BagMod.SELECTED, index);
		}
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

	/** Cuanto del saco esta usado (stacks en los sacos por slots, peso en los demas). */
	public static int used(BagTier tier, List<ItemStack> list) {
		return tier.maxStacks() > 0 ? list.size() : weight(list);
	}

	public static int max(BagTier tier) {
		return tier.maxStacks() > 0 ? tier.maxStacks() : tier.maxWeight();
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
			int found = -1;
			for (int j = 0; j < list.size(); j++) {
				ItemStack s = list.get(j);
				if (ItemStack.isSameItemSameComponents(s, source) && s.getCount() < s.getMaxStackSize()) {
					found = j;
					break;
				}
			}
			ItemStack touched;
			if (found >= 0) {
				touched = list.remove(found);
				touched.grow(1);
			} else {
				if (tier.maxStacks() > 0 && list.size() >= tier.maxStacks()) {
					break;
				}
				touched = source.copyWithCount(1);
			}
			list.add(0, touched); // el ultimo tocado va primero, como en el bundle de vanilla
			weight += unit;
			moved++;
		}
		return moved;
	}

	/** Saca el stack seleccionado; si no hay seleccion, el primero (el ultimo que se coloco). */
	public static ItemStack take(List<ItemStack> list, int selected) {
		if (list.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int index = selected >= 0 && selected < list.size() ? selected : 0;
		return list.remove(index);
	}
}
