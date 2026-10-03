package name.modid.potion;

import name.modid.BetterMinecraft;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registro de la cazuela de pociones. Se llama desde BetterMinecraft.onInitialize. */
public final class PotionMod {
	public static final ResourceKey<Block> POTION_CAULDRON_KEY =
		ResourceKey.create(Registries.BLOCK, BetterMinecraft.id("potion_cauldron"));

	public static PotionCauldronBlock POTION_CAULDRON;
	public static BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON_ENTITY;

	private PotionMod() {
	}

	public static void init() {
		POTION_CAULDRON = Registry.register(
			BuiltInRegistries.BLOCK,
			POTION_CAULDRON_KEY,
			new PotionCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).setId(POTION_CAULDRON_KEY))
		);
		POTION_CAULDRON_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			POTION_CAULDRON_KEY.identifier(),
			FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, POTION_CAULDRON).build()
		);
		PotionCauldronInteraction.init();
	}
}
