package name.modid.bag;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Receta especial (conserva el contenido):
 * - 8 lingotes de oro alrededor + bundle al centro = Saco de oro.
 * - 8 lingotes de hierro alrededor + Saco de oro al centro = Saco de hierro.
 * - Filas 1 y 3 de bloques de hierro; fila 2 = bundle, Saco de hierro, bundle = Saco de hierro mejorado (suma el contenido de los tres).
 */
public class BagUpgradeRecipe extends CustomRecipe {
	private record Match(Item result, BagTier tier, List<ItemStack> sources) {
	}

	public BagUpgradeRecipe() {
		super();
	}

	private static Match match(CraftingInput input) {
		if (input.width() != 3 || input.height() != 3) {
			return null;
		}
		ItemStack center = input.getItem(4);
		if (ring(input, Items.GOLD_INGOT) && center.is(ItemTags.BUNDLES)) {
			return new Match(BagMod.GOLD_BAG, BagTier.GOLD, List.of(center));
		}
		if (ring(input, Items.IRON_INGOT) && center.is(BagMod.GOLD_BAG)) {
			return new Match(BagMod.IRON_BAG, BagTier.IRON, List.of(center));
		}
		if (row(input, 0, Items.IRON_BLOCK) && row(input, 2, Items.IRON_BLOCK)
			&& input.getItem(3).is(ItemTags.BUNDLES) && center.is(BagMod.IRON_BAG) && input.getItem(5).is(ItemTags.BUNDLES)) {
			return new Match(BagMod.REINFORCED_IRON_BAG, BagTier.REINFORCED_IRON, List.of(center, input.getItem(3), input.getItem(5)));
		}
		return null;
	}

	private static boolean ring(CraftingInput input, Item item) {
		for (int i = 0; i < 9; i++) {
			if (i != 4 && !input.getItem(i).is(item)) {
				return false;
			}
		}
		return true;
	}

	private static boolean row(CraftingInput input, int row, Item item) {
		for (int col = 0; col < 3; col++) {
			if (!input.getItem(row * 3 + col).is(item)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return match(input) != null;
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
		Match match = match(input);
		if (match == null) {
			return ItemStack.EMPTY;
		}
		ItemStack result = new ItemStack(match.result());
		List<ItemStack> contents = new ArrayList<>();
		for (ItemStack source : match.sources()) {
			if (source.getItem() instanceof BagItem) {
				for (ItemStack s : BagContents.read(source)) {
					BagContents.insert(match.tier(), contents, s);
				}
			} else {
				BundleContents bundle = source.get(DataComponents.BUNDLE_CONTENTS);
				if (bundle != null) {
					BundleContents.Mutable mutable = new BundleContents.Mutable(bundle);
					ItemStack s = mutable.removeOne();
					while (!s.isEmpty()) {
						BagContents.insert(match.tier(), contents, s);
						s = mutable.removeOne();
					}
				}
			}
		}
		BagContents.write(result, match.tier(), contents);
		return result;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return BagMod.UPGRADE_SERIALIZER;
	}
}
