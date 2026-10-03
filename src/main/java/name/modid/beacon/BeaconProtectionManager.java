package name.modid.beacon;

import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * En el End, un jugador dentro del rango de un Beacon activo (piramide valida + rayo libre)
 * pierde el efecto Wither al instante. Todo del lado del servidor; no modifica el Beacon
 * ni el efecto Wither en general.
 */
public final class BeaconProtectionManager {
	private static final int MAX_RANGE = 50; // piramide de 4 niveles: 4 * 10 + 10

	private BeaconProtectionManager() {
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			if (level.dimension() != Level.END) {
				return;
			}
			for (ServerPlayer player : level.players()) {
				if (player.hasEffect(MobEffects.WITHER) && isProtected(level, player)) {
					player.removeEffect(MobEffects.WITHER);
				}
			}
		});
	}

	private static boolean isProtected(ServerLevel level, ServerPlayer player) {
		int pcx = player.blockPosition().getX() >> 4;
		int pcz = player.blockPosition().getZ() >> 4;
		int chunkRadius = MAX_RANGE / 16 + 1;
		for (int cx = pcx - chunkRadius; cx <= pcx + chunkRadius; cx++) {
			for (int cz = pcz - chunkRadius; cz <= pcz + chunkRadius; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
					BlockPos pos = entry.getKey();
					if (!entry.getValue().getBlockState().is(Blocks.BEACON)) {
						continue;
					}
					int levels = pyramidLevels(level, pos);
					if (levels <= 0 || !beamClear(level, pos)) {
						continue;
					}
					double range = levels * 10 + 10;
					// Mismo area que el Beacon de vanilla: rango en X/Z/hacia abajo, y hasta el techo.
					if (player.getX() >= pos.getX() - range && player.getX() <= pos.getX() + 1 + range
						&& player.getZ() >= pos.getZ() - range && player.getZ() <= pos.getZ() + 1 + range
						&& player.getY() >= pos.getY() - range) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Niveles de piramide (0 a 4) con bloques base validos, como en vanilla. */
	private static int pyramidLevels(ServerLevel level, BlockPos beacon) {
		int levels = 0;
		for (int layer = 1; layer <= 4; layer++) {
			int y = beacon.getY() - layer;
			if (y < level.getMinY()) {
				break;
			}
			for (int x = beacon.getX() - layer; x <= beacon.getX() + layer; x++) {
				for (int z = beacon.getZ() - layer; z <= beacon.getZ() + layer; z++) {
					if (!level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.BEACON_BASE_BLOCKS)) {
						return levels;
					}
				}
			}
			levels = layer;
		}
		return levels;
	}

	/** El rayo esta libre si ningun bloque opaco (salvo bedrock) lo tapa. */
	private static boolean beamClear(ServerLevel level, BlockPos beacon) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = beacon.getY() + 1; level.isInsideBuildHeight(y); y++) {
			BlockState state = level.getBlockState(pos.set(beacon.getX(), y, beacon.getZ()));
			if (state.canOcclude() && !state.is(Blocks.BEDROCK)) {
				return false;
			}
		}
		return true;
	}
}
