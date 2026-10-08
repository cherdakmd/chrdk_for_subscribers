package ru.chrdk.pantheon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Веха чердака: сколько имён набралось и когда это случилось. */
public record Milestone(int count, long at) {
	public static final Codec<Milestone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("count").forGetter(Milestone::count),
			Codec.LONG.optionalFieldOf("at", 0L).forGetter(Milestone::at)
	).apply(instance, Milestone::new));
}
