package name.modid.potion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * - Pocion con efectos sobre un caldero (vacio, con agua o de pociones): se vuelca y se mezclan los efectos.
 * - Botella de vidrio sobre la cazuela de pociones: sale UNA pocion con todos los efectos y el caldero queda vacio.
 * Solo servidor hace el trabajo; el cliente devuelve SUCCESS para no duplicar la accion.
 * No se toca ninguna interaccion vanilla del caldero (agua, lava, nieve): el evento solo actua con pociones con efectos.
 */
public final class PotionCauldronInteraction {
	private PotionCauldronInteraction() {
	}

	public static void init() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> onUse(player, level, hand, hit));
	}

	private static InteractionResult onUse(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		boolean isPotionCauldron = state.is(PotionMod.POTION_CAULDRON);
		boolean isBaseCauldron = state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON);
		if (!isPotionCauldron && !isBaseCauldron) {
			return InteractionResult.PASS;
		}

		ItemStack held = player.getItemInHand(hand);
		if (isPotion(held)) {
			List<MobEffectInstance> incoming = effectsOf(held);
			if (incoming.isEmpty()) {
				return InteractionResult.PASS; // agua u otra pocion sin efectos: vanilla
			}
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			List<MobEffectInstance> merged = incoming;
			if (isPotionCauldron && level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be) {
				merged = merge(be.getEffects(), incoming);
			}
			level.setBlock(pos, PotionMod.POTION_CAULDRON.defaultBlockState(), 3);
			if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be) {
				be.setEffects(merged);
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
			giveBack(player, held, new ItemStack(Items.GLASS_BOTTLE));
			return InteractionResult.SUCCESS;
		}

		if (isPotionCauldron && held.is(Items.GLASS_BOTTLE)) {
			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be) || be.getEffects().isEmpty()) {
				return InteractionResult.PASS;
			}
			PotionContents contents = PotionContents.EMPTY;
			for (MobEffectInstance effect : be.getEffects()) {
				contents = contents.withEffectAdded(new MobEffectInstance(effect));
			}
			ItemStack potion = new ItemStack(Items.POTION);
			potion.set(DataComponents.POTION_CONTENTS, contents);
			level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			giveBack(player, held, potion);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	/** Gasta 1 del item en mano (salvo creativo) y entrega el resultado al inventario o al piso. */
	private static void giveBack(Player player, ItemStack held, ItemStack result) {
		if (!player.hasInfiniteMaterials()) {
			held.shrink(1);
		}
		if (!player.getInventory().add(result)) {
			player.drop(result, false);
		}
	}

	private static boolean isPotion(ItemStack stack) {
		return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
	}

	private static List<MobEffectInstance> effectsOf(ItemStack stack) {
		PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
		List<MobEffectInstance> out = new ArrayList<>();
		if (contents != null) {
			for (MobEffectInstance effect : contents.getAllEffects()) {
				out.add(new MobEffectInstance(effect));
			}
		}
		return out;
	}

	/** Mismo efecto: gana el de mayor nivel; con igual nivel, el de mayor duracion. */
	private static List<MobEffectInstance> merge(List<MobEffectInstance> current, List<MobEffectInstance> incoming) {
		Map<Holder<MobEffect>, MobEffectInstance> map = new LinkedHashMap<>();
		for (MobEffectInstance effect : current) {
			map.put(effect.getEffect(), effect);
		}
		for (MobEffectInstance effect : incoming) {
			MobEffectInstance old = map.get(effect.getEffect());
			if (old == null
				|| effect.getAmplifier() > old.getAmplifier()
				|| (effect.getAmplifier() == old.getAmplifier() && effect.getDuration() > old.getDuration())) {
				map.put(effect.getEffect(), effect);
			}
		}
		return new ArrayList<>(map.values());
	}
}
