package name.modid.dragon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;

/** Version visual y sonora de la muerte del dragon, SIN matarlo. */
public final class DragonDeathSequence {
	private DragonDeathSequence() {
	}

	public static void tick(ServerLevel level, EnderDragon dragon, int t) {
		if (t == 0) {
			for (ServerPlayer p : level.players()) {
				level.playSound((Player) null, p.blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 5.0F, 1.0F);
			}
		}
		// Explosiones alrededor del dragon, como en la muerte vanilla.
		float ox = (level.getRandom().nextFloat() - 0.5F) * 8.0F;
		float oy = (level.getRandom().nextFloat() - 0.5F) * 4.0F;
		float oz = (level.getRandom().nextFloat() - 0.5F) * 8.0F;
		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, dragon.getX() + ox, dragon.getY() + 2.0 + oy, dragon.getZ() + oz, 1, 0.0, 0.0, 0.0, 0.0);

		// Tandas de XP repartidas durante la transicion (cada una una sola vez por tick exacto).
		int step = 20;
		if (t > 0 && t % step == 0) {
			int burst = t / step - 1;
			if (burst >= 0 && burst < DragonConfig.TRANSITION_XP_BURSTS) {
				DragonExperienceManager.awardBurst(level, dragon, burst);
			}
		}
	}
}
