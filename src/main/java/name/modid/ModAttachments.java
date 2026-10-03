package name.modid;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Datos persistentes guardados en el nivel del End (sobreviven a reinicios). */
public final class ModAttachments {
	/** Bits de estado de la pelea: ver DragonPhaseManager. */
	public static final AttachmentType<Integer> FIGHT_FLAGS =
		AttachmentRegistry.createPersistent(BetterMinecraft.id("fight_flags"), Codec.INT);
	/** Tick actual de la transicion del 50 %. */
	public static final AttachmentType<Integer> TRANSITION_TICK =
		AttachmentRegistry.createPersistent(BetterMinecraft.id("transition_tick"), Codec.INT);

	private ModAttachments() {
	}

	public static void init() {
		// Cargar la clase registra los attachments.
	}
}
