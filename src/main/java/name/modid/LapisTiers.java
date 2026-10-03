package name.modid;

/**
 * Reglas de la mesa de encantamientos (v2).
 *
 * - 3 lapis o menos: hoja 1 = vanilla puro.
 * - 4 lapis = hoja 2, 5 lapis = hoja 3 ... 22 lapis = hoja 20 (tope, MAX_SHEET).
 * - Nivel requerido de cada fila = valor vanilla + (hoja - 1)  ->  30, 31, 32... en la fila 3.
 * - Al encantar se gastan tantos lapis como niveles de XP: hoja + 2 (4 en la hoja 2, 22 en la 20).
 * - El nivel requerido que muestra la mesa nunca es menor que lo que se va a cobrar,
 *   asi que lo que ves en la mesa es lo que realmente hace falta.
 */
public final class LapisTiers {
	public static final int VANILLA_LAPIS = 3;
	public static final int MAX_SHEET = 20;

	private LapisTiers() {
	}

	public static int sheet(int lapisCount) {
		if (lapisCount <= VANILLA_LAPIS) {
			return 1;
		}
		return Math.min(MAX_SHEET, lapisCount - VANILLA_LAPIS + 1);
	}

	public static boolean isActive(int lapisCount) {
		return lapisCount > VANILLA_LAPIS;
	}

	/** Lapis gastados = niveles de XP gastados (hojas 2 o mas). */
	public static int lapisCost(int sheet) {
		return sheet + VANILLA_LAPIS - 1;
	}

	/** Nivel requerido que se muestra, a partir del valor vanilla de la fila. */
	public static int requiredLevel(int vanillaRequired, int sheet) {
		return Math.max(vanillaRequired + (sheet - 1), lapisCost(sheet));
	}

	public static long seedFor(long seed, int sheet) {
		if (sheet <= 1) {
			return seed;
		}
		return seed ^ (long) ((sheet - 1) * 0x9E3779B1);
	}
}
