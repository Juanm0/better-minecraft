package name.modid.dragon;

import name.modid.ModAttachments;
import net.minecraft.server.level.ServerLevel;

/** Estado persistente de la pelea, guardado como bits en el nivel del End. */
public final class DragonPhaseManager {
	public static final int TERRAIN_DONE = 1;
	public static final int TRANSITION_STARTED = 2;
	public static final int PHASE2 = 4;
	public static final int CRYSTALS_SPAWNED = 8;

	private DragonPhaseManager() {
	}

	public static int flags(ServerLevel level) {
		Integer v = level.getAttached(ModAttachments.FIGHT_FLAGS);
		return v == null ? 0 : v;
	}

	public static boolean has(ServerLevel level, int bit) {
		return (flags(level) & bit) != 0;
	}

	public static void add(ServerLevel level, int bit) {
		level.setAttached(ModAttachments.FIGHT_FLAGS, flags(level) | bit);
	}

	public static int transitionTick(ServerLevel level) {
		Integer v = level.getAttached(ModAttachments.TRANSITION_TICK);
		return v == null ? 0 : v;
	}

	public static void setTransitionTick(ServerLevel level, int tick) {
		level.setAttached(ModAttachments.TRANSITION_TICK, tick);
	}

	public static boolean isPhase2(ServerLevel level) {
		return has(level, PHASE2);
	}

	public static DragonPhase current(ServerLevel level) {
		if (has(level, PHASE2)) {
			return DragonPhase.PHASE_2;
		}
		return has(level, TRANSITION_STARTED) ? DragonPhase.TRANSITION : DragonPhase.PHASE_1;
	}

	/** Cuando el dragon muere se limpia todo menos el terreno (los pilares quedan construidos). */
	public static void resetFightState(ServerLevel level) {
		level.setAttached(ModAttachments.FIGHT_FLAGS, flags(level) & TERRAIN_DONE);
		setTransitionTick(level, 0);
	}
}
