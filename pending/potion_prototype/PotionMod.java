package name.modid.potion;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class PotionMod {
    public static final Block POTION_CAULDRON = Registry.register(
            BuiltInRegistries.BLOCK,
            Identifier.fromNamespaceAndPath("better-minecraft", "potion_cauldron"),
            new PotionCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON))
    );

    public static final BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("better-minecraft", "potion_cauldron"),
            FabricBlockEntityTypeBuilder.<PotionCauldronBlockEntity>create(PotionCauldronBlockEntity::new, POTION_CAULDRON).build()
    );

    private PotionMod() {}

    public static void initialize() {
        // Static registration is intentionally completed before interactions are installed.
    }
}
