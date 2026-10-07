package ru.chrdk.pantheon.client;

import net.fabricmc.api.ClientModInitializer;

import ru.chrdk.pantheon.PantheonMod;

/**
 * Клиентская часть. Пока пустая: в v0.2 здесь появится рендер фигурок подписчиков
 * (3D-модель игрока вместо статуэтки) и портретные рамы.
 */
public class PantheonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PantheonMod.LOGGER.info("[{}] Клиентская часть загружена.", PantheonMod.MOD_NAME);
	}
}
