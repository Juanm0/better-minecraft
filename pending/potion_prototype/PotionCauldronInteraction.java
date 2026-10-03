package name.modid.potion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PotionCauldronInteraction {
    private PotionCauldronInteraction() {}

    public static InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player,
                                             InteractionHand hand, ItemStack stack) {
        if (!isPotion(stack)) return InteractionResult.PASS;
        if (!state.is(Blocks.CAULDRON) && !state.is(Blocks.WATER_CAULDRON) && !state.is(PotionMod.POTION_CAULDRON)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        List<net.minecraft.world.effect.MobEffectInstance> incoming = effects(stack);
        if (incoming.isEmpty()) return InteractionResult.PASS;

        List<net.minecraft.world.effect.MobEffectInstance> merged = new ArrayList<>(incoming);
        if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be) {
            merged = merge(be.getEffects(), incoming);
        }

        level.setBlock(pos, PotionMod.POTION_CAULDRON.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity be) {
            be.setEffects(merged);
        }
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }

    private static boolean isPotion(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }

    private static List<net.minecraft.world.effect.MobEffectInstance> effects(ItemStack stack) {
        PotionContents c = stack.get(DataComponents.POTION_CONTENTS);
        if (c == null) return List.of();
        List<net.minecraft.world.effect.MobEffectInstance> out = new ArrayList<>();
        c.getAllEffects().forEach(out::add);
        return out;
    }

    private static List<net.minecraft.world.effect.MobEffectInstance> merge(List<net.minecraft.world.effect.MobEffectInstance> a,
                                                                              List<net.minecraft.world.effect.MobEffectInstance> b) {
        Map<Object, net.minecraft.world.effect.MobEffectInstance> map = new LinkedHashMap<>();
        for (var e : a) map.put(e.getEffect().get(), e);
        for (var e : b) {
            var old = map.get(e.getEffect().get());
            if (old == null || e.getAmplifier() > old.getAmplifier() || e.getDuration() > old.getDuration()) map.put(e.getEffect().get(), e);
        }
        return new ArrayList<>(map.values());
    }
}
