package ru.chrdk.pantheon.gameplay;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Ритуальное возвышение фигурки у Чердачного алтаря: две одинаковые фигурки одного ника
 * и тира (в двух руках) плюс ресурсы из инвентаря превращаются в фигурку тира выше.
 */
public final class FigurineRitual {
	/** Выше пятого тира фигурки не растут — дальше только эндгейм-реликвии. */
	public static final int MAX_TIER = 5;

	private FigurineRitual() {
	}

	/** Одна позиция ритуального ресурса: предмет и сколько надо. */
	public record Cost(Item item, int count) {
		boolean matches(ItemStack stack) {
			return stack.is(item);
		}
	}

	/** Ресурсы для перехода с тира {@code tier} на тир выше (с тира 5 — пусто). */
	public static List<Cost> costFor(int tier) {
		return switch (tier) {
			case 1 -> List.of(new Cost(Items.IRON_INGOT, 4));
			case 2 -> List.of(new Cost(Items.DIAMOND, 2));
			case 3 -> List.of(new Cost(Items.NETHERITE_INGOT, 1));
			case 4 -> List.of(new Cost(Items.NETHER_STAR, 1), new Cost(Items.DRAGON_BREATH, 1), new Cost(Items.GOLD_INGOT, 4));
			default -> List.of();
		};
	}

	/** Чего не хватает в инвентаре игрока для возвышения с тира {@code tier}. */
	public static List<Cost> missingCosts(Player player, int tier) {
		List<Cost> missing = new ArrayList<>();

		for (Cost cost : costFor(tier)) {
			int found = player.getInventory().clearOrCountMatchingItems(cost::matches, false, cost.count(), null);

			if (found < cost.count()) {
				missing.add(new Cost(cost.item(), cost.count() - found));
			}
		}

		return missing;
	}

	/** Списывает ресурсы возвышения с тира {@code tier} из инвентаря игрока. */
	public static void consumeCosts(Player player, int tier) {
		for (Cost cost : costFor(tier)) {
			player.getInventory().clearOrCountMatchingItems(cost::matches, true, cost.count(), null);
		}
	}

	/** «алмаз ×2, звезда Незера ×1» — для сообщений о недостающих ресурсах. */
	public static String describe(List<Cost> costs) {
		List<String> parts = new ArrayList<>();

		for (Cost cost : costs) {
			parts.add(new ItemStack(cost.item()).getHoverName().getString() + " ×" + cost.count());
		}

		return String.join(", ", parts);
	}
}
