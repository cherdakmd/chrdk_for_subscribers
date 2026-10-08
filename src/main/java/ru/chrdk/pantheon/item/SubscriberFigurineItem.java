package ru.chrdk.pantheon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import ru.chrdk.pantheon.registry.PantheonContent;

/** Персональная фигурка: имя переносится со вписанной печати специальным рецептом. */
public class SubscriberFigurineItem extends Item {
	public SubscriberFigurineItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	public static ItemStack named(String nick, int tier) {
		ItemStack figurine = new ItemStack(PantheonContent.SUBSCRIBER_FIGURINE);
		figurine.set(DataComponents.CUSTOM_NAME,
				Component.literal("Фигурка " + nick).withStyle(ChatFormatting.GOLD));
		figurine.set(DataComponents.LORE,
				new net.minecraft.world.item.component.ItemLore(java.util.List.of(
						Component.literal("Подписчик: " + nick).withStyle(ChatFormatting.GRAY),
						Component.literal("Тир " + tier).withStyle(ChatFormatting.DARK_GRAY))));
		return figurine;
	}

	public static String nameOf(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() != PantheonContent.SUBSCRIBER_FIGURINE
				|| !stack.has(DataComponents.CUSTOM_NAME)) {
			return null;
		}
		String name = stack.getHoverName().getString();
		if (!name.startsWith("Фигурка ")) return null;
		name = name.substring("Фигурка ".length()).trim();
		return name.isEmpty() ? null : name;
	}
}
