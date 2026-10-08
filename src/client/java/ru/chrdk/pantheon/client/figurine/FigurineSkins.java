package ru.chrdk.pantheon.client.figurine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.component.ResolvableProfile;

import ru.chrdk.pantheon.PantheonMod;

/**
 * Откуда берётся внешность фигурки.
 *
 * <ol>
 *   <li>Если ник похож на майнкрафт-аккаунт и у него есть скин — берём настоящий скин игрока
 *       (через ванильный кэш скинов, без собственных запросов к сети).</li>
 *   <li>Иначе — процедурный аватар: один из набора сгенерированных скинов, выбранный по нику.
 *       Он всегда одинаковый для одного и того же имени.</li>
 * </ol>
 */
public final class FigurineSkins {
	/** Сколько процедурных скинов лежит в ресурсах мода. */
	public static final int VARIANTS = 48;

	private static final Pattern MC_NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");
	private static final Map<String, Entry> ENTRIES = new HashMap<>();

	private FigurineSkins() {
	}

	/** Что рисовать и каким телосложением. */
	public record Resolution(Identifier texture, boolean slim) {
	}

	private static final class Entry {
		private final @Nullable ResolvableProfile profile;
		private boolean checked;
		private @Nullable Identifier skin;
		private boolean slim;

		private Entry(@Nullable ResolvableProfile profile) {
			this.profile = profile;
		}
	}

	public static Resolution resolve(String nick, PlayerSkinRenderCache cache) {
		String trimmed = nick.trim();
		String key = trimmed.toLowerCase(Locale.ROOT);
		Entry entry = ENTRIES.get(key);

		if (entry == null) {
			ResolvableProfile profile = MC_NAME.matcher(trimmed).matches() ? ResolvableProfile.createUnresolved(trimmed) : null;
			entry = new Entry(profile);
			ENTRIES.put(key, entry);
		}

		if (entry.profile != null && !entry.checked) {
			check(entry, cache);
		}

		if (entry.skin != null) {
			return new Resolution(entry.skin, entry.slim);
		}

		return procedural(key);
	}

	/** Спрашивает ванильный кэш, есть ли у ника настоящий скин (и можно ли больше не спрашивать). */
	private static void check(Entry entry, PlayerSkinRenderCache cache) {
		try {
			CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>> future = cache.lookup(entry.profile);

			if (!future.isDone() || future.isCompletedExceptionally() || future.isCancelled()) {
				return;
			}

			entry.checked = true;
			Optional<PlayerSkinRenderCache.RenderInfo> result = future.getNow(Optional.empty());

			if (result.isPresent()) {
				PlayerSkinRenderCache.RenderInfo info = result.get();
				entry.skin = info.playerSkin().body().texturePath();
				entry.slim = info.playerSkin().model() == PlayerModelType.SLIM;
			}
		} catch (RuntimeException exception) {
			// Ника нет, сеть недоступна или профиль не резолвится — остаёмся на процедурном аватаре.
			entry.checked = true;
		}
	}

	private static Resolution procedural(String key) {
		int index = Math.floorMod(stableHash(key), VARIANTS);
		boolean slim = index % 4 == 0;
		Identifier texture = PantheonMod.id(String.format("figurine/skin_%02d", index));
		return new Resolution(texture, slim);
	}

	/** FNV-1a: одинаковый ник всегда даёт одинаковый аватар, независимо от версии игры. */
	private static int stableHash(String value) {
		int hash = 0x811C9DC5;

		for (int i = 0; i < value.length(); i++) {
			hash ^= value.charAt(i);
			hash *= 0x01000193;
		}

		return hash;
	}
}
