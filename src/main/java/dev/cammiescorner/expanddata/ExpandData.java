package dev.cammiescorner.expanddata;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExpandData implements ModInitializer {
	public static final String MOD_ID = "assets/expanddata";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Expand Donk 3: Electric Boogalee!");
	}
}
