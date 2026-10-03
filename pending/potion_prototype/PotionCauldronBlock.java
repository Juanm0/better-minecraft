package name.modid.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityProvider;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A cauldron-like block that stores and mixes arbitrary potion effects. */
public class PotionCauldronBlock extends Block implements BlockEntityProvider {
    public PotionCauldronBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          net.minecraft.world.entity.player.Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!isPotion(stack)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            List<MobEffectInstance> merged = merge(be.getEffects(), stack);
            be.setEffects(merged);
            level.sendBlockUpdated(pos, state, state, 3);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.swing(hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                net.minecraft.world.entity.player.Player player,
                                                BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be)) return InteractionResult.PASS;
        if (!be.getEffects().isEmpty() && !level.isClientSide()) {
            ItemStack bottle = new ItemStack(Items.POTION);
            PotionContents contents = new PotionContents(java.util.Optional.empty(), java.util.Optional.of(0x9B59FF), be.getEffects(), java.util.Optional.of("mixed"));
            bottle.set(DataComponents.POTION_CONTENTS, contents);
            if (!player.getInventory().add(bottle)) player.drop(bottle, false);
            be.setEffects(List.of());
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    private static List<MobEffectInstance> merge(List<MobEffectInstance> existing, ItemStack incoming) {
        Map<Object, MobEffectInstance> map = new LinkedHashMap<>();
        for (MobEffectInstance e : existing) map.put(e.getEffect().get(), e);
        PotionContents c = incoming.get(DataComponents.POTION_CONTENTS);
        if (c != null) for (MobEffectInstance e : c.getAllEffects()) {
            MobEffectInstance old = map.get(e.getEffect().get());
            if (old == null || e.getAmplifier() > old.getAmplifier() || e.getDuration() > old.getDuration()) map.put(e.getEffect().get(), e);
        }
        return new ArrayList<>(map.values());
    }
}
