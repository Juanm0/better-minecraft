package name.modid.client;

import java.util.List;
import name.modid.potion.PotionCauldronBlock;
import name.modid.potion.PotionColors;
import name.modid.potion.PotionMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;

public class BetterMinecraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BagClient.init();
		// Tine el liquido de la cazuela (tintindex 0 del modelo water_cauldron) con el color guardado en el BlockState.
		BlockColorRegistry.register(List.of(new BlockTintSource() {
			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return color(state);
			}

			@Override
			public int color(BlockState state) {
				return ARGB.opaque(PotionColors.rgb(state.getValue(PotionCauldronBlock.COLOR)));
			}
		}), PotionMod.POTION_CAULDRON);
	}
}
