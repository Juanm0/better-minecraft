package name.modid.dragon;

import java.util.List;
import java.util.UUID;
import name.modid.world.EndIslandGenerator;
import name.modid.world.EndPillarGenerator;
import name.modid.world.SecondaryPillarData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

/**
 * Orquesta la pelea. No modifica EnderDragonFight: solo lo lee.
 * Fase 1 = vanilla. Al 50 % de vida: transicion (una vez) y despues fase 2.
 */
public final class DragonFightManager {
	private static EndIslandGenerator islandJob;
	private static int pillarIndex = 0;
	private static boolean pillarsDone = false;

	private DragonFightManager() {
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(DragonFightManager::onLevelTick);
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof EnderDragon && entity.level() instanceof ServerLevel level && level.dimension() == Level.END) {
				DragonPhaseManager.resetFightState(level);
			}
		});
		// Durante la transicion del 50 % (iniciada y sin fase 2) el dragon no recibe dano: asi no muere antes de tiempo.
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof EnderDragon && entity.level() instanceof ServerLevel level && level.dimension() == Level.END) {
				return !(DragonPhaseManager.has(level, DragonPhaseManager.TRANSITION_STARTED)
					&& !DragonPhaseManager.has(level, DragonPhaseManager.PHASE2));
			}
			return true;
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
	}

	private static void reset() {
		islandJob = null;
		pillarIndex = 0;
		pillarsDone = false;
		DragonWitherAttack.clear();
	}

	private static void onLevelTick(ServerLevel level) {
		if (level.dimension() != Level.END) {
			return;
		}
		DragonWitherAttack.tickProjectiles(level);

		EnderDragonFight fight = level.getDragonFight();
		if (fight == null) {
			return;
		}
		UUID id = fight.dragonUUID();
		if (id == null || !(level.getEntity(id) instanceof EnderDragon dragon) || !dragon.isAlive()) {
			return;
		}

		// 1) Terreno: pilares nuevos y isla grande, desde el comienzo de la pelea.
		if (!DragonPhaseManager.has(level, DragonPhaseManager.TERRAIN_DONE)) {
			buildTerrain(level);
		}

		// 2) Disparo de la transicion: una sola vez (bit TRANSITION_STARTED persistente).
		if (!DragonPhaseManager.has(level, DragonPhaseManager.TRANSITION_STARTED)
			&& dragon.getHealth() <= dragon.getMaxHealth() * DragonConfig.PHASE2_HEALTH_FRACTION) {
			DragonPhaseManager.add(level, DragonPhaseManager.TRANSITION_STARTED);
			DragonPhaseManager.setTransitionTick(level, 0);
			// el dragon no recibe dano durante la transicion: ver el evento ALLOW_DAMAGE de init()
		}

		// 3) Transicion en curso.
		if (DragonPhaseManager.has(level, DragonPhaseManager.TRANSITION_STARTED)
			&& !DragonPhaseManager.has(level, DragonPhaseManager.PHASE2)) {
			int t = DragonPhaseManager.transitionTick(level);
			DragonDeathSequence.tick(level, dragon, t);
			if (t >= DragonConfig.TRANSITION_TICKS) {
				DragonPhaseManager.add(level, DragonPhaseManager.PHASE2);
			} else {
				DragonPhaseManager.setTransitionTick(level, t + 1);
			}
		}

		// 4) Fase 2: cristales nuevos (una vez) y ataque de Wither.
		if (DragonPhaseManager.isPhase2(level)) {
			if (!DragonPhaseManager.has(level, DragonPhaseManager.CRYSTALS_SPAWNED) && DragonCrystalManager.spawnAll(level)) {
				DragonPhaseManager.add(level, DragonPhaseManager.CRYSTALS_SPAWNED);
			}
			DragonWitherAttack.maybeLaunch(level, dragon);
		}
	}

	/** Un pilar por tick y despues la isla por tandas. */
	private static void buildTerrain(ServerLevel level) {
		if (!pillarsDone) {
			List<SecondaryPillarData> pillars = SecondaryPillarData.compute(level.getSeed());
			if (EndPillarGenerator.build(level, pillars.get(pillarIndex))) {
				pillarIndex++;
				if (pillarIndex >= pillars.size()) {
					pillarsDone = true;
				}
			}
			return;
		}
		if (islandJob == null) {
			islandJob = new EndIslandGenerator(level);
		}
		if (islandJob.step(DragonConfig.ISLAND_COLUMNS_PER_TICK)) {
			DragonPhaseManager.add(level, DragonPhaseManager.TERRAIN_DONE);
			islandJob = null;
		}
	}
}
