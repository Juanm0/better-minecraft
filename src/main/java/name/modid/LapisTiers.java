package name.modid;

/**
 * Reglas de la mesa de encantamientos: cantidad de lapislazuli -> "hoja" -> costos.
 *
 * - 3 lapis o menos: hoja 1, identica a vanilla.
 * - 4 lapis: hoja 2, 5 lapis: hoja 3, ... 22 lapis: hoja 20 (tope).
 * - Costo en niveles de la opcion i (0, 1, 2) en la hoja k: 3*(k-1) + i + 1
 *   (hoja 1: 1-2-3, hoja 2: 4-5-6, ... hoja 20: 58-59-60).
 * - Lapis que se gastan en las hojas 2 o mas: k + 2 (4 en la hoja 2, 22 en la hoja 20).
 */
public final class LapisTiers {
	public static final int VANILLA_LAPIS = 3;
	public static final int MAX_SHEET = 20;

	private LapisTiers() {
	}

	/** Hoja (1 a 20) segun los lapis que hay en la ranura. */
	public static int sheet(int lapisCount) {
		if (lapisCount <= VANILLA_LAPIS) {
			return 1;
		}
		return Math.min(MAX_SHEET, lapisCount - VANILLA_LAPIS + 1);
	}

	/** true si el mod cambia algo (mas de 3 lapis). */
	public static boolean isActive(int lapisCount) {
		return lapisCount > VANILLA_LAPIS;
	}

	/** Niveles de experiencia que cuesta la opcion {@code index} (0 a 2) en la hoja dada. */
	public static int levelCost(int sheet, int index) {
		return 3 * (sheet - 1) + index + 1;
	}

	/** Lapislazuli que se gastan al encantar en la hoja dada (solo hojas 2 o mas). */
	public static int lapisCost(int sheet) {
		return sheet + VANILLA_LAPIS - 1;
	}

	/** Semilla que usa la mesa en cada hoja: la hoja 1 conserva la de vanilla. */
	public static long seedFor(long seed, int sheet) {
		if (sheet <= 1) {
			return seed;
		}
		return seed ^ (long) ((sheet - 1) * 0x9E3779B1);
	}
}
