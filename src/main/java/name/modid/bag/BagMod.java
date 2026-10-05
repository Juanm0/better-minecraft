package name.modid.bag;

import java.util.List;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import name.modid.BetterMinecraft;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Registro de los sacos mejorados: componente de contenido, 3 items, receta especial de mejora y entradas del creativo. */
public final class BagMod {
	public static DataComponentType<List<ItemStack>> CONTENTS;
	/** Stack seleccionado con la rueda del mouse (ausente = ninguno). */
	public static DataComponentType<Integer> SELECTED;
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
		SELECTED = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			BetterMinecraft.id("bag_selected"),
			DataComponentType.<Integer>builder().persistent(Codec.INT).build()
		);
		PayloadTypeRegistry.serverboundPlay().register(BagSelectPayload.TYPE, BagSelectPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(BagSelectPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			ItemStack stack = ItemStack.EMPTY;
			if (payload.kind() == BagSelectPayload.INVENTORY_SLOT) {
				if (payload.index() >= 0 && payload.index() < player.getInventory().getContainerSize()) {
					stack = player.getInventory().getItem(payload.index());
				}
			} else {
				AbstractContainerMenu menu = player.containerMenu;
				if (payload.index() >= 0 && payload.index() < menu.slots.size()) {
					stack = menu.getSlot(payload.index()).getItem();
				}
			}
			if (!(stack.getItem() instanceof BagItem)) {
				return;
			}
			int size = BagContents.read(stack).size();
			int selected = payload.selected();
			BagContents.setSelected(stack, selected >= 0 && selected < size ? selected : -1);
			player.containerMenu.broadcastChanges();
		});
		GOLD_BAG = registerBag("gold_bag", BagTier.GOLD);
		IRON_BAG = registerBag("iron_bag", BagTier.IRON);
		REINFORCED_IRON_BAG = registerBag("reinforced_iron_bag", BagTier.REINFORCED_IRON);
		UPGRADE_SERIALIZER = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			BetterMinecraft.id("bag_upgrade"),
			new RecipeSerializer<>(MapCodec.unit(BagUpgradeRecipe::new), StreamCodec.unit(new BagUpgradeRecipe()))
		);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
			tab.accept(GOLD_BAG);
			tab.accept(IRON_BAG);
			tab.accept(REINFORCED_IRON_BAG);
		});
	}

	private static BagItem registerBag(String name, BagTier tier) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BetterMinecraft.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new BagItem(new Item.Properties().setId(key).stacksTo(1), tier));
	}
}
