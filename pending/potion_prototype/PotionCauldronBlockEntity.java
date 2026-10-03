package name.modid.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PotionCauldronBlockEntity extends BlockEntity {
    private List<MobEffectInstance> effects = List.of();

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(PotionMod.POTION_CAULDRON_ENTITY, pos, state);
    }

    public List<MobEffectInstance> getEffects() { return effects; }

    public void setEffects(List<MobEffectInstance> effects) {
        this.effects = List.copyOf(effects);
        setChanged();
    }
}
