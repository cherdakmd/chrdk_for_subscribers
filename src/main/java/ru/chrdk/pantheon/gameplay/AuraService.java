package ru.chrdk.pantheon.gameplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

import ru.chrdk.pantheon.block.AtticShrineBlock;
import ru.chrdk.pantheon.block.ShrineBlockEntity;
import ru.chrdk.pantheon.block.SubscriberChestBlock;
import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;

/**
 * Аура Пантеона: считает фигурки и портреты подписчиков вокруг игрока и выдаёт баффы
 * по ступеням {@link AuraTier}. Рост канала = сила в мире.
 *
 * <p>Пересчёт раз в 5 секунд, чтобы не гонять поиск по святилищам каждый тик.
 */
public final class AuraService {
	/** Радиус, в котором фигурки «слышат» игрока. */
	public static final int RADIUS = 16;
	private static final int PERIOD = 100;
	private static final int DURATION = 240;
	private static final Map<UUID, AuraTier> CURRENT = new ConcurrentHashMap<>();

	private AuraService() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(AuraService::tickLevel);
	}

	private static void tickLevel(ServerLevel level) {
		if (level.getGameTime() % PERIOD != 0) {
			return;
		}

		PantheonData data = PantheonData.get(level);
		forgetLostShrines(level, data);

		for (ServerPlayer player : level.players()) {
			if (!data.auraEnabled()) {
				fade(player, "Аура Пантеона погашена: её выключили.");
				continue;
			}

			int count = countNear(level, data, player);
			AuraTier tier = AuraTier.forCount(count);

			if (tier == null) {
				fade(player, "Аура Пантеона остыла: фигурок рядом нет.");
				continue;
			}

			player.addEffect(new MobEffectInstance(tier.effect(), DURATION, tier.amplifier(), true, true, true));

			AuraTier previous = CURRENT.put(player.getUUID(), tier);

			if (previous != tier) {
				player.sendSystemMessage(Component.literal("Аура Пантеона: " + tier.title()
						+ " — рядом " + count + " фигурок (" + tier.effect().value().getDisplayName().getString() + ")")
						.withStyle(ChatFormatting.GOLD));
			}

			// Тихие искорки вокруг — чтобы аура чувствовалась, а не только читалась в чате.
			if (level.getGameTime() % (PERIOD * 4) == 0) {
				level.sendParticles(ParticleTypes.END_ROD,
						player.getX(), player.getY() + 1.4D, player.getZ(),
						3, 0.5D, 0.5D, 0.5D, 0.01D);
			}
		}
	}

	/** Сколько активных подписчиков «стоит» рядом с игроком. */
	public static int countNear(ServerLevel level, PantheonData data, ServerPlayer player) {
		BlockPos origin = player.blockPosition();
		int count = 0;

		for (BlockPos pos : data.shrines()) {
			if (!pos.closerThan(origin, RADIUS) || !level.isLoaded(pos)) {
				continue;
			}

			// Ларь подписчика — тоже святилище, но ауру дают именно фигурки и портреты.
			if (level.getBlockState(pos).getBlock() instanceof SubscriberChestBlock) {
				continue;
			}

			if (!(level.getBlockEntity(pos) instanceof ShrineBlockEntity shrine) || shrine.getSubscriber().isEmpty()) {
				continue;
			}

			boolean active = data.find(shrine.getSubscriber()).map(Subscriber::active).orElse(true);

			if (active) {
				count++;
			}
		}

		return count;
	}

	/** Выкидывает из летописи святилища, которых в мире больше нет. */
	public static void forgetLostShrines(ServerLevel level, PantheonData data) {
		List<BlockPos> stale = new ArrayList<>();

		for (BlockPos pos : data.shrines()) {
			if (!level.isLoaded(pos)) {
				continue;
			}

			var state = level.getBlockState(pos);

			if (!(state.getBlock() instanceof AtticShrineBlock) || !state.getValue(AtticShrineBlock.OCCUPIED)) {
				stale.add(pos);
			}
		}

		data.forgetShrines(stale);
	}

	private static void fade(ServerPlayer player, String message) {
		if (CURRENT.remove(player.getUUID()) != null) {
			player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	/** Текущая ступень игрока — для команд и табличек. */
	public static AuraTier tierOf(ServerPlayer player) {
		return CURRENT.get(player.getUUID());
	}

	/** Забываем игрока, когда он выходит, чтобы не копить мусор. */
	public static void forget(UUID playerId) {
		CURRENT.remove(playerId);
	}
}
