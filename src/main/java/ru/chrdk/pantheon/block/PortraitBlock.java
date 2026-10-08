package ru.chrdk.pantheon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Портретная рама — место подписчика на стене.
 * Внутри рамы рисуется голова подписчика: у кого есть майнкрафт-аккаунт — его лицо,
 * у остальных — процедурный аватар по нику.
 */
public class PortraitBlock extends AtticShrineBlock {
	public PortraitBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PortraitBlockEntity(pos, state);
	}

	@Override
	protected Component emptyHint() {
		return Component.literal("Рама пуста. Возьми именную печать (переименуй пустую в наковальне) и щёлкни по раме.");
	}

	@Override
	protected Component onEnshrined(String nick, int tier) {
		return Component.literal("✦ Портрет " + nick + " повешен в раму (тир " + tier + "). Со стены смотрит.");
	}

	@Override
	protected Component onReleased(String nick) {
		return Component.literal("Портрет " + nick + " снят со стены, печать вернулась в руки.");
	}
}
