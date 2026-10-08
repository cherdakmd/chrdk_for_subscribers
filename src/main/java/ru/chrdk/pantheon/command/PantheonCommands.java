package ru.chrdk.pantheon.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

import ru.chrdk.pantheon.data.LedgerBook;
import ru.chrdk.pantheon.data.Milestones;
import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.gameplay.AuraService;
import ru.chrdk.pantheon.gameplay.AuraTier;
import ru.chrdk.pantheon.item.Seals;
import ru.chrdk.pantheon.registry.PantheonContent;
import ru.chrdk.pantheon.util.Chat;
import ru.chrdk.pantheon.util.Text;

/**
 * Команды мода: /attic (он же /чердак).
 */
public final class PantheonCommands {
	private static final int PAGE_SIZE = 10;

	private PantheonCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			dispatcher.register(build("attic"));
			dispatcher.register(build("чердак"));
		});
	}

	private static LiteralArgumentBuilder<CommandSourceStack> build(String name) {
		return literal(name)
				.then(literal("list")
						.executes(context -> list(context.getSource(), 1))
						.then(argument("page", IntegerArgumentType.integer(1))
								.executes(context -> list(context.getSource(), IntegerArgumentType.getInteger(context, "page")))))
				.then(literal("add")
						.then(argument("nick", StringArgumentType.greedyString())
								.executes(context -> add(context.getSource(), StringArgumentType.getString(context, "nick")))))
				.then(literal("remove")
						.then(argument("nick", StringArgumentType.greedyString())
								.executes(context -> remove(context.getSource(), StringArgumentType.getString(context, "nick")))))
				.then(literal("tier")
						.then(argument("nick", StringArgumentType.string())
								.then(argument("tier", IntegerArgumentType.integer(1, 5))
										.executes(context -> tier(context.getSource(),
												StringArgumentType.getString(context, "nick"),
												IntegerArgumentType.getInteger(context, "tier"))))))
				.then(literal("seal")
						.then(argument("nick", StringArgumentType.greedyString())
								.executes(context -> seal(context.getSource(), StringArgumentType.getString(context, "nick")))))
				.then(literal("scroll")
						.executes(context -> scroll(context.getSource())))
				.then(literal("book")
						.executes(context -> book(context.getSource())))
				.then(literal("info")
						.then(argument("ник", StringArgumentType.greedyString())
								.executes(context -> info(context.getSource(),
										StringArgumentType.getString(context, "ник")))))
				.then(literal("find")
						.then(argument("текст", StringArgumentType.greedyString())
								.executes(context -> find(context.getSource(),
										StringArgumentType.getString(context, "текст")))))
				.then(literal("top")
						.executes(context -> top(context.getSource(), 10))
						.then(argument("сколько", IntegerArgumentType.integer(1, 50))
								.executes(context -> top(context.getSource(),
										IntegerArgumentType.getInteger(context, "сколько")))))
				.then(literal("toggle")
						.then(argument("ник", StringArgumentType.greedyString())
								.executes(context -> toggle(context.getSource(),
										StringArgumentType.getString(context, "ник")))))
				.then(literal("aura")
						.executes(context -> aura(context.getSource(), null))
						.then(literal("on")
								.executes(context -> aura(context.getSource(), true)))
						.then(literal("off")
								.executes(context -> aura(context.getSource(), false))))
				.then(literal("import")
						.executes(context -> importList(context.getSource(), DEFAULT_IMPORT))
						.then(argument("файл", StringArgumentType.string())
								.executes(context -> importList(context.getSource(),
										StringArgumentType.getString(context, "файл")))))
				.then(literal("export")
						.executes(context -> exportList(context.getSource())))
				.then(literal("stats")
						.executes(context -> stats(context.getSource())));
	}

	private static int list(CommandSourceStack source, int page) {
		Catalog.page(source.getLevel(), page, message -> source.sendSuccess(() -> message, false));
		return 1;
	}

	private static int info(CommandSourceStack source, String rawNick) {
		String nick = rawNick.trim();
		PantheonData data = PantheonData.get(source.getLevel());
		Subscriber subscriber = data.find(nick).orElse(null);

		if (subscriber == null) {
			source.sendFailure(Component.literal("В летописи нет имени " + nick + ". Поиск: /attic find " + nick));
			return 0;
		}

		Catalog.card(source.getLevel(), subscriber, message -> source.sendSuccess(() -> message, false));
		return 1;
	}

	private static int find(CommandSourceStack source, String query) {
		Catalog.find(source.getLevel(), query.trim(), message -> source.sendSuccess(() -> message, false));
		return 1;
	}

	private static int top(CommandSourceStack source, int limit) {
		PantheonData data = PantheonData.get(source.getLevel());
		source.sendSuccess(() -> Component.literal("── Обласканные дарами ──").withStyle(ChatFormatting.GOLD), false);

		int shown = 0;

		for (Subscriber subscriber : data.mostHonoured(limit)) {
			if (subscriber.offerings() == 0) {
				break;
			}

			source.sendSuccess(() -> Component.literal("✦" + subscriber.offerings() + " ").withStyle(ChatFormatting.YELLOW)
					.append(Chat.name(subscriber)), false);
			shown++;
		}

		if (shown == 0) {
			source.sendSuccess(() -> Component.literal("Даров пока никто не приносил: щёлкни предметом по занятому святилищу.")
					.withStyle(ChatFormatting.GRAY), false);
		}

		return shown;
	}

	private static int toggle(CommandSourceStack source, String rawNick) {
		String nick = rawNick.trim();
		PantheonData data = PantheonData.get(source.getLevel());
		Subscriber subscriber = data.find(nick).orElse(null);

		if (subscriber == null) {
			source.sendFailure(Component.literal("В летописи нет имени " + nick + "."));
			return 0;
		}

		boolean nowActive = !subscriber.active();
		data.setActive(nick, nowActive);
		source.sendSuccess(() -> Component.literal(nowActive
				? "✦ " + subscriber.name() + " снова в строю: фигурка светится и считается в ауре."
				: "✧ " + subscriber.name() + " потух: место остаётся в истории, но аура его не считает.")
				.withStyle(nowActive ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY), true);
		return 1;
	}

	private static int add(CommandSourceStack source, String rawNick) {
		String nick = rawNick.trim();

		if (nick.isEmpty()) {
			source.sendFailure(Component.literal("Ник не может быть пустым."));
			return 0;
		}

		PantheonData data = PantheonData.get(source.getLevel());

		if (!data.add(nick, "ручная запись", 1)) {
			source.sendSuccess(() -> Component.literal(nick + " уже на чердаке.").withStyle(ChatFormatting.YELLOW), false);
			return 0;
		}

		Milestones.check(source.getLevel());
		source.sendSuccess(() -> Component.literal("✦ " + nick + " вписан в летопись.").withStyle(ChatFormatting.GOLD), true);
		return 1;
	}

	private static int remove(CommandSourceStack source, String rawNick) {
		String nick = rawNick.trim();
		PantheonData data = PantheonData.get(source.getLevel());

		if (!data.remove(nick)) {
			source.sendFailure(Component.literal("В летописи нет имени " + nick + "."));
			return 0;
		}

		source.sendSuccess(() -> Component.literal(nick + " вычеркнут из летописи.").withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int tier(CommandSourceStack source, String rawNick, int tier) {
		String nick = rawNick.trim();
		PantheonData data = PantheonData.get(source.getLevel());

		if (!data.setTier(nick, tier)) {
			source.sendFailure(Component.literal("В летописи нет имени " + nick + "."));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("Тир " + nick + " → " + tier + ".").withStyle(ChatFormatting.YELLOW), false);
		return 1;
	}

	private static int seal(CommandSourceStack source, String rawNick) throws CommandSyntaxException {
		String nick = rawNick.trim();
		ServerPlayer player = source.getPlayerOrException();
		ItemStack seal = Seals.named(nick);
		player.getInventory().placeItemBackInInventory(seal, Prediction.SERVER_ONLY);
		source.sendSuccess(() -> Component.literal("Печать с именем " + nick + " у тебя в руках. Щёлкни ею по постаменту.")
				.withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int scroll(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		player.getInventory().placeItemBackInInventory(new ItemStack(PantheonContent.NAME_SCROLL), Prediction.SERVER_ONLY);
		source.sendSuccess(() -> Component.literal("Свиток Имён выдан.").withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int book(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		ItemStack book = LedgerBook.create(source.getLevel());
		player.getInventory().placeItemBackInInventory(book, Prediction.SERVER_ONLY);
		source.sendSuccess(() -> Component.literal("Книга Имён обновлена: вся летопись чердака у тебя в руке.")
				.withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int aura(CommandSourceStack source, Boolean enable) {
		PantheonData data = PantheonData.get(source.getLevel());

		if (enable != null) {
			data.setAura(enable);
			source.sendSuccess(() -> Component.literal(enable
					? "Аура Пантеона включена: фигурки рядом снова поддерживают тебя."
					: "Аура Пантеона выключена.").withStyle(ChatFormatting.GOLD), true);
			return enable ? 1 : 0;
		}

		source.sendSuccess(() -> Component.literal("── Аура Пантеона ──").withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(() -> Component.literal("Состояние: " + (data.auraEnabled() ? "включена" : "выключена")
				+ " · святилищ с именами: " + data.shrines().size()
				+ " · радиус " + AuraService.RADIUS + " блоков").withStyle(ChatFormatting.YELLOW), false);

		if (source.getEntity() instanceof ServerPlayer player) {
			AuraTier tier = AuraService.tierOf(player);
			int count = AuraService.countNear(source.getLevel(), data, player);

			source.sendSuccess(() -> Component.literal("Рядом фигурок: " + count
					+ (tier == null ? " · аура спит" : " · ступень: " + tier.title()
							+ " (" + tier.effect().value().getDisplayName().getString() + ")")).withStyle(ChatFormatting.GRAY), false);
		}

		StringBuilder ladder = new StringBuilder("Ступени: ");

		for (AuraTier tier : AuraTier.values()) {
			if (ladder.length() > 10) {
				ladder.append(" · ");
			}

			ladder.append(tier.threshold()).append(" → ").append(tier.title());
		}

		final String ladderText = ladder.toString();
		source.sendSuccess(() -> Component.literal(ladderText).withStyle(ChatFormatting.DARK_GRAY), false);
		return data.shrines().size();
	}

	/** Импорт списка подписчиков: по одному нику в строке, можно "ник,тир,источник". */
	private static int importList(CommandSourceStack source, String fileName) {
		Path file = configPath(fileName);

		if (!Files.isReadable(file)) {
			source.sendFailure(Component.literal("Файл не найден: " + file
					+ " (положи список в папку config/chrdk_pantheon)"));
			return 0;
		}

		List<String> lines;

		try {
			lines = Files.readAllLines(file, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			source.sendFailure(Component.literal("Не удалось прочитать " + file + ": " + exception.getMessage()));
			return 0;
		}

		PantheonData data = PantheonData.get(source.getLevel());
		List<String> added = new ArrayList<>();
		int skipped = 0;

		for (String raw : lines) {
			String line = raw.trim();

			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			String[] parts = line.split(",");
			String nick = parts[0].trim();

			if (nick.isEmpty()) {
				continue;
			}

			int tier = parts.length > 1 ? parseTier(parts[1].trim()) : 1;
			String origin = parts.length > 2 ? parts[2].trim() : "импорт из файла";

			if (data.add(nick, origin, tier)) {
				added.add(nick);
			} else {
				skipped++;
			}
		}

		final int skippedCount = skipped;
		final List<String> imported = List.copyOf(added);

		if (!imported.isEmpty()) {
			Milestones.check(source.getLevel());
			source.sendSuccess(() -> Component.literal("✦ Импорт: в летопись вписано " + imported.size()
					+ " имён, пропущено (уже были) " + skippedCount + ".").withStyle(ChatFormatting.GOLD), true);
			source.sendSuccess(() -> Component.literal("Свежие имена: "
					+ String.join(", ", imported.subList(0, Math.min(8, imported.size())))
					+ (imported.size() > 8 ? "…" : "")).withStyle(ChatFormatting.GRAY), false);
		} else {
			source.sendSuccess(() -> Component.literal("Новых имён нет: все " + skippedCount + " уже на чердаке.")
					.withStyle(ChatFormatting.YELLOW), false);
		}

		return added.size();
	}

	/** Экспорт летописи в файл — чтобы править списки в блокноте. */
	private static int exportList(CommandSourceStack source) {
		PantheonData data = PantheonData.get(source.getLevel());
		Path file = configPath(DEFAULT_IMPORT);
		List<String> lines = new ArrayList<>();
		lines.add("# Летопись чердака: ник,тир,источник");
		lines.add("# Правится руками, затем /attic import");

		for (Subscriber subscriber : data.all()) {
			lines.add(subscriber.name() + "," + subscriber.tier() + "," + subscriber.source());
		}

		try {
			Files.createDirectories(file.getParent());
			Files.write(file, lines, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			source.sendFailure(Component.literal("Не удалось записать " + file + ": " + exception.getMessage()));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("Летопись выгружена: " + file + " (" + data.size() + " имён).")
				.withStyle(ChatFormatting.GOLD), false);
		return data.size();
	}

	private static Path configPath(String fileName) {
		String name = fileName.endsWith(".txt") ? fileName : fileName + ".txt";
		return FabricLoader.getInstance().getConfigDir().resolve("chrdk_pantheon").resolve(name);
	}

	private static int parseTier(String raw) {
		try {
			return Math.max(1, Math.min(5, Integer.parseInt(raw)));
		} catch (NumberFormatException ignored) {
			return 1;
		}
	}

	private static int stats(CommandSourceStack source) {
		PantheonData data = PantheonData.get(source.getLevel());
		source.sendSuccess(() -> Component.literal("── Чердак Бессмертных ──").withStyle(ChatFormatting.GOLD), false);
		source.sendSuccess(() -> Component.literal("Подписчиков: " + data.size()
				+ " · в строю: " + data.activeCount()
				+ " · потухло: " + (data.size() - data.activeCount())).withStyle(ChatFormatting.YELLOW), false);
		source.sendSuccess(() -> Component.literal("Святилищ с именами: " + data.shrines().size()
				+ " · даров принесено: " + data.totalOfferings()
				+ " · аура: " + (data.auraEnabled() ? "вкл" : "выкл")).withStyle(ChatFormatting.GRAY), false);
		source.sendSuccess(() -> Component.literal(Milestones.describe(source.getLevel())
				+ " · " + Milestones.nextHint(data)).withStyle(ChatFormatting.GRAY), false);
		return data.size();
	}

	private static final String DEFAULT_IMPORT = "subscribers.txt";
}
