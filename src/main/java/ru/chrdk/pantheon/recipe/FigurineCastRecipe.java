package ru.chrdk.pantheon.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import ru.chrdk.pantheon.item.Seals;
import ru.chrdk.pantheon.item.SubscriberFigurineItem;
import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * Ритуальная отливка фигурки: именная печать + свеча + золотой слиток + бумага.
 * Имя с печати переносится на фигурку, тир — первый. Обычный рецепт не годится:
 * он потерял бы имя, поэтому крафт особый (как фейерверк) и не светится в книге рецептов.
 */
public class FigurineCastRecipe extends CustomRecipe {
	public FigurineCastRecipe() {
		super();
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		boolean seal = false;
		boolean candle = false;
		boolean gold = false;
		boolean paper = false;
		int total = 0;

		for (ItemStack stack : input.items()) {
			if (stack.isEmpty()) {
				continue;
			}

			total++;

			if (stack.is(PantheonContent.BLANK_SEAL) && Seals.nameOf(stack) != null) {
				seal = true;
			} else if (stack.is(Items.CANDLE)) {
				candle = true;
			} else if (stack.is(Items.GOLD_INGOT)) {
				gold = true;
			} else if (stack.is(Items.PAPER)) {
				paper = true;
			} else {
				return false;
			}
		}

		return total == 4 && seal && candle && gold && paper;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		for (ItemStack stack : input.items()) {
			if (stack.is(PantheonContent.BLANK_SEAL)) {
				String nick = Seals.nameOf(stack);

				if (nick != null) {
					return SubscriberFigurineItem.named(nick, 1);
				}
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return PantheonRecipes.FIGURINE_CAST;
	}
}
