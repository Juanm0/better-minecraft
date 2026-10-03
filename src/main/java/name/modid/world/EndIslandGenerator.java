package name.modid.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Agranda la isla central del End con un anillo de End Stone. Solo rellena aire
 * y salta las columnas donde ya hay terreno vanilla. Trabaja por tandas para no trabar el servidor.
 */
public final class EndIslandGenerator {
	private static final int INNER = 52;
	private static final int OUTER = 105;
	private static final int SURFACE_Y = 62;

	private final ServerLevel level;
	private final List<int[]> retry = new ArrayList<>();
	private int x = -OUTER;
	private int z = -OUTER;
	private boolean scanDone = false;

	public EndIslandGenerator(ServerLevel level) {
		this.level = level;
	}

	/** @return true cuando termino. */
	public boolean step(int budget) {
		if (!scanDone) {
			int done = 0;
			while (done < budget && !scanDone) {
				int cx = x;
				int cz = z;
				z++;
				if (z > OUTER) {
					z = -OUTER;
					x++;
					if (x > OUTER) {
						scanDone = true;
					}
				}
				done++;
				if (!column(cx, cz)) {
					retry.add(new int[] {cx, cz});
				}
			}
			return false;
		}
		List<int[]> copy = new ArrayList<>(retry);
		retry.clear();
		int n = 0;
		for (int[] c : copy) {
			if (n++ >= budget || !column(c[0], c[1])) {
				retry.add(c);
			}
		}
		return retry.isEmpty();
	}

	/** @return false si el chunk no esta cargado. */
	private boolean column(int cx, int cz) {
		double d = Math.sqrt((double) cx * cx + (double) cz * cz);
		if (d < INNER || d > OUTER) {
			return true;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		if (!level.hasChunkAt(pos.set(cx, SURFACE_Y, cz))) {
			return false;
		}
		for (int y = 72; y >= 50; y--) {
			if (!level.getBlockState(pos.set(cx, y, cz)).isAir()) {
				return true; // ya hay terreno de vanilla
			}
		}
		int h = (cx * 73856093) ^ (cz * 19349663);
		int top = SURFACE_Y + (((h & 3) == 0) ? 1 : 0);
		int thickness = Math.max(2, (int) (16 * (1.0 - (d - INNER) / (OUTER - INNER))) + ((h >> 2) & 1));
		for (int y = top - thickness; y <= top; y++) {
			pos.set(cx, y, cz);
			if (level.getBlockState(pos).isAir()) {
				level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 2);
			}
		}
		return true;
	}
}
