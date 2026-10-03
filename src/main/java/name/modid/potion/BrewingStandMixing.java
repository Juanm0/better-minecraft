package name.modid.potion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import name.modid.BetterMinecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * Soporte para pociones (brewing stand) para las pociones MEZCLADAS de la cazuela (sin pocion base, solo efectos propios),
 * que el sistema de recetas de vanilla no reconoce:
 * - Polvora en el ingrediente: la pocion pasa a ser arrojable.
 * - Redstone en el ingrediente: +8 min a cada efecto, tope 16 min (si ningun efecto puede subir, no hace nada ni gasta redstone).
 * Sin Mixins: se rastrean los brewing stands cargados y se corre un temporizador propio de 20 s (no usa combustible).
 * Las pociones vanilla siguen con la logica de vanilla.
 */
public final class BrewingStandMixing {
	public static final int MAX_DURATION = 16 * 60 * 20;
	public static final int BONUS = 8 * 60 * 20;
	private static final int BREW_TICKS = 200;
	private static final int INGREDIENT_SLOT = 3;

	private static final List<BrewingStandBlockEntity> TRACKED = new ArrayList<>();
	private static final java.util.Set<BrewingStandBlockEntity> LOGGED = new java.util.HashSet<>();
	private static final Map<BrewingStandBlockEntity, Integer> PROGRESS = new HashMap<>();

	private BrewingStandMixing() {
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(BrewingStandMixing::tick);
		// Registro directo: al abrir un soporte se lo rastrea aunque el escaneo no lo haya encontrado todavia.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!level.isClientSide() && level.getBlockEntity(hit.getBlockPos()) instanceof BrewingStandBlockEntity stand && !TRACKED.contains(stand)) {
				TRACKED.add(stand);
			}
			return InteractionResult.PASS;
		});
	}

	/** Mensaje en la action bar al jugador mas cercano (<= 8 bloques). */
	private static void notifyNear(ServerLevel level, BlockPos pos, String text) {
		Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8.0, false);
		if (player instanceof ServerPlayer sp) {
			sp.sendSystemMessage(Component.literal(text), true);
		}
	}

	/** Cada segundo busca soportes para pociones en los chunks cercanos a los jugadores (sin depender de eventos de carga). */
	private static void scan(ServerLevel level) {
		TRACKED.removeIf(stand -> stand.isRemoved());
		for (ServerPlayer player : level.players()) {
			int pcx = player.blockPosition().getX() >> 4;
			int pcz = player.blockPosition().getZ() >> 4;
			for (int dx = -4; dx <= 4; dx++) {
				for (int dz = -4; dz <= 4; dz++) {
					LevelChunk chunk = level.getChunkSource().getChunkNow(pcx + dx, pcz + dz);
					if (chunk == null) {
						continue;
					}
					for (BlockEntity be : chunk.getBlockEntities().values()) {
						if (be instanceof BrewingStandBlockEntity stand && !be.isRemoved() && !TRACKED.contains(stand)) {
							TRACKED.add(stand);
						}
					}
				}
			}
		}
		PROGRESS.keySet().removeIf(stand -> !TRACKED.contains(stand));
		LOGGED.removeIf(stand -> !TRACKED.contains(stand));
	}

	private static void tick(ServerLevel level) {
		if (level.getGameTime() % 20 == 0) {
			scan(level);
		}
		if (TRACKED.isEmpty()) {
			return;
		}
		for (BrewingStandBlockEntity stand : new ArrayList<>(TRACKED)) {
			if (stand.getLevel() != level || stand.isRemoved()) {
				continue;
			}
			if (!canProcess(stand)) {
				PROGRESS.remove(stand);
				ItemStack ing = stand.getItem(INGREDIENT_SLOT);
				if ((ing.is(Items.GUNPOWDER) || ing.is(Items.REDSTONE)) && LOGGED.add(stand)) {
					notifyNear(level, stand.getBlockPos(), "Destiladora: no hay pociones mezcladas aplicables (ver latest.log)");
					BetterMinecraft.LOGGER.info("Soporte {}: ingrediente {} pero ninguna botella aplicable; slot0={} contenido={}",
						stand.getBlockPos(), ing.getItem(), stand.getItem(0), stand.getItem(0).get(DataComponents.POTION_CONTENTS));
				}
				continue;
			}
			int progress = PROGRESS.getOrDefault(stand, 0) + 1;
			if (progress == 1) {
				BetterMinecraft.LOGGER.info("Soporte de pociones en {} empezo a procesar pociones mezcladas", stand.getBlockPos());
				notifyNear(level, stand.getBlockPos(), "Destiladora: mezclando... (10 s)");
			}
			if (progress < BREW_TICKS) {
				PROGRESS.put(stand, progress);
				continue;
			}
			PROGRESS.remove(stand);
			process(level, stand);
		}
	}

	/** Hay ingrediente valido y al menos una botella que cambiaria. */
	private static boolean canProcess(BrewingStandBlockEntity stand) {
		ItemStack ingredient = stand.getItem(INGREDIENT_SLOT);
		boolean gunpowder = ingredient.is(Items.GUNPOWDER);
		if (!gunpowder && !ingredient.is(Items.REDSTONE)) {
			return false;
		}
		for (int i = 0; i < 3; i++) {
			if (convert(stand.getItem(i), gunpowder) != null) {
				return true;
			}
		}
		return false;
	}

	private static void process(ServerLevel level, BrewingStandBlockEntity stand) {
		ItemStack ingredient = stand.getItem(INGREDIENT_SLOT);
		boolean gunpowder = ingredient.is(Items.GUNPOWDER);
		boolean changed = false;
		for (int i = 0; i < 3; i++) {
			ItemStack result = convert(stand.getItem(i), gunpowder);
			if (result != null) {
				stand.setItem(i, result);
				changed = true;
			}
		}
		if (changed) {
			ingredient.shrink(1);
			if (ingredient.isEmpty()) {
				stand.setItem(INGREDIENT_SLOT, ItemStack.EMPTY);
			}
			stand.setChanged();
			BlockPos pos = stand.getBlockPos();
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.2F);
			notifyNear(level, pos, gunpowder ? "Destiladora: pociones arrojables listas" : "Destiladora: duracion aumentada");
		}
	}

	/** Devuelve la botella transformada, o null si no corresponde / no cambia nada. */
	private static ItemStack convert(ItemStack stack, boolean gunpowder) {
		if (!isMixed(stack)) {
			return null;
		}
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		if (gunpowder) {
			if (!stack.is(Items.POTION)) {
				return null; // ya es arrojable
			}
			ItemStack splash = new ItemStack(Items.SPLASH_POTION);
			splash.set(DataComponents.POTION_CONTENTS, contents);
			return splash;
		}
		boolean changed = false;
		PotionContents extended = PotionContents.EMPTY;
		for (MobEffectInstance effect : contents.getAllEffects()) {
			int duration = effect.getDuration();
			if (duration > 1 && duration < MAX_DURATION) {
				extended = extended.withEffectAdded(new MobEffectInstance(effect.getEffect(), Math.min(MAX_DURATION, duration + BONUS), effect.getAmplifier()));
				changed = true;
			} else {
				extended = extended.withEffectAdded(new MobEffectInstance(effect));
			}
		}
		if (!changed) {
			return null;
		}
		ItemStack out = stack.copy();
		out.setCount(1);
		out.set(DataComponents.POTION_CONTENTS, extended);
		return out;
	}

	/** Pocion o arrojable hecha por la cazuela: tiene efectos pero no pocion base de vanilla. */
	private static boolean isMixed(ItemStack stack) {
		if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION)) {
			return false;
		}
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		return contents != null && contents.potion().isEmpty() && contents.hasEffects();
	}
}
