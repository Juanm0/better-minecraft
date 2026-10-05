package name.modid.client;

import java.util.List;
import name.modid.BetterMinecraft;
import name.modid.bag.BagContents;
import name.modid.bag.BagItem;
import name.modid.bag.BagSelectPayload;
import name.modid.bag.BagTooltip;
import name.modid.client.mixin.AbstractContainerScreenAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Parte de cliente de los sacos: tooltip grafico y seleccion con la rueda del mouse (inventario normal y creativo). */
public final class BagClient {
	private static int logged = 0;

	private BagClient() {
	}

	public static void init() {
		ClientTooltipComponentCallback.EVENT.register(data -> data instanceof BagTooltip tooltip ? new ClientBagTooltip(tooltip) : null);
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?>) {
				ScreenMouseEvents.allowMouseScroll(screen).register(
					(s, mouseX, mouseY, horizontalAmount, verticalAmount) -> !onScroll(s, verticalAmount)
				);
			}
		});
	}

	/** @return true si la rueda se uso para elegir un item del saco (y no debe hacer otra cosa). */
	private static boolean onScroll(Screen screen, double verticalAmount) {
		if (!(screen instanceof AbstractContainerScreen<?> containerScreen) || verticalAmount == 0.0) {
			return false;
		}
		if (!containerScreen.getMenu().getCarried().isEmpty()) {
			return false;
		}
		Slot slot = ((AbstractContainerScreenAccessor) containerScreen).bm$getHoveredSlot();
		if (slot == null) {
			return false;
		}
		ItemStack stack = slot.getItem();
		if (!(stack.getItem() instanceof BagItem)) {
			return false;
		}
		List<ItemStack> list = BagContents.read(stack);
		if (list.isEmpty()) {
			return false;
		}
		int size = list.size();
		int current = BagContents.selected(stack);
		int direction = verticalAmount > 0 ? -1 : 1;
		int next;
		if (current < 0) {
			next = direction > 0 ? 0 : size - 1;
		} else {
			next = Math.floorMod(current + direction, size);
		}
		BagContents.setSelected(stack, next); // se ve al instante; el servidor lo confirma enseguida

		// En el inventario del jugador (y sobre todo en el creativo) los indices de la pantalla no coinciden con los del
		// servidor: se busca el saco en el inventario por identidad y se manda ese indice.
		int kind = BagSelectPayload.MENU_SLOT;
		int index = slot.index;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			Inventory inventory = mc.player.getInventory();
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				if (inventory.getItem(i) == stack) {
					kind = BagSelectPayload.INVENTORY_SLOT;
					index = i;
					break;
				}
			}
		}
		if (logged++ < 3) {
			BetterMinecraft.LOGGER.info("Saco: rueda sobre {} (tipo {}, indice {}) -> seleccion {} de {}", stack.getItem(), kind, index, next, size);
		}
		ClientPlayNetworking.send(new BagSelectPayload(kind, index, next));
		return true;
	}
}
