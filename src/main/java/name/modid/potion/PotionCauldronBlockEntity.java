package name.modid.potion;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Guarda los efectos mezclados; se persisten con ValueInput/ValueOutput. */
public class PotionCauldronBlockEntity extends BlockEntity {
	private static final Codec<List<MobEffectInstance>> EFFECTS_CODEC = MobEffectInstance.CODEC.listOf();

	private List<MobEffectInstance> effects = List.of();

	public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(PotionMod.POTION_CAULDRON_ENTITY, pos, state);
	}

	public List<MobEffectInstance> getEffects() {
		return effects;
	}

	public void setEffects(List<MobEffectInstance> effects) {
		this.effects = List.copyOf(effects);
		this.setChanged();
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.effects = input.read("effects", EFFECTS_CODEC).orElse(List.of());
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("effects", EFFECTS_CODEC, this.effects);
	}
}
