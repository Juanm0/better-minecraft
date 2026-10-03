package name.modid;

import name.modid.beacon.BeaconProtectionManager;
import name.modid.dragon.DragonDamage;
import name.modid.dragon.DragonFightManager;
import name.modid.potion.PotionMod;
import name.modid.world.FloatingEndCityGenerator;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterMinecraft implements ModInitializer {
	public static final String MOD_ID = "better-minecraft";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModAttachments.init();
		DragonFightManager.init();
		DragonDamage.init();
		BeaconProtectionManager.init();
		PotionMod.init();
		FloatingEndCityGenerator.init();

		LOGGER.info("Better Minecraft etapa 8 (End Cities flotantes, cazuela). Loaded: enchanting reroll, two-phase dragon, beacon protection, potion cauldron.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
