package ru.chrdk.pantheon.block;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.util.Text;

/**
 * Чердачный алтарь — сердце зала. Правый клик по нему рассказывает, сколько подписчиков
 * уже на полках и кто пришёл последним.
 */
public class AtticAltarBlock extends Block {
	public AtticAltarBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel) {
			PantheonData data = PantheonData.get(serverLevel);

			player.sendSystemMessage(Component.literal("── Чердак Бессмертных ──").withStyle(ChatFormatting.GOLD));
			player.sendSystemMessage(Component.literal("Подписчиков на полках: " + data.size()
					+ " (в строю: " + data.activeCount() + ")").withStyle(ChatFormatting.YELLOW));

			List<Subscriber> latest = data.latest(3);

			if (latest.isEmpty()) {
				player.sendSystemMessage(Component.literal("Полки пусты. Смастери печать, назови её в наковальне и щёлкни по постаменту.")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else {
				for (Subscriber subscriber : latest) {
					player.sendSystemMessage(Component.literal("  · " + subscriber.name()
							+ " — тир " + subscriber.tier()
							+ ", с " + Text.date(subscriber.addedAt())).withStyle(ChatFormatting.GRAY));
				}
			}

			level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.5F, 1.4F);
		}

		return InteractionResult.SUCCESS;
	}
}
