package ru.chrdk.pantheon.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

import ru.chrdk.pantheon.PantheonMod;

/** Сериализаторы рецептов мода. */
public final class PantheonRecipes {
	/** Отливка фигурки из именной печати — рецепт без полей, лишь бы был «особым». */
	public static final RecipeSerializer<FigurineCastRecipe> FIGURINE_CAST = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER, PantheonMod.id("crafting_figurine_cast"),
			new RecipeSerializer<FigurineCastRecipe>(MapCodec.unit(FigurineCastRecipe::new), StreamCodec.unit(new FigurineCastRecipe())));

	private PantheonRecipes() {
	}

	public static void register() {
		// Сериализатор регистрируется при загрузке класса — вызов лишь для порядка.
	}
}
