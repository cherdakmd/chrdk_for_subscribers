package ru.chrdk.pantheon.data;

import java.util.Locale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Запись о подписчике в летописи чердака.
 *
 * @param name    ник подписчика в том виде, как он будет выбит на табличке
 * @param source  откуда пришёл (ручная запись, импорт, эфир)
 * @param addedAt когда попал на чердак (миллисекунды, epoch)
 * @param tier    «тир» — насколько богатая полка/большая статуэтка (1..5)
 * @param active  активен ли подписчик сейчас (отписавшиеся остаются, но гаснут)
 * @param offerings сколько даров принесли подписчику (каждый дар — счётчик обелиска)
 */
public record Subscriber(String name, String source, long addedAt, int tier, boolean active, int offerings) {
	public static final Codec<Subscriber> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("name").forGetter(Subscriber::name),
			Codec.STRING.optionalFieldOf("source", "ручная запись").forGetter(Subscriber::source),
			Codec.LONG.optionalFieldOf("added_at", 0L).forGetter(Subscriber::addedAt),
			Codec.INT.optionalFieldOf("tier", 1).forGetter(Subscriber::tier),
			Codec.BOOL.optionalFieldOf("active", true).forGetter(Subscriber::active),
			Codec.INT.optionalFieldOf("offerings", 0).forGetter(Subscriber::offerings)
	).apply(instance, Subscriber::new));

	public static Subscriber fresh(String name, String source, long addedAt, int tier) {
		return new Subscriber(name, source, addedAt, tier, true, 0);
	}

	/** Ключ, по которому ищем подписчика: ник без учёта регистра. */
	public String key() {
		return name.toLowerCase(Locale.ROOT);
	}

	public Subscriber withTier(int newTier) {
		return new Subscriber(name, source, addedAt, newTier, active, offerings);
	}

	public Subscriber withActive(boolean newActive) {
		return new Subscriber(name, source, addedAt, tier, newActive, offerings);
	}

	public Subscriber withOfferings(int newOfferings) {
		return new Subscriber(name, source, addedAt, tier, active, newOfferings);
	}
}
