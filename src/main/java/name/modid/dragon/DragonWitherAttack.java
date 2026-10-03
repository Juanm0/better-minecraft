package name.modid.dragon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.phys.Vec3;

/**
 * Ataque especial de la fase 2: calavera de Wither (bola oscura, dano + efecto Wither)
 * que al impactar deja una nube de Wither. Solo los proyectiles que lanza este codigo
 * dejan nube; no se toca ningun proyectil ni nube del resto del juego.
 */
public final class DragonWitherAttack {
	private static final Map<UUID, Vec3> TRACKED = new HashMap<>();

	private DragonWitherAttack() {
	}

	public static void clear() {
		TRACKED.clear();
	}

	/** Se llama cada tick en fase 2: a veces lanza una calavera. */
	public static void maybeLaunch(ServerLevel level, EnderDragon dragon) {
		if (level.getGameTime() % 20 != 0) {
			return;
		}
		if (level.getRandom().nextDouble() >= DragonConfig.WITHER_ATTACK_CHANCE_PER_SECOND) {
			return;
		}
		Player target = level.getNearestPlayer(dragon, DragonConfig.WITHER_ATTACK_RANGE);
		if (target == null) {
			return;
		}
		Vec3 from = new Vec3(dragon.getX(), dragon.getY() + 1.0, dragon.getZ());
		Vec3 dir = target.getEyePosition().subtract(from).normalize();
		WitherSkull skull = new WitherSkull(level, dragon, dir);
		skull.setPos(from.x + dir.x * 4.0, from.y + dir.y * 4.0, from.z + dir.z * 4.0);
		level.addFreshEntity(skull);
		TRACKED.put(skull.getUUID(), skull.position());
	}

	/** Se llama siempre: cuando una calavera desaparece (impacto), deja la nube. */
	public static void tickProjectiles(ServerLevel level) {
		Iterator<Map.Entry<UUID, Vec3>> it = TRACKED.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Vec3> entry = it.next();
			Entity e = level.getEntity(entry.getKey());
			if (e != null && e.isAlive()) {
				entry.setValue(e.position());
				continue;
			}
			spawnCloud(level, entry.getValue());
			it.remove();
		}
	}

	private static void spawnCloud(ServerLevel level, Vec3 at) {
		AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
		cloud.setRadius(DragonConfig.WITHER_CLOUD_RADIUS);
		cloud.setDuration(DragonConfig.WITHER_CLOUD_DURATION);
		cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, DragonConfig.WITHER_EFFECT_TICKS, DragonConfig.WITHER_EFFECT_AMPLIFIER));
		level.addFreshEntity(cloud);
	}
}
