package ru.chrdk.pantheon.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * Печать — «чернильница» ритуала. Пустая печать бесполезна, но если переименовать её
 * в наковальне (или выдать командой), на ней появляется имя подписчика, и её можно
 * водрузить на постамент.
 */
public final class Seals {
	private Seals() {
	}

	/** Печать, подписанная именем. */
	public static ItemStack named(String nick) {
		ItemStack seal = new ItemStack(PantheonContent.BLANK_SEAL);
		seal.set(DataComponents.CUSTOM_NAME, Component.literal(nick).withStyle(ChatFormatting.GOLD));
		return seal;
	}

	/** Имя с печати или null, если печать пустая. */
	public static String nameOf(ItemStack stack) {
		if (stack.isEmpty() || !stack.is(PantheonContent.BLANK_SEAL)) {
			return null;
		}

		if (!stack.has(DataComponents.CUSTOM_NAME)) {
			return null;
		}

		String name = stack.getHoverName().getString().trim();
		return name.isEmpty() ? null : name;
	}

	public static boolean isNamed(ItemStack stack) {
		return nameOf(stack) != null;
	}
}
