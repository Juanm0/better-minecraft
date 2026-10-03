package name.modid.dragon;

import java.lang.reflect.Field;
import name.modid.BetterMinecraft;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/** Mas dano fisico del dragon contra jugadores, solo en fase 2. Sin Mixins. */
public final class DragonDamage {
	private static boolean applying = false;
	/** Campo privado Entity.invulnerableTime (no tiene setter publico en 26.3). Null si no se pudo abrir. */
	private static final Field INVULNERABLE_TIME = findInvulnerableTimeField();

	private static Field findInvulnerableTimeField() {
		try {
			Field f = Entity.class.getDeclaredField("invulnerableTime");
			f.setAccessible(true);
			return f;
		} catch (ReflectiveOperationException | RuntimeException e) {
			BetterMinecraft.LOGGER.warn("No se pudo acceder a Entity.invulnerableTime: el dano extra del dragon en fase 2 puede quedar bloqueado por los ticks de invulnerabilidad.", e);
			return null;
		}
	}

	private static void resetInvulnerableTime(Entity entity) {
		if (INVULNERABLE_TIME == null) {
			return;
		}
		try {
			INVULNERABLE_TIME.setInt(entity, 0);
		} catch (IllegalAccessException ignored) {
			// sin efecto: el dano extra puede ser ignorado esta vez
		}
	}

	private DragonDamage() {
	}

	public static void init() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (applying || blocked || damageTaken <= 0.0F) {
				return;
			}
			if (!(entity instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) {
				return;
			}
			if (!(source.getDirectEntity() instanceof EnderDragon) || !DragonPhaseManager.isPhase2(level)) {
				return;
			}
			float extra = damageTaken * (DragonConfig.PHASE2_DAMAGE_MULTIPLIER - 1.0F);
			if (extra <= 0.0F) {
				return;
			}
			applying = true;
			try {
				resetInvulnerableTime(player);
				player.hurtServer(level, source, extra);
			} finally {
				applying = false;
			}
		});
	}
}
