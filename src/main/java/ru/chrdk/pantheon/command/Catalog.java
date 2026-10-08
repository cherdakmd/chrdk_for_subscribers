package ru.chrdk.pantheon.command;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.util.Chat;
import ru.chrdk.pantheon.util.Text;

/**
 * Каталог чердака прямо в чате: список имён с кликом по каждому.
 * Экран-каталог у обелиска вырастет из этого позже, а страницы уже работают
 * где угодно — в книге, на табличке, в чате.
 */
public final class Catalog {
	public static final int PAGE_SIZE = 10;

	private Catalog() {
	}

	/** Печатает страницу каталога: имена кликабельны, есть кнопки вперёд/назад. */
	public static void page(ServerLevel level, int page, Consumer<Component> sink) {
		PantheonData data = PantheonData.get(level);
		List<Subscriber> subscribers = data.all();

		if (subscribers.isEmpty()) {
			sink.accept(Component.literal("Полки пусты: ни одного имени.").withStyle(ChatFormatting.GRAY));
			sink.accept(Component.literal("Смастери печать, назови её в наковальне и щёлкни по постаменту. Или /attic import.")
					.withStyle(ChatFormatting.DARK_GRAY));
			return;
		}

		int pages = (subscribers.size() + PAGE_SIZE - 1) / PAGE_SIZE;
		int current = Math.min(Math.max(page, 1), pages);
		int from = (current - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, subscribers.size());

		sink.accept(Component.literal("── Летопись чердака · стр. " + current + "/" + pages
				+ " · всего " + subscribers.size() + " ──").withStyle(ChatFormatting.GOLD));

		for (int i = from; i < to; i++) {
			final int number = i + 1;
			final Subscriber subscriber = subscribers.get(i);
			sink.accept(Component.literal("#" + number + " ").withStyle(ChatFormatting.DARK_GRAY)
					.append(Chat.name(subscriber))
					.append(Component.literal(" · тир " + subscriber.tier()
							+ (subscriber.offerings() > 0 ? " · ✦" + subscriber.offerings() : "")
							+ (subscriber.active() ? "" : " · потух")).withStyle(ChatFormatting.GRAY)));
		}

		MutableComponent navigation = Component.empty();

		if (current > 1) {
			navigation.append(Chat.button("[← стр. " + (current - 1) + "]",
					"/attic list " + (current - 1), Component.literal("Предыдущая страница"))).append("  ");
		}

		navigation.append(Chat.button("[топ даров]",
				"/attic top", Component.literal("Кто больше всех обласкан дарами"))).append("  ");

		if (current < pages) {
			navigation.append(Chat.button("[стр. " + (current + 1) + " →]",
					"/attic list " + (current + 1), Component.literal("Следующая страница")));
		}

		sink.accept(navigation);
	}

	/** Карточка подписчика с кнопками управления. */
	public static void card(ServerLevel level, Subscriber subscriber, Consumer<Component> sink) {
		PantheonData data = PantheonData.get(level);
		int index = data.all().indexOf(subscriber) + 1;

		sink.accept(Component.literal("── " + subscriber.name() + " ──").withStyle(ChatFormatting.GOLD));
		sink.accept(Component.literal("#" + index
				+ " · тир " + subscriber.tier()
				+ " · на чердаке с " + Text.date(subscriber.addedAt())
				+ " · " + subscriber.source()
				+ (subscriber.active() ? " · в строю" : " · потух")).withStyle(ChatFormatting.GRAY));

		if (subscriber.offerings() > 0) {
			sink.accept(Component.literal("Даров принесено: " + subscriber.offerings()).withStyle(ChatFormatting.GRAY));
		}

		MutableComponent tiers = Component.literal("Тир: ").withStyle(ChatFormatting.DARK_GRAY);

		for (int tier = 1; tier <= 5; tier++) {
			final int value = tier;
			tiers.append(Chat.button(subscriber.tier() == tier ? "[" + tier + "]" : " " + tier + " ",
					"/attic tier " + subscriber.name() + " " + value,
					Component.literal("Поставить тир " + value)));
		}

		sink.accept(tiers);
		sink.accept(Component.empty()
				.append(Chat.button("[выдать печать]", "/attic seal " + subscriber.name(),
						Component.literal("Печать с этим именем — её можно впечатать в постамент или ларь")))
				.append("  ")
				.append(Chat.button("[выдать фигурку]", "/attic figurine " + subscriber.name() + " " + subscriber.tier(),
						Component.literal("Фигурка с этим именем и тиром — водрузить на постамент")))
				.append("  ")
				.append(Chat.button(subscriber.active() ? "[потушить]" : "[вернуть в строй]",
						"/attic toggle " + subscriber.name(),
						Component.literal(subscriber.active() ? "Отписавшиеся гаснут, но остаются на чердаке"
								: "Снова зажечь фигурку")))
				.append("  ")
				.append(Chat.button("[к списку]", "/attic list", Component.literal("Вся летопись"))));
	}

	/** Поиск по летописи. */
	public static void find(ServerLevel level, String query, Consumer<Component> sink) {
		PantheonData data = PantheonData.get(level);
		String needle = query.toLowerCase(java.util.Locale.ROOT);
		int shown = 0;

		sink.accept(Component.literal("── Поиск: «" + query + "» ──").withStyle(ChatFormatting.GOLD));

		for (Subscriber subscriber : data.all()) {
			if (!subscriber.name().toLowerCase(java.util.Locale.ROOT).contains(needle)) {
				continue;
			}

			if (shown >= 20) {
				sink.accept(Component.literal("…есть ещё совпадения, уточни запрос.").withStyle(ChatFormatting.DARK_GRAY));
				return;
			}

			final Subscriber found = subscriber;
			sink.accept(Component.literal("· ").withStyle(ChatFormatting.DARK_GRAY)
					.append(Chat.name(found))
					.append(Component.literal(" · тир " + found.tier()).withStyle(ChatFormatting.GRAY)));
			shown++;
		}

		if (shown == 0) {
			sink.accept(Component.literal("Ничего не нашлось.").withStyle(ChatFormatting.GRAY));
		}
	}
}
