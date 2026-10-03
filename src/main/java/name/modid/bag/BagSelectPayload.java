package name.modid.bag;

import name.modid.BetterMinecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente -> servidor: "en el slot X, el saco tiene seleccionado el stack Y" (rueda del mouse). */
public record BagSelectPayload(int slot, int selected) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BagSelectPayload> TYPE = new CustomPacketPayload.Type<>(BetterMinecraft.id("bag_select"));
	public static final StreamCodec<FriendlyByteBuf, BagSelectPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, BagSelectPayload::slot,
		ByteBufCodecs.VAR_INT, BagSelectPayload::selected,
		BagSelectPayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
