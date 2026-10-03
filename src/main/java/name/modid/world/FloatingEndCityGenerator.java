package name.modid.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import name.modid.BetterMinecraft;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * End Cities de vanilla (con End Ships, Shulkers y Elytra) sobre islas flotantes de End Stone en el Overworld (Y 185..215).
 * Sin Mixins ni Structure propia: cuando se genera por primera vez el chunk "anfitrion" de una celda de SPACING chunks,
 * se encola una tarea; en el tick del nivel se arma la isla y se llama a EndCityPieces.startHouseTower(...) de vanilla
 * y se hace postProcess de cada pieza (asi vienen cofres, naves y shulkers).
 * Solo se decide al generar un chunk NUEVO, asi que no se repite al reiniciar el mundo.
 */
public final class FloatingEndCityGenerator {
	/** Una ciudad por celda de SPACING x SPACING chunks (20 = ~320 bloques). */
	private static final int SPACING = 20;
	private static final Map<ResourceKey<Level>, ArrayDeque<BlockPos>> PENDING = new HashMap<>();

	private FloatingEndCityGenerator() {
	}

	public static void init() {
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyGenerated) -> {
			if (!newlyGenerated || level.dimension() != Level.OVERWORLD) {
				return;
			}
			ChunkPos pos = chunk.getPos();
			int chunkX = pos.getMiddleBlockX() >> 4;
			int chunkZ = pos.getMiddleBlockZ() >> 4;
			if (isHost(level.getSeed(), chunkX, chunkZ)) {
				PENDING.computeIfAbsent(level.dimension(), k -> new ArrayDeque<>())
					.add(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
			}
		});
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			ArrayDeque<BlockPos> queue = PENDING.get(level.dimension());
			if (queue == null || queue.isEmpty()) {
				return;
			}
			BlockPos target = queue.poll();
			try {
				generate(level, target.getX(), target.getZ());
			} catch (Exception e) {
				BetterMinecraft.LOGGER.error("Fallo generando End City flotante en {}", target, e);
			}
		});
	}

	private static long hash(long seed, int sx, int sz) {
		long h = seed ^ (sx * 341873128712L) ^ (sz * 132897987541L) ^ 0x5DEECE66DL;
		h ^= (h >>> 33);
		h *= 0xff51afd7ed558ccdL;
		h ^= (h >>> 33);
		return h;
	}

	private static boolean isHost(long seed, int chunkX, int chunkZ) {
		int sx = Math.floorDiv(chunkX, SPACING);
		int sz = Math.floorDiv(chunkZ, SPACING);
		long h = hash(seed, sx, sz);
		int offX = 3 + (int) Math.floorMod(h, (long) (SPACING - 6));
		int offZ = 3 + (int) Math.floorMod(h >>> 20, (long) (SPACING - 6));
		return chunkX == sx * SPACING + offX && chunkZ == sz * SPACING + offZ;
	}

	private static void generate(ServerLevel level, int cx, int cz) {
		RandomSource random = RandomSource.create(hash(level.getSeed(), cx, cz));
		int top = 185 + random.nextInt(30);

		// cargar los chunks que va a tocar la ciudad (radio ~3 chunks)
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				level.getChunk((cx >> 4) + dx, (cz >> 4) + dz);
			}
		}

		buildIsland(level, cx, top, cz, random);

		BlockPos base = new BlockPos(cx, top + 1, cz);
		List<StructurePiece> pieces = new ArrayList<>();
		EndCityPieces.startHouseTower(templateManager(level), base, Rotation.getRandom(random), pieces, random);
		for (StructurePiece piece : pieces) {
			BoundingBox box = piece.getBoundingBox();
			piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), random, box,
				new ChunkPos(box.minX() >> 4, box.minZ() >> 4), base);
		}
		BetterMinecraft.LOGGER.info("End City flotante generada en {}", base);
	}

	/**
	 * Obtiene el StructureTemplateManager sin depender del nombre del getter (cambio entre versiones):
	 * busca por reflexion un metodo publico sin parametros que devuelva ese tipo, primero en el server y luego en el nivel.
	 */
	private static StructureTemplateManager templateManager(ServerLevel level) {
		Object[] holders = {level.getServer(), level};
		for (Object holder : holders) {
			for (java.lang.reflect.Method m : holder.getClass().getMethods()) {
				if (m.getParameterCount() == 0 && m.getReturnType() == StructureTemplateManager.class) {
					try {
						return (StructureTemplateManager) m.invoke(holder);
					} catch (ReflectiveOperationException e) {
						throw new IllegalStateException("No se pudo obtener StructureTemplateManager", e);
					}
				}
			}
		}
		throw new IllegalStateException("No hay metodo que devuelva StructureTemplateManager");
	}

	/** Isla en forma de cono invertido de End Stone, tope plano en y = top. */
	private static void buildIsland(ServerLevel level, int cx, int top, int cz, RandomSource random) {
		int radius = 26 + random.nextInt(8);
		int depth = 14;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy < depth; dy++) {
			double shrink = 1.0 - (double) dy / depth;
			double r = radius * Math.pow(shrink, 0.8);
			int ri = (int) Math.ceil(r);
			for (int dx = -ri; dx <= ri; dx++) {
				for (int dz = -ri; dz <= ri; dz++) {
					double wobble = 1.0 + (random.nextDouble() - 0.5) * 0.25;
					if (dx * dx + dz * dz <= r * r * wobble) {
						pos.set(cx + dx, top - dy, cz + dz);
						level.setBlock(pos, Blocks.END_STONE.defaultBlockState(), 2);
					}
				}
			}
		}
	}
}
