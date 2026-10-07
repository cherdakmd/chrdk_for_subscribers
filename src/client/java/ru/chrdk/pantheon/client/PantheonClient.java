package ru.chrdk.pantheon.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import ru.chrdk.pantheon.PantheonMod;
import ru.chrdk.pantheon.client.figurine.FigurineLayers;
import ru.chrdk.pantheon.client.figurine.PedestalRenderer;
import ru.chrdk.pantheon.client.figurine.PortraitLayers;
import ru.chrdk.pantheon.client.figurine.PortraitRenderer;
import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * Клиентская часть: фигурки подписчиков на постаментах и портреты в рамах.
 *
 * <p>И то и другое — модель игрока, поэтому у подписчиков с майнкрафт-аккаунтом
 * видно их настоящий скин, а у остальных — процедурный аватар по нику.
 */
public class PantheonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(FigurineLayers.CLASSIC, FigurineLayers::createClassic);
		ModelLayerRegistry.registerModelLayer(FigurineLayers.SLIM, FigurineLayers::createSlim);
		ModelLayerRegistry.registerModelLayer(PortraitLayers.HEAD, PortraitLayers::createHead);

		BlockEntityRenderers.register(PantheonContent.PEDESTAL_ENTITY, PedestalRenderer::new);
		BlockEntityRenderers.register(PantheonContent.PORTRAIT_ENTITY, PortraitRenderer::new);

		PantheonMod.LOGGER.info("[{}] Клиентская часть загружена: фигурки и портреты включены.", PantheonMod.MOD_NAME);
	}
}
