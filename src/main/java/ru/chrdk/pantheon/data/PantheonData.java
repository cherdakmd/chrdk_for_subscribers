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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import ru.chrdk.pantheon.PantheonMod;

/**
 * Летопись чердака: все подписчики, когда-либо попавшие в мир, координаты «святилищ»
 * (постаментов и рам с именами) и пройденные вехи.
 * Хранится прямо в мире (работает и в одиночной игре без сервера).
 */
public class PantheonData extends SavedData {
	private static final Codec<PantheonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Subscriber.CODEC)
					.optionalFieldOf("subscribers", Map.of())
					.forGetter(PantheonData::snapshot),
			Codec.LONG.listOf()
					.optionalFieldOf("shrines", List.of())
					.forGetter(PantheonData::shrineSnapshot),
			Milestone.CODEC.listOf()
					.optionalFieldOf("milestones", List.of())
					.forGetter(PantheonData::milestones),
			Codec.BOOL.optionalFieldOf("aura", true)
					.forGetter(PantheonData::auraEnabled)
	).apply(instance, PantheonData::new));

	public static final SavedDataType<PantheonData> TYPE = new SavedDataType<>(
			PantheonMod.id("attic_ledger"), PantheonData::new, CODEC, null);

	private final Map<String, Subscriber> subscribers = new HashMap<>();
	private final List<BlockPos> shrines = new ArrayList<>();
	private final List<Milestone> milestones = new ArrayList<>();
	private boolean aura = true;

	public PantheonData() {
	}

	private PantheonData(Map<String, Subscriber> loaded, List<Long> shrinePositions, List<Milestone> milestones, boolean aura) {
		this.subscribers.putAll(loaded);

		for (long packed : shrinePositions) {
			this.shrines.add(BlockPos.of(packed));
		}

		this.milestones.addAll(milestones);
		this.aura = aura;
	}

	public static PantheonData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	private Map<String, Subscriber> snapshot() {
		return Map.copyOf(subscribers);
	}

	private List<Long> shrineSnapshot() {
		List<Long> packed = new ArrayList<>(shrines.size());

		for (BlockPos pos : shrines) {
			packed.add(pos.asLong());
		}

		return packed;
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

	/** Отмечает подношение, принесённое подписчику. */
	public int addOffering(String name) {
		Subscriber existing = subscribers.get(name.toLowerCase(Locale.ROOT));

		if (existing == null) {
			return 0;
		}

		Subscriber updated = existing.withOfferings(existing.offerings() + 1);
		subscribers.put(existing.key(), updated);
		setDirty();
		return updated.offerings();
	}

	// ── Святилища (постаменты и рамы с именами) ─────────────────────────────

	/** Позиции занятых святилищ — по ним считает аура и обелиск. */
	public List<BlockPos> shrines() {
		return List.copyOf(shrines);
	}

	public void trackShrine(BlockPos pos) {
		if (!shrines.contains(pos)) {
			shrines.add(pos);
			setDirty();
		}
	}

	public void untrackShrine(BlockPos pos) {
		if (shrines.remove(pos)) {
			setDirty();
		}
	}

	/** Забывает святилища, которых больше нет (блок сломали или имя сняли). */
	public void forgetShrines(List<BlockPos> stale) {
		if (!stale.isEmpty() && shrines.removeAll(stale)) {
			setDirty();
		}
	}

	// ── Вехи ────────────────────────────────────────────────────────────────

	public List<Milestone> milestones() {
		return List.copyOf(milestones);
	}

	public boolean addMilestone(Milestone milestone) {
		if (milestones.stream().anyMatch(existing -> existing.count() == milestone.count())) {
			return false;
		}

		milestones.add(milestone);
		milestones.sort(Comparator.comparingInt(Milestone::count));
		setDirty();
		return true;
	}

	// ── Аура ────────────────────────────────────────────────────────────────

	public boolean auraEnabled() {
		return aura;
	}

	public void setAura(boolean enabled) {
		this.aura = enabled;
		setDirty();
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

	/** Подписчики по убыванию числа подношений — для обелиска. */
	public List<Subscriber> mostHonoured(int limit) {
		List<Subscriber> list = all();
		list.sort(Comparator.comparingInt(Subscriber::offerings).reversed().thenComparing(Subscriber::name));
		return list.size() > limit ? list.subList(0, limit) : list;
	}

	public int totalOfferings() {
		int total = 0;

		for (Subscriber subscriber : subscribers.values()) {
			total += subscriber.offerings();
		}

		return total;
	}
}
