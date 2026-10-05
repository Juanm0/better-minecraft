package name.modid.bag;

import name.modid.BetterMinecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Cliente -> servidor: "el saco que esta en tal slot tiene seleccionado el stack Y" (rueda del mouse).
 * kind 0 = indice de slot del menu abierto (cofres, etc.); kind 1 = indice del inventario del jugador
 * (sirve tambien en el inventario creativo, cuyos slots no coinciden con los del servidor).
 */
public record BagSelectPayload(int kind, int index, int selected) implements CustomPacketPayload {
	public static final int MENU_SLOT = 0;
	public static final int INVENTORY_SLOT = 1;

	public static final CustomPacketPayload.Type<BagSelectPayload> TYPE = new CustomPacketPayload.Type<>(BetterMinecraft.id("bag_select"));
	public static final StreamCodec<FriendlyByteBuf, BagSelectPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, BagSelectPayload::kind,
		ByteBufCodecs.VAR_INT, BagSelectPayload::index,
		ByteBufCodecs.VAR_INT, BagSelectPayload::selected,
		BagSelectPayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
