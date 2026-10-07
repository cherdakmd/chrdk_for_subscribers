package ru.chrdk.pantheon.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.util.Text;

/**
 * Свиток Имён — носимый список подписчиков. Правый клик читает летопись вслух (в чат).
 */
public class NameScrollItem extends Item {
	private static final int LINES_PER_READ = 12;

	public NameScrollItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel) {
			PantheonData data = PantheonData.get(serverLevel);
			List<Subscriber> subscribers = data.all();

			if (subscribers.isEmpty()) {
				player.sendSystemMessage(Component.literal("Свиток пуст: ни одного имени на чердаке.").withStyle(ChatFormatting.GRAY));
			} else {
				player.sendSystemMessage(Component.literal("═══ Свиток Имён · всего " + subscribers.size() + " ═══").withStyle(ChatFormatting.GOLD));

				for (int i = 0; i < Math.min(LINES_PER_READ, subscribers.size()); i++) {
					Subscriber subscriber = subscribers.get(i);
					player.sendSystemMessage(Component.literal("#" + (i + 1) + " " + subscriber.name()
							+ " · тир " + subscriber.tier()
							+ " · " + Text.date(subscriber.addedAt())
							+ (subscriber.active() ? "" : " · потух")).withStyle(ChatFormatting.GRAY));
				}

				if (subscribers.size() > LINES_PER_READ) {
					player.sendSystemMessage(Component.literal("...и ещё " + (subscribers.size() - LINES_PER_READ)
							+ " имён. Полный список: /attic list").withStyle(ChatFormatting.DARK_GRAY));
				}
			}

			level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_HIT, SoundSource.PLAYERS, 0.4F, 1.6F);
		}

		return InteractionResult.SUCCESS;
	}
}
