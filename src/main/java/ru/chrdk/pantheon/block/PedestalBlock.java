package ru.chrdk.pantheon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Постамент — место подписчика на чердаке: печать «впечатывается», над постаментом
 * встаёт фигурка подписчика.
 */
public class PedestalBlock extends AtticShrineBlock {
	public PedestalBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PedestalBlockEntity(pos, state);
	}

	@Override
	protected Component emptyHint() {
		return Component.literal("Постамент пуст. Водрузи именную печать или фигурку подписчика — "
				+ "фигурку отливают в верстаке из печати, свечи, золота и бумаги.");
	}

	@Override
	protected Component onEnshrined(String nick, int tier) {
		return Component.literal("✦ " + nick + " водружён на постамент (тир " + tier + "). Чердак помнит.");
	}

	@Override
	protected Component onReleased(String nick) {
		return Component.literal("Фигурка " + nick + " снята с постамента и вернулась в руки.");
	}
}
