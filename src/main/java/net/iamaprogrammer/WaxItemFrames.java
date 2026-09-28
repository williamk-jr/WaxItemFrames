package net.iamaprogrammer;

import net.fabricmc.api.ModInitializer;
import net.iamaprogrammer.config.ModConfig;
import net.iamaprogrammer.config.core.ConfigRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaxItemFrames implements ModInitializer {
	public static final String MOD_ID = "wax-item-frames";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ModConfig CONFIG;

	@Override
	public void onInitialize() {
		ModConfig defaultConfig = new ModConfig();
		defaultConfig.shouldFixItemFrameWhenWaxed(false);
		CONFIG = new ConfigRegistry<>(defaultConfig, ModConfig.class).register();
		LOGGER.info("Wax Item Frames loaded");
	}
}
