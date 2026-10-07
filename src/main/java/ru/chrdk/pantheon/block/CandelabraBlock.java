package ru.chrdk.pantheon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Чердачный канделябр: три свечи на латунной подставке. Светит ярче фонаря
 * и слегка дымит — с ним чердак выглядит жилым.
 */
public class CandelabraBlock extends Block {
	public CandelabraBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) != 0) {
			return;
		}

		level.addParticle(ParticleTypes.SMOKE,
				pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
				pos.getY() + 0.95D,
				pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.6D,
				0.0D, 0.01D, 0.0D);
	}
}
