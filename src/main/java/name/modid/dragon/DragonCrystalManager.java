package name.modid.dragon;

import java.util.List;
import name.modid.world.SecondaryPillarData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.AABB;

/** Crea los 10 End Crystals nuevos. No toca ni regenera los cristales vanilla. */
public final class DragonCrystalManager {
	private DragonCrystalManager() {
	}

	/** @return true cuando los 10 cristales existen (se llama de nuevo si faltan chunks). */
	public static boolean spawnAll(ServerLevel level) {
		List<SecondaryPillarData> pillars = SecondaryPillarData.compute(level.getSeed());
		for (SecondaryPillarData p : pillars) {
			if (!level.hasChunkAt(new BlockPos(p.x(), p.topY() + 1, p.z()))) {
				return false;
			}
		}
		for (SecondaryPillarData p : pillars) {
			BlockPos spot = new BlockPos(p.x(), p.topY() + 1, p.z());
			// No duplicar si ya hay uno.
			if (!level.getEntitiesOfClass(EndCrystal.class, new AABB(spot).inflate(0.6)).isEmpty()) {
				continue;
			}
			EndCrystal crystal = new EndCrystal(level, p.x() + 0.5, p.topY() + 1.0, p.z() + 0.5);
			crystal.setShowBottom(false);
			level.addFreshEntity(crystal);
		}
		return true;
	}
}
