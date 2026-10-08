package ru.chrdk.pantheon.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Имя и тир, записанные на фигурке подписчика.
 * Лежит в компоненте предмета, поэтому фигурка не теряет личность ни при крафте,
 * ни при переименовании на наковальне.
 */
public record FigurineData(String nick, int tier) {
	public static final Codec<FigurineData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("nick").forGetter(FigurineData::nick),
			Codec.INT.fieldOf("tier").forGetter(FigurineData::tier)
	).apply(instance, FigurineData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, FigurineData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, FigurineData::nick,
			ByteBufCodecs.VAR_INT, FigurineData::tier,
			FigurineData::new);
}
