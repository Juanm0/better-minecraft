package name.modid.potion;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-side prototype store for potion-cauldron contents. */
public final class PotionCauldronData {
    private PotionCauldronData() {}

    private static final Map<Key, List<MobEffectInstance>> DATA = new java.util.HashMap<>();

    public static void set(Object levelKey, long pos, List<MobEffectInstance> effects) {
        DATA.put(new Key(levelKey, pos), List.copyOf(effects));
    }

    public static List<MobEffectInstance> get(Object levelKey, long pos) {
        return DATA.getOrDefault(new Key(levelKey, pos), List.of());
    }

    public static void remove(Object levelKey, long pos) {
        DATA.remove(new Key(levelKey, pos));
    }

    public static List<MobEffectInstance> mix(ItemStack first, ItemStack second) {
        LinkedHashMap<Object, MobEffectInstance> merged = new LinkedHashMap<>();
        add(merged, first);
        add(merged, second);
        return new ArrayList<>(merged.values());
    }

    private static void add(Map<Object, MobEffectInstance> merged, ItemStack stack) {
        PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents == null) return;
        for (MobEffectInstance effect : contents.getAllEffects()) {
            Object key = effect.getEffect().get();
            MobEffectInstance old = merged.get(key);
            if (old == null || effect.getDuration() > old.getDuration() || effect.getAmplifier() > old.getAmplifier()) {
                merged.put(key, effect);
            }
        }
    }

    private record Key(Object levelKey, long pos) {}
}
