package ru.chrdk.pantheon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import ru.chrdk.pantheon.registry.PantheonContent;

/** Данные портретной рамы: чьё лицо в ней висит. */
public class PortraitBlockEntity extends AbstractShrineBlockEntity {
	public PortraitBlockEntity(BlockPos pos, BlockState state) {
		super(PantheonContent.PORTRAIT_ENTITY, pos, state);
	}
}
