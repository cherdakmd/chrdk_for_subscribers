package ru.chrdk.pantheon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import ru.chrdk.pantheon.registry.PantheonContent;

/** Данные постамента: чьё имя впечатано и какого размера фигурку ставить. */
public class PedestalBlockEntity extends AbstractShrineBlockEntity {
	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		super(PantheonContent.PEDESTAL_ENTITY, pos, state);
	}
}
