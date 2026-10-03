package name.modid.potion;

/** Paleta fija de colores para el liquido de la cazuela (el color vive en el BlockState, asi el cliente no necesita el BlockEntity). */
public final class PotionColors {
	/** Indice 0 = violeta (mezcla de 2+ pociones). */
	public static final int MIXED = 0;

	private static final int[] PALETTE = {
		0x8A2BE2, // 0 violeta (mezcla)
		0xE03030, // 1 rojo
		0xF08A20, // 2 naranja
		0xF0E030, // 3 amarillo
		0x90E030, // 4 lima
		0x30B030, // 5 verde
		0x20B0A0, // 6 turquesa
		0x33EBFF, // 7 cian
		0x5090F0, // 8 celeste
		0x3050E0, // 9 azul
		0xE030C0, // 10 magenta
		0xF090B0, // 11 rosa
		0x8A5030, // 12 marron
		0x909090, // 13 gris
		0x404050, // 14 oscuro
		0xF0F0F0  // 15 blanco
	};

	private PotionColors() {
	}

	public static int count() {
		return PALETTE.length;
	}

	/** Color RGB (sin alfa) del indice. */
	public static int rgb(int index) {
		return PALETTE[Math.floorMod(index, PALETTE.length)];
	}

	/** Indice de la paleta mas cercano a un color RGB. Se ignora el 0 (violeta) para que solo lo use la mezcla. */
	public static int nearest(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		int best = 1;
		long bestDist = Long.MAX_VALUE;
		for (int i = 1; i < PALETTE.length; i++) {
			int pr = (PALETTE[i] >> 16) & 0xFF;
			int pg = (PALETTE[i] >> 8) & 0xFF;
			int pb = PALETTE[i] & 0xFF;
			long d = (long) (r - pr) * (r - pr) + (long) (g - pg) * (g - pg) + (long) (b - pb) * (b - pb);
			if (d < bestDist) {
				bestDist = d;
				best = i;
			}
		}
		return best;
	}
}
