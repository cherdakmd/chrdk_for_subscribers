package ru.chrdk.pantheon.gameplay;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Подношения: игрок кладёт предмет занятому святилищу, подписчик благодарит даром.
 * Чем выше тир подписчика — тем щедрее ответ, и раз в игровой день на каждую фигурку.
 */
public final class Offerings {
	/** Кулдаун на одно святилище — игровой день. */
	public static final long COOLDOWN_TICKS = 24000L;

	private record Reward(Item item, int minTier, int weight, int minCount, int maxCount) {
	}

	private static final List<Reward> TABLE = List.of(
			new Reward(Items.PAPER, 1, 6, 1, 3),
			new Reward(Items.GOLD_NUGGET, 1, 5, 1, 3),
			new Reward(Items.CANDLE, 1, 4, 1, 2),
			new Reward(Items.INK_SAC, 1, 4, 1, 2),
			new Reward(Items.IRON_NUGGET, 2, 5, 2, 4),
			new Reward(Items.LAPIS_LAZULI, 2, 4, 1, 3),
			new Reward(Items.HONEYCOMB, 2, 3, 1, 2),
			new Reward(Items.GLOW_BERRIES, 2, 3, 1, 3),
			new Reward(Items.BOOK, 3, 3, 1, 2),
			new Reward(Items.AMETHYST_SHARD, 3, 3, 1, 3),
			new Reward(Items.IRON_INGOT, 3, 3, 1, 2),
			new Reward(Items.EMERALD, 4, 3, 1, 3),
			new Reward(Items.GOLD_INGOT, 4, 3, 1, 2),
			new Reward(Items.LANTERN, 4, 2, 1, 1),
			new Reward(Items.DIAMOND, 5, 2, 1, 1),
			new Reward(Items.GOLDEN_APPLE, 5, 2, 1, 1)
	);

	private Offerings() {
	}

	/** Дары по тиру подписчика: 1–3 подарка, чем выше тир — тем лучше и больше. */
	public static List<ItemStack> roll(int tier, RandomSource random) {
		List<Reward> pool = new ArrayList<>();

		for (Reward reward : TABLE) {
			if (reward.minTier() <= tier) {
				pool.add(reward);
			}
		}

		List<ItemStack> gifts = new ArrayList<>();

		if (pool.isEmpty()) {
			gifts.add(new ItemStack(Items.GOLD_NUGGET));
			return gifts;
		}

		int rolls = 1 + random.nextInt(tier >= 4 ? 3 : 2);

		for (int i = 0; i < rolls; i++) {
			int total = 0;

			for (Reward reward : pool) {
				total += reward.weight();
			}

			int pick = random.nextInt(Math.max(1, total));

			for (Reward reward : pool) {
				pick -= reward.weight();

				if (pick < 0) {
					int count = reward.minCount() + random.nextInt(reward.maxCount() - reward.minCount() + 1);
					gifts.add(new ItemStack(reward.item(), count));
					break;
				}
			}
		}

		return gifts;
	}

	/** Выбрасывает дары у святилища и рассказывает об этом игроку. */
	public static void grant(ServerLevel level, BlockPos pos, Player player, String nick, int tier, int offeringNumber) {
		RandomSource random = level.getRandom();
		List<ItemStack> gifts = roll(tier, random);
		StringBuilder given = new StringBuilder();

		for (ItemStack gift : gifts) {
			ItemEntity entity = new ItemEntity(level,
					pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D,
					pos.getY() + 1.1D,
					pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.4D,
					gift);
			entity.setDefaultPickUpDelay();
			level.addFreshEntity(entity);

			if (given.length() > 0) {
				given.append(", ");
			}

			given.append(gift.getHoverName().getString());

			if (gift.getCount() > 1) {
				given.append(" ×").append(gift.getCount());
			}
		}

		player.sendSystemMessage(Component.literal("✦ Дар принят: " + nick + " благодарит — " + given
				+ " (дар №" + offeringNumber + ")").withStyle(ChatFormatting.GOLD));

		level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 0.9F);
		level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
	}
}
