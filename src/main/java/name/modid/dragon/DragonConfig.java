package name.modid.dragon;

/** Todos los numeros de balance en un solo lugar. */
public final class DragonConfig {
	private DragonConfig() {
	}

	/** La fase 2 empieza cuando la vida baja a esta fraccion. */
	public static final float PHASE2_HEALTH_FRACTION = 0.5F;
	/** Duracion de la transicion (en ticks). 200 = lo que dura la muerte vanilla. */
	public static final int TRANSITION_TICKS = 200;
	/** XP de la transicion = XP de muerte vanilla * esta fraccion. */
	public static final double TRANSITION_XP_FRACTION = 0.5;
	/** Cantidad de tandas de orbes durante la transicion. */
	public static final int TRANSITION_XP_BURSTS = 5;
	/** XP de muerte vanilla: primera vez / veces posteriores. */
	public static final int VANILLA_XP_FIRST_KILL = 12000;
	public static final int VANILLA_XP_LATER = 500;

	/** Multiplicador del dano fisico del dragon en la fase 2 (1.0 = vanilla). */
	public static final float PHASE2_DAMAGE_MULTIPLIER = 1.5F;

	/** Probabilidad, por segundo, de que el dragon lance el ataque de Wither en fase 2. */
	public static final double WITHER_ATTACK_CHANCE_PER_SECOND = 0.35;
	public static final double WITHER_ATTACK_RANGE = 110.0;
	public static final float WITHER_CLOUD_RADIUS = 3.0F;
	public static final int WITHER_CLOUD_DURATION = 200;
	public static final int WITHER_EFFECT_TICKS = 100;
	public static final int WITHER_EFFECT_AMPLIFIER = 1;

	/** Columnas de isla procesadas por tick. */
	public static final int ISLAND_COLUMNS_PER_TICK = 400;
}
