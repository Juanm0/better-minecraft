package name.modid.bag;

import java.util.List;
import name.modid.BetterMinecraft;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Registro de los sacos mejorados: componente de contenido, 3 items, receta especial de mejora y entradas del creativo. */
public final class BagMod {
	public static DataComponentType<List<ItemStack>> CONTENTS;
	public static BagItem GOLD_BAG;
	public static BagItem IRON_BAG;
	public static BagItem REINFORCED_IRON_BAG;
	public static RecipeSerializer<BagUpgradeRecipe> UPGRADE_SERIALIZER;

	private BagMod() {
	}

	public static void init() {
		CONTENTS = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			BetterMinecraft.id("bag_contents"),
			DataComponentType.<List<ItemStack>>builder().persistent(ItemStack.CODEC.listOf()).build()
		);
		GOLD_BAG = registerBag("gold_bag", BagTier.GOLD);
		IRON_BAG = registerBag("iron_bag", BagTier.IRON);
		REINFORCED_IRON_BAG = registerBag("reinforced_iron_bag", BagTier.REINFORCED_IRON);
		UPGRADE_SERIALIZER = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			BetterMinecraft.id("bag_upgrade"),
			new CustomRecipe.Serializer<>(BagUpgradeRecipe::new)
		);
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			entries.accept(GOLD_BAG);
			entries.accept(IRON_BAG);
			entries.accept(REINFORCED_IRON_BAG);
		});
	}

	private static BagItem registerBag(String name, BagTier tier) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BetterMinecraft.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new BagItem(new Item.Properties().setId(key).stacksTo(1), tier));
	}
}
