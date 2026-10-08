package ru.chrdk.pantheon;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.chrdk.pantheon.command.PantheonCommands;
import ru.chrdk.pantheon.gameplay.AuraService;
import ru.chrdk.pantheon.recipe.PantheonRecipes;
import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * «Чердак Бессмертных» — мод, который увековечивает подписчиков канала в мире выживания.
 */
public class PantheonMod implements ModInitializer {
	public static final String MOD_ID = "chrdk_pantheon";
	public static final String MOD_NAME = "Чердак Бессмертных";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PantheonContent.register();
		PantheonRecipes.register();
		PantheonCommands.register();
		AuraService.register();

		LOGGER.info("[{}] Чердак открыт, полки ждут подписчиков.", MOD_NAME);
	}
}
