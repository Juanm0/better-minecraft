package name.modid.dragon;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import name.modid.BetterMinecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

/**
 * XP de la transicion del 50 %. Se entregan ORBES fisicos (ExperienceOrb.award), igual que
 * la muerte vanilla. NO toca la XP de la muerte real.
 */
public final class DragonExperienceManager {
	private DragonExperienceManager() {
	}

	/** XP que el dragon suelta al morir en vanilla: 12000 la primera vez, 500 despues. */
	public static int vanillaDeathXp(EnderDragonFight fight) {
		return previouslyKilled(fight) ? DragonConfig.VANILLA_XP_LATER : DragonConfig.VANILLA_XP_FIRST_KILL;
	}

	public static int transitionTotal(EnderDragonFight fight) {
		return (int) Math.floor(vanillaDeathXp(fight) * DragonConfig.TRANSITION_XP_FRACTION);
	}

	/** Suelta la tanda numero {@code burst} (0..BURSTS-1) en la posicion del dragon. */
	public static void awardBurst(ServerLevel level, EnderDragon dragon, int burst) {
		int total = transitionTotal(level.getDragonFight());
		int each = total / DragonConfig.TRANSITION_XP_BURSTS;
		int amount = (burst == DragonConfig.TRANSITION_XP_BURSTS - 1)
			? total - each * (DragonConfig.TRANSITION_XP_BURSTS - 1)
			: each;
		if (amount > 0) {
			ExperienceOrb.award(level, dragon.position(), amount);
		}
	}

	// Se usa reflexion para no depender de si el metodo es publico o el campo privado.
	private static boolean previouslyKilled(EnderDragonFight fight) {
		if (fight == null) {
			return false;
		}
		try {
			Method m = EnderDragonFight.class.getMethod("hasPreviouslyKilledDragon");
			return (boolean) m.invoke(fight);
		} catch (ReflectiveOperationException ignored) {
			// seguimos con el campo
		}
		try {
			Field f = EnderDragonFight.class.getDeclaredField("previouslyKilled");
			f.setAccessible(true);
			return f.getBoolean(fight);
		} catch (ReflectiveOperationException e) {
			BetterMinecraft.LOGGER.warn("No se pudo saber si el dragon ya fue derrotado; se asume la primera vez (12000 XP).");
			return false;
		}
	}
}
