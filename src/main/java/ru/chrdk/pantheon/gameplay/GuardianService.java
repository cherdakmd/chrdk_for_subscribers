package ru.chrdk.pantheon.gameplay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;

/**
 * Хранители чердака: во время рейда топ-подписчики оживают и дерутся за игрока.
 *
 * <p>Если рядом с игроком идёт рейд и стоят активные фигурки и портреты (аура не спит),
 * чердак выставляет до трёх Хранителей — приручённых игроком волков в доспехах цвета тира,
 * с именем «Хранитель &lt;ник&gt;». Хранитель сражается вместе с игроком и уходит на полку,
 * когда рейд окончен или подошло время жизни.
 */
public final class GuardianService {
	/** Пересчёт раз в 10 секунд — чаще не нужно. */
	private static final int PERIOD = 200;
	/** Хранителей на одного игрока во время рейда. */
	private static final int MAX_PER_PLAYER = 3;
	/** Время жизни Хранителя — 10 игровых минут. */
	private static final long MAX_LIFE_TICKS = 12000L;
	private static final double MESSAGE_RADIUS = 48.0D;
	/** Тег, чтобы найти Хранителя после перезапуска мира. */
	public static final String GUARDIAN_TAG = "chrdk_guardian";

	private static final Map<Integer, Guardian> GUARDIANS = new ConcurrentHashMap<>();

	/** Живой Хранитель: волк, ник, тир, когда призван, чей, в каком измерении. */
	private record Guardian(int wolfId, String nick, int tier, long spawnedAt, UUID ownerId, ResourceKey<Level> dimension) {
	}

	private GuardianService() {
	}

	public static void register() {
		ServerTickEvents.END_LEVEL_TICK.register(GuardianService::tickLevel);
	}

	private static void tickLevel(ServerLevel level) {
		if (level.getGameTime() % PERIOD != 0) {
			return;
		}

		recall(level);
		summon(level);
	}

	/** Убирает Хранителей этого измерения, у которых рейд окончен или вышло время жизни. */
	private static void recall(ServerLevel level) {
		Iterator<Map.Entry<Integer, Guardian>> iterator = GUARDIANS.entrySet().iterator();

		while (iterator.hasNext()) {
			Guardian guardian = iterator.next().getValue();

			if (!guardian.dimension().equals(level.dimension())) {
				continue;
			}

			Entity wolf = level.getEntity(guardian.wolfId());

			if (wolf == null || !wolf.isAlive()) {
				// Умер в бою или недоступен — просто снимаем с учёта.
				iterator.remove();
				continue;
			}

			boolean expired = level.getGameTime() - guardian.spawnedAt() > MAX_LIFE_TICKS;
			boolean raidOver = level.getRaidAt(wolf.blockPosition()) == null;

			if (!expired && !raidOver) {
				continue;
			}

			BlockPos pos = wolf.blockPosition();
			wolf.discard();
			level.sendParticles(ParticleTypes.POOF,
					pos.getX() + 0.5D, pos.getY() + 0.6D, pos.getZ() + 0.5D,
					10, 0.3D, 0.3D, 0.3D, 0.01D);
			level.playSound(null, pos, SoundEvents.WOLF_WHINE, SoundSource.NEUTRAL, 0.8F, 1.2F);
			broadcast(level, pos, "Хранитель " + guardian.nick() + " вернулся на полку.", ChatFormatting.GRAY);
			iterator.remove();
		}
	}

	/** Призывает Хранителей игрокам, которых рейд застал рядом с их чердаком. */
	private static void summon(ServerLevel level) {
		PantheonData data = PantheonData.get(level);

		for (ServerPlayer player : level.players()) {
			if (player.isSpectator() || player.isCreative()) {
				continue;
			}

			if (level.getRaidAt(player.blockPosition()) == null) {
				continue;
			}

			// Аура не спит — рядом активные фигурки и портреты.
			if (AuraService.countNear(level, data, player) <= 0) {
				continue;
			}

			int existing = countGuardiansOf(level, player.getUUID());

			if (existing >= MAX_PER_PLAYER) {
				continue;
			}

			for (Subscriber champion : champions(data, MAX_PER_PLAYER)) {
				if (existing >= MAX_PER_PLAYER) {
					break;
				}

				if (hasGuardian(level, champion.name())) {
					continue;
				}

				if (spawn(level, player, champion)) {
					existing++;
				}
			}
		}
	}

	/** Топ подписчиков чердака: сначала обласканные дарами, потом по тиру и давности. */
	private static List<Subscriber> champions(PantheonData data, int limit) {
		List<Subscriber> active = new ArrayList<>();

		for (Subscriber subscriber : data.all()) {
			if (subscriber.active()) {
				active.add(subscriber);
			}
		}

		active.sort(Comparator.comparingInt(Subscriber::offerings).reversed()
				.thenComparing(Comparator.comparingInt(Subscriber::tier).reversed())
				.thenComparingLong(Subscriber::addedAt));

		return active.size() > limit ? active.subList(0, limit) : active;
	}

	/** Призывает одного Хранителя рядом с игроком. */
	private static boolean spawn(ServerLevel level, ServerPlayer player, Subscriber subscriber) {
		Entity entity = EntityType.WOLF.create(level);

		if (!(entity instanceof TamableAnimal wolf)) {
			return false;
		}

		BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				player.blockPosition().offset(level.random.nextInt(7) - 3, 0, level.random.nextInt(7) - 3));
		wolf.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player.getYRot(), 0.0F);
		wolf.tame(player);
		wolf.setCustomName(Component.literal("Хранитель " + subscriber.name()).withStyle(ChatFormatting.GOLD));
		wolf.setCustomNameVisible(true);
		wolf.setItemSlot(EquipmentSlot.BODY, dyedArmor(subscriber.tier()));
		wolf.addTag(GUARDIAN_TAG);

		if (!level.addFreshEntity(wolf)) {
			return false;
		}

		GUARDIANS.put(wolf.getId(), new Guardian(wolf.getId(), subscriber.name(), subscriber.tier(),
				level.getGameTime(), player.getUUID(), level.dimension()));

		level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_WOLF, SoundSource.NEUTRAL, 0.9F, 1.1F);
		level.sendParticles(ParticleTypes.END_ROD,
				pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
				10, 0.3D, 0.3D, 0.3D, 0.02D);
		broadcast(level, pos, "✦ Хранитель " + subscriber.name() + " (тир " + subscriber.tier()
				+ ") пришёл на помощь!", ChatFormatting.GOLD);
		return true;
	}

	/** Доспех Хранителя, покрашенный в цвет тира подписчика. */
	private static ItemStack dyedArmor(int tier) {
		int rgb = switch (tier) {
			case 2 -> 0xF9801D;
			case 3 -> 0x3AB3DA;
			case 4 -> 0x8932B8;
			case 5 -> 0xB02E26;
			default -> 0xF0F0F0;
		};

		ItemStack armor = new ItemStack(Items.WOLF_ARMOR);
		armor.set(DataComponents.DYED_COLOR, new DyedItemColor(rgb, false));
		return armor;
	}

	private static int countGuardiansOf(ServerLevel level, UUID ownerId) {
		int count = 0;

		for (Guardian guardian : GUARDIANS.values()) {
			if (guardian.dimension().equals(level.dimension()) && guardian.ownerId().equals(ownerId)) {
				count++;
			}
		}

		return count;
	}

	private static boolean hasGuardian(ServerLevel level, String nick) {
		for (Guardian guardian : GUARDIANS.values()) {
			if (guardian.dimension().equals(level.dimension()) && guardian.nick().equalsIgnoreCase(nick)) {
				return true;
			}
		}

		return false;
	}

	/** Сообщение ближайшим игрокам. */
	private static void broadcast(ServerLevel level, BlockPos pos, String message, ChatFormatting color) {
		Component text = Component.literal(message).withStyle(color);

		for (ServerPlayer nearby : level.players()) {
			if (nearby.blockPosition().closerThan(pos, MESSAGE_RADIUS)) {
				nearby.sendSystemMessage(text);
			}
		}
	}
}
