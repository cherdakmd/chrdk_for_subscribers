package ru.chrdk.pantheon.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import ru.chrdk.pantheon.util.Text;

/**
 * Книга Имён — летопись чердака в виде обычной книги, которую можно читать (и передавать).
 * Её «печатает» Обелиск Имён.
 */
public final class LedgerBook {
	private static final int ENTRIES_PER_PAGE = 12;
	private static final int MAX_PAGES = 48;
	private static final int MAX_PAGE_CHARS = 240;
	private static final int NAME_WIDTH = 14;

	private LedgerBook() {
	}

	public static ItemStack create(ServerLevel level) {
		PantheonData data = PantheonData.get(level);
		List<Subscriber> subscribers = data.all();
		List<Filterable<Component>> pages = new ArrayList<>();

		pages.add(page(mainPage(data, subscribers)));

		int perPageCapacity = (MAX_PAGES - 1) * ENTRIES_PER_PAGE;
		int listed = Math.min(subscribers.size(), perPageCapacity);

		for (int start = 0; start < listed; start += ENTRIES_PER_PAGE) {
			StringBuilder page = new StringBuilder();
			int end = Math.min(start + ENTRIES_PER_PAGE, listed);

			for (int i = start; i < end; i++) {
				if (page.length() > 0) {
					page.append('\n');
				}

				page.append(entry(subscribers.get(i), i + 1));
			}

			pages.add(page(Component.literal(clamp(page.toString()))));
		}

		if (subscribers.size() > listed) {
			pages.add(page(Component.literal(clamp("…и ещё " + (subscribers.size() - listed)
					+ " имён не влезли.\n\nПолный список: /attic list"))));
		}

		Component honours = honoursPage(data);

		if (honours != null) {
			pages.add(page(honours));
		}

		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
		book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
				Filterable.passThrough("Книга Имён"),
				"Чердак Бессмертных",
				0,
				List.copyOf(pages),
				true));
		return book;
	}

	/** Первая страница: сводка и главные имена. */
	private static Component mainPage(PantheonData data, List<Subscriber> subscribers) {
		StringBuilder page = new StringBuilder();
		page.append("Чердак Бессмертных\n");
		page.append("Имён на полках: ").append(data.size()).append('\n');
		page.append("В строю: ").append(data.activeCount())
				.append(" · потухло: ").append(data.size() - data.activeCount());

		if (subscribers.isEmpty()) {
			page.append("\n\nПолки пусты.\n\nСмастери печать,\nназови её в наковальне\nи щёлкни по постаменту.");
			return Component.literal(page.toString());
		}

		if (data.totalOfferings() > 0) {
			page.append("\nДаров принесено: ").append(data.totalOfferings());
		}

		page.append("\n\n── Первые на чердаке ──\n");

		for (int i = 0; i < Math.min(3, subscribers.size()); i++) {
			Subscriber subscriber = subscribers.get(i);
			page.append(name(subscriber.name())).append(" — ").append(Text.date(subscriber.addedAt())).append('\n');
		}

		page.append("\n── Пришли последними ──\n");
		List<Subscriber> latest = data.latest(3);
		List<Subscriber> newestFirst = new ArrayList<>(latest);
		newestFirst.sort((a, b) -> Long.compare(a.addedAt(), b.addedAt()));

		for (Subscriber subscriber : newestFirst) {
			page.append(name(subscriber.name())).append(" — ").append(Text.date(subscriber.addedAt())).append('\n');
		}

		return Component.literal(clamp(page.toString()));
	}

	/** Последняя страница: вехи чердака и самые обласканные подписчики. */
	private static Component honoursPage(PantheonData data) {
		if (data.milestones().isEmpty() && data.totalOfferings() == 0) {
			return null;
		}

		StringBuilder page = new StringBuilder("── Вехи чердака ──\n");

		if (data.milestones().isEmpty()) {
			page.append("пока ни одной\n");
		} else {
			for (Milestone milestone : data.milestones()) {
				page.append(milestone.count()).append(" имён — ").append(Text.date(milestone.at())).append('\n');
			}
		}

		StringBuilder ladder = new StringBuilder("\nДальше: ");
		ladder.append(Milestones.nextHint(data));
		page.append(ladder);

		if (data.totalOfferings() > 0) {
			page.append("\n\n── Обласканные дарами ──\n");

			for (Subscriber subscriber : data.mostHonoured(5)) {
				if (subscriber.offerings() == 0) {
					break;
				}

				page.append(name(subscriber.name())).append(" — ").append(subscriber.offerings())
						.append(subscriber.offerings() == 1 ? " дар" : " дара").append('\n');
			}
		}

		return Component.literal(clamp(page.toString()));
	}

	/** Одна строка летописи: номер, ник, тир и пометка «потух». */
	private static String entry(Subscriber subscriber, int number) {
		StringBuilder line = new StringBuilder();
		line.append('#').append(number).append(' ').append(name(subscriber.name()));

		if (subscriber.tier() > 1) {
			line.append(" т").append(subscriber.tier());
		}

		if (subscriber.offerings() > 0) {
			line.append(" ✦").append(subscriber.offerings());
		}

		if (!subscriber.active()) {
			line.append(" †");
		}

		return line.toString();
	}

	/** Ник в книге не длиннее NAME_WIDTH символов — иначе строки ломаются. */
	private static String name(String nick) {
		return nick.length() <= NAME_WIDTH ? nick : nick.substring(0, NAME_WIDTH - 1) + "…";
	}

	/** Страница книги: Filterable в 26.3 принимает только компонент. */
	private static Filterable<Component> page(Component page) {
		return Filterable.passThrough(page);
	}

	private static String clamp(String page) {
		return page.length() <= MAX_PAGE_CHARS ? page : page.substring(0, MAX_PAGE_CHARS);
	}

	/** Краткая сводка для чата — та же, что показывает обелиск. */
	public static Component summary(ServerLevel level) {
		PantheonData data = PantheonData.get(level);
		return Component.literal("Чердак: " + data.size() + " имён · в строю " + data.activeCount()
						+ " · потухло " + (data.size() - data.activeCount()))
				.withStyle(ChatFormatting.YELLOW);
	}
}
