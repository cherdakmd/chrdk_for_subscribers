package ru.chrdk.pantheon.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import ru.chrdk.pantheon.PantheonMod;

/**
 * Летопись чердака: все подписчики, когда-либо попавшие в мир.
 * Хранится прямо в мире (работает и в одиночной игре без сервера).
 */
public class PantheonData extends SavedData {
	private static final Codec<PantheonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Subscriber.CODEC)
					.optionalFieldOf("subscribers", Map.of())
					.forGetter(PantheonData::snapshot)
	).apply(instance, PantheonData::new));

	public static final SavedDataType<PantheonData> TYPE = new SavedDataType<>(
			PantheonMod.id("attic_ledger"), PantheonData::new, CODEC, null);

	private final Map<String, Subscriber> subscribers = new HashMap<>();

	public PantheonData() {
	}

	private PantheonData(Map<String, Subscriber> loaded) {
		this.subscribers.putAll(loaded);
	}

	public static PantheonData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	private Map<String, Subscriber> snapshot() {
		return Map.copyOf(subscribers);
	}

	public int size() {
		return subscribers.size();
	}

	public int activeCount() {
		int count = 0;

		for (Subscriber subscriber : subscribers.values()) {
			if (subscriber.active()) {
				count++;
			}
		}

		return count;
	}

	public Optional<Subscriber> find(String name) {
		return Optional.ofNullable(subscribers.get(name.toLowerCase(Locale.ROOT)));
	}

	public boolean add(String name, String source, int tier) {
		String key = name.toLowerCase(Locale.ROOT);

		if (subscribers.containsKey(key)) {
			return false;
		}

		subscribers.put(key, Subscriber.fresh(name, source, System.currentTimeMillis(), tier));
		setDirty();
		return true;
	}

	public boolean remove(String name) {
		if (subscribers.remove(name.toLowerCase(Locale.ROOT)) != null) {
			setDirty();
			return true;
		}

		return false;
	}

	public boolean setTier(String name, int tier) {
		Subscriber existing = subscribers.get(name.toLowerCase(Locale.ROOT));

		if (existing == null) {
			return false;
		}

		subscribers.put(existing.key(), existing.withTier(tier));
		setDirty();
		return true;
	}

	public boolean setActive(String name, boolean active) {
		Subscriber existing = subscribers.get(name.toLowerCase(Locale.ROOT));

		if (existing == null) {
			return false;
		}

		subscribers.put(existing.key(), existing.withActive(active));
		setDirty();
		return true;
	}

	/** Все подписчики в порядке появления на чердаке. */
	public List<Subscriber> all() {
		List<Subscriber> list = new ArrayList<>(subscribers.values());
		list.sort(Comparator.comparingLong(Subscriber::addedAt).thenComparing(Subscriber::name));
		return list;
	}

	/** Последние пришедшие — первыми в списке. */
	public List<Subscriber> latest(int limit) {
		List<Subscriber> list = all();
		list.sort(Comparator.comparingLong(Subscriber::addedAt).reversed());
		return list.size() > limit ? list.subList(0, limit) : list;
	}
}
