package name.modid.dragon;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/** Mas dano fisico del dragon contra jugadores, solo en fase 2. Sin Mixins. */
public final class DragonDamage {
	private static boolean applying = false;

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
				player.invulnerableTime = 0;
				player.hurtServer(level, source, extra);
			} finally {
				applying = false;
			}
		});
	}
}
