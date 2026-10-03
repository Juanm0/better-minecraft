package name.modid.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Caldero con efectos mezclados. Las interacciones viven en PotionCauldronInteraction (evento de Fabric).
 * LEVEL = cuantas botellas rinde (1..3). COLOR = indice de PotionColors (el liquido se tine en el cliente con ese indice).
 */
public class PotionCauldronBlock extends BaseEntityBlock {
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 3);
	public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, PotionColors.count() - 1);

	public PotionCauldronBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 3).setValue(COLOR, PotionColors.MIXED));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL, COLOR);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PotionCauldronBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	/** Particulas del color del liquido sobre la superficie (solo cliente). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int rgb = PotionColors.rgb(state.getValue(COLOR));
		double surface = pos.getY() + (6.0 + 3.0 * state.getValue(LEVEL)) / 16.0;
		for (int i = 0; i < 2; i++) {
			double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
			double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
			level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, ARGB.opaque(rgb)), x, surface + 0.02, z, 0.0, 0.0, 0.0);
		}
	}
}
