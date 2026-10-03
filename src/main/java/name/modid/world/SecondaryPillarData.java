package name.modid.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Los 10 pilares nuevos. Se calculan siempre igual a partir de la semilla del mundo,
 * asi no hace falta guardar posiciones: se pueden recalcular en cualquier momento.
 */
public record SecondaryPillarData(int x, int z, int radius, int topY, boolean caged) {
	public static final int COUNT = 10;

	public static List<SecondaryPillarData> compute(long worldSeed) {
		Random random = new Random(worldSeed ^ 0x5EC0D1A9L);
		int cagedA = random.nextInt(COUNT);
		int cagedB = random.nextInt(COUNT - 1);
		if (cagedB >= cagedA) {
			cagedB++;
		}
		List<SecondaryPillarData> out = new ArrayList<>();
		for (int i = 0; i < COUNT; i++) {
			// Segundo anillo, entre los pilares de vanilla (radio 42) y el borde de la isla.
			double angle = (Math.PI * 2.0 / COUNT) * i + 0.31 + (random.nextDouble() - 0.5) * 0.45;
			double dist = 60.0 + random.nextDouble() * 22.0;
			int x = (int) Math.round(Math.cos(angle) * dist);
			int z = (int) Math.round(Math.sin(angle) * dist);
			int radius = 3 + random.nextInt(3);
			int topY = 78 + random.nextInt(34);
			out.add(new SecondaryPillarData(x, z, radius, topY, i == cagedA || i == cagedB));
		}
		return out;
	}
}
