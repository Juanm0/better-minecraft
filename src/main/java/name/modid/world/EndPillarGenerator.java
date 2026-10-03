package name.modid.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Construye un pilar nuevo (obsidiana + bedrock arriba + jaula opcional). No toca los pilares de vanilla. */
public final class EndPillarGenerator {
	private static final int BOTTOM_Y = 20;

	private EndPillarGenerator() {
	}

	/** @return false si faltan chunks por cargar (se reintenta despues). */
	public static boolean build(ServerLevel level, SecondaryPillarData p) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int r = p.radius() + 3;
		int[][] corners = {{-r, -r}, {-r, r}, {r, -r}, {r, r}, {0, 0}};
		for (int[] c : corners) {
			if (!level.hasChunkAt(pos.set(p.x() + c[0], p.topY(), p.z() + c[1]))) {
				return false;
			}
		}
		int rad = p.radius();
		for (int dx = -rad; dx <= rad; dx++) {
			for (int dz = -rad; dz <= rad; dz++) {
				if (dx * dx + dz * dz > rad * rad + 1) {
					continue;
				}
				for (int y = BOTTOM_Y; y <= p.topY(); y++) {
					level.setBlock(pos.set(p.x() + dx, y, p.z() + dz), Blocks.OBSIDIAN.defaultBlockState(), 2);
				}
			}
		}
		level.setBlock(pos.set(p.x(), p.topY(), p.z()), Blocks.BEDROCK.defaultBlockState(), 2);

		if (p.caged()) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					for (int dy = 0; dy <= 3; dy++) {
						boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
						if (edge || dy == 3) {
							level.setBlock(pos.set(p.x() + dx, p.topY() + 1 + dy, p.z() + dz),
								Blocks.IRON_BARS.defaultBlockState(), 3);
						}
					}
				}
			}
		}
		return true;
	}
}
