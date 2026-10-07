package ru.chrdk.pantheon.gameplay;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * Ступени Ауры Пантеона: чем больше фигурок и портретов подписчиков рядом
 * (в радиусе {@link AuraService#RADIUS} блоков), тем сильнее чердак поддерживает игрока.
 */
public enum AuraTier {
	HEARTH(1, MobEffects.SPEED, 0, "Чердачный уют"),
	RHYTHM(10, MobEffects.HASTE, 0, "Рабочий гул"),
	WALLS(50, MobEffects.RESISTANCE, 0, "Крепкие стены"),
	BREATH(100, MobEffects.REGENERATION, 0, "Дыхание хранителей"),
	MIGHT(500, MobEffects.STRENGTH, 0, "Мощь Пантеона");

	private final int threshold;
	private final Holder<MobEffect> effect;
	private final int amplifier;
	private final String title;

	AuraTier(int threshold, Holder<MobEffect> effect, int amplifier, String title) {
		this.threshold = threshold;
		this.effect = effect;
		this.amplifier = amplifier;
		this.title = title;
	}

	public int threshold() {
		return threshold;
	}

	public Holder<MobEffect> effect() {
		return effect;
	}

	public int amplifier() {
		return amplifier;
	}

	public String title() {
		return title;
	}

	/** Ступень для текущего числа фигурок рядом (null — аура ещё не проснулась). */
	public static AuraTier forCount(int count) {
		AuraTier best = null;

		for (AuraTier tier : values()) {
			if (count >= tier.threshold) {
				best = tier;
			}
		}

		return best;
	}
}
