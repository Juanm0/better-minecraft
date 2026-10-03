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
	/** Cuantas pociones se volcaron en esta mezcla (2+ = liquido violeta). */
	private int pours = 0;
	/** true = al sacar botella sale pocion arrojadiza (se activa con polvora). */
	private boolean splash = false;

	public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(PotionMod.POTION_CAULDRON_ENTITY, pos, state);
	}

	public List<MobEffectInstance> getEffects() {
		return effects;
	}

	public int getPours() {
		return pours;
	}

	public boolean isSplash() {
		return splash;
	}

	public void setSplash(boolean splash) {
		this.splash = splash;
		this.setChanged();
	}

	public void setMix(List<MobEffectInstance> effects, int pours) {
		this.effects = List.copyOf(effects);
		this.pours = pours;
		this.setChanged();
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.effects = input.read("effects", EFFECTS_CODEC).orElse(List.of());
		this.pours = input.getIntOr("pours", 0);
		this.splash = input.getBooleanOr("splash", false);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("effects", EFFECTS_CODEC, this.effects);
		output.putInt("pours", this.pours);
		output.putBoolean("splash", this.splash);
	}
}
