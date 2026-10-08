package ru.chrdk.pantheon.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import ru.chrdk.pantheon.data.Subscriber;

/** Кликабельные элементы чата: имена в летописи ведут в карточку подписчика. */
public final class Chat {
	private Chat() {
	}

	/** Кнопка, которая выполняет команду по клику. */
	public static Component button(String label, String command, Component tooltip) {
		return Component.literal(label).withStyle(Style.EMPTY
				.withColor(ChatFormatting.AQUA)
				.withClickEvent(new ClickEvent.RunCommand(command))
				.withHoverEvent(new HoverEvent.ShowText(tooltip)));
	}

	/** Имя подписчика: клик — карточка, наведение — краткая справка. */
	public static Component name(Subscriber subscriber) {
		return Component.literal(subscriber.name()).withStyle(Style.EMPTY
				.withColor(subscriber.active() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY)
				.withClickEvent(new ClickEvent.RunCommand("/attic info " + subscriber.name()))
				.withHoverEvent(new HoverEvent.ShowText(card(subscriber))));
	}

	/** Всплывающая справка по подписчику. */
	public static Component card(Subscriber subscriber) {
		StringBuilder text = new StringBuilder();
		text.append(subscriber.name()).append('\n');
		text.append("тир ").append(subscriber.tier()).append('\n');
		text.append("на чердаке с ").append(Text.date(subscriber.addedAt())).append('\n');
		text.append("источник: ").append(subscriber.source()).append('\n');

		if (subscriber.offerings() > 0) {
			text.append("даров: ").append(subscriber.offerings()).append('\n');
		}

		text.append(subscriber.active() ? "в строю" : "потух");
		text.append("\n\nклик — открыть карточку");
		return Component.literal(text.toString());
	}
}
