package ru.chrdk.pantheon.data;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import ru.chrdk.pantheon.util.Text;

/**
 * Вехи чердака: рубежи, которые отмечаются событием в мире и записью в летописи.
 */
public final class Milestones {
	/** Рубежи, которые чердак отмечает. */
	public static final List<Integer> THRESHOLDS = List.of(1, 10, 50, 100, 500, 1000);

	private Milestones() {
	}

	/** Ближайший непройденный рубеж, который уже достигнут (если есть). */
	public static Optional<Integer> nextReached(PantheonData data) {
		int size = data.size();

		for (int threshold : THRESHOLDS) {
			if (size >= threshold && data.milestones().stream().noneMatch(milestone -> milestone.count() == threshold)) {
				return Optional.of(threshold);
			}
		}

		return Optional.empty();
	}

	/** Записывает веху и объявляет её всем, кто на чердаке. */
	public static void celebrate(ServerLevel level, int threshold) {
		PantheonData data = PantheonData.get(level);
		long now = System.currentTimeMillis();

		if (!data.addMilestone(new Milestone(threshold, now))) {
			return;
		}

		Subscriber latest = data.latest(1).stream().findFirst().orElse(null);
		String gifted = latest == null ? "" : " Последним пришёл " + latest.name() + ".";
		Component headline = Component.literal("✦ Веха чердака: " + threshold + " имён!").withStyle(ChatFormatting.GOLD);
		Component details = Component.literal("Стены дрогнули, свечи вспыхнули." + gifted
				+ " (" + Text.date(now) + ")").withStyle(ChatFormatting.YELLOW);

		for (ServerPlayer player : level.players()) {
			player.sendSystemMessage(headline);
			player.sendSystemMessage(details);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.1F);
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.6F, 1.4F);
			level.sendParticles(ParticleTypes.END_ROD,
					player.getX(), player.getY() + 1.6D, player.getZ(),
					24, 0.6D, 0.8D, 0.6D, 0.03D);
		}
	}

	/** Короткая строка для книги и сводок. */
	public static String describe(ServerLevel level) {
		List<Milestone> milestones = PantheonData.get(level).milestones();

		if (milestones.isEmpty()) {
			return "вех пока нет";
		}

		Milestone last = milestones.get(milestones.size() - 1);
		return "последняя веха — " + last.count() + " (" + Text.date(last.at()) + ")";
	}

	/** Ближайший рубеж впереди — для подсказки. */
	public static String nextHint(PantheonData data) {
		for (int threshold : THRESHOLDS) {
			if (data.size() < threshold) {
				return "до вехи " + threshold + " осталось " + (threshold - data.size()) + " имён";
			}
		}

		return "все вехи пройдены";
	}

	/** Проверяет, не врезали ли в летопись новые имена, и празднует. */
	public static void check(ServerLevel level) {
		PantheonData data = PantheonData.get(level);
		Optional<Integer> reached;

		while ((reached = nextReached(data)).isPresent()) {
			celebrate(level, reached.get());
		}
	}
}
