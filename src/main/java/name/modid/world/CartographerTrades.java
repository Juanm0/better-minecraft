package name.modid.world;

import java.util.Optional;
import name.modid.BetterMinecraft;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Los aldeanos cartografos venden un mapa a la End City flotante mas cercana (Overworld).
 * Sin sistema de ofertas de Fabric: al interactuar con un cartografo (servidor), si todavia no tiene la oferta, se agrega a sus ofertas
 * (se guarda con el aldeano). La profesion se lee por reflexion para no depender del paquete de Villager en 26.3.
 */
public final class CartographerTrades {
	private static final String MAP_NAME = "Mapa de End City flotante";

	private CartographerTrades() {
	}

	public static void init() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (level instanceof ServerLevel serverLevel && entity instanceof Merchant merchant && isCartographer(entity)) {
				try {
					addOffer(serverLevel, entity, merchant);
				} catch (Exception e) {
					BetterMinecraft.LOGGER.error("No se pudo agregar el mapa de End City al cartografo", e);
				}
			}
			return InteractionResult.PASS;
		});
	}

	private static boolean isCartographer(Entity entity) {
		try {
			Object data = entity.getClass().getMethod("getVillagerData").invoke(entity);
			Object profession = data.getClass().getMethod("profession").invoke(data);
			if (profession instanceof Holder<?> holder) {
				return holder.unwrapKey().map(key -> key.identifier().getPath().equals("cartographer")).orElse(false);
			}
		} catch (ReflectiveOperationException | RuntimeException ignored) {
			// no es un aldeano con profesion
		}
		return false;
	}

	private static void addOffer(ServerLevel level, Entity villager, Merchant merchant) {
		MerchantOffers offers = merchant.getOffers();
		for (MerchantOffer offer : offers) {
			Component name = offer.getResult().get(DataComponents.CUSTOM_NAME);
			if (name != null && MAP_NAME.equals(name.getString())) {
				return; // ya la tiene
			}
		}
		BlockPos city = FloatingEndCityGenerator.nearestCity(level.getSeed(), villager.getBlockX(), villager.getBlockZ());
		if (city == null) {
			return;
		}
		ItemStack map = MapItem.create(level, city.getX(), city.getZ(), (byte) 2, true, true);
		MapItem.renderBiomePreviewMap(level, map);
		MapItemSavedData.addTargetDecoration(map, city, "+", MapDecorationTypes.RED_X);
		map.set(DataComponents.CUSTOM_NAME, Component.literal(MAP_NAME));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 16), Optional.of(new ItemCost(Items.COMPASS, 1)), map, 0, 2, 25, 0.2F));
		BetterMinecraft.LOGGER.info("Cartografo en {} ahora vende mapa a la End City en {}", villager.blockPosition(), city);
	}
}
