package ru.chrdk.pantheon.block;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.data.Milestones;
import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.gameplay.AuraService;
import ru.chrdk.pantheon.gameplay.AuraTier;
import ru.chrdk.pantheon.gameplay.FigurineRitual;
import ru.chrdk.pantheon.item.SubscriberFigurineItem;
import ru.chrdk.pantheon.util.Text;

/**
 * Чердачный алтарь — сердце зала. Правый клик по нему рассказывает, сколько подписчиков
 * уже на полках и кто пришёл последним. А ещё у алтаря возвышают фигурки (v0.6):
 * две одинаковые фигурки одного ника и тира в двух руках плюс ритуальные ресурсы
 * из инвентаря превращаются в фигурку тира выше.
 */
public class AtticAltarBlock extends Block {
	public AtticAltarBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack other = player.getItemInHand(otherHand);

		String nick = SubscriberFigurineItem.nameOf(stack);
		String otherNick = SubscriberFigurineItem.nameOf(other);

		if (nick == null || otherNick == null) {
			// Не ритуал — пусть сработает сводка по чердаку.
			return InteractionResult.PASS;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		if (!nick.equalsIgnoreCase(otherNick)) {
			player.sendSystemMessage(Component.literal("У алтаря соединяются фигурки одного подписчика, "
					+ "а в руках — разные имена.").withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		int tier = SubscriberFigurineItem.tierOf(stack);
		int otherTier = SubscriberFigurineItem.tierOf(other);

		if (tier != otherTier) {
			player.sendSystemMessage(Component.literal("Фигурки " + nick + " разного тира (" + tier
					+ " и " + otherTier + ") — сначала уравняй тиры резцом или новым возвышением.")
					.withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		if (tier >= FigurineRitual.MAX_TIER) {
			player.sendSystemMessage(Component.literal("Фигурка " + nick + " уже " + FigurineRitual.MAX_TIER
					+ "-го тира — выше некуда.").withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		List<FigurineRitual.Cost> missing = FigurineRitual.missingCosts(player, tier);

		if (!missing.isEmpty()) {
			player.sendSystemMessage(Component.literal("Не хватает для ритуала: "
					+ FigurineRitual.describe(missing) + ".").withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		stack.shrink(1);
		other.shrink(1);
		FigurineRitual.consumeCosts(player, tier);
		player.getInventory().placeItemBackInInventory(SubscriberFigurineItem.named(nick, tier + 1), Prediction.SERVER_ONLY);

		serverLevel.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.9F, 1.2F);
		serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.6F);
		serverLevel.sendParticles(ParticleTypes.END_ROD,
				pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
				12, 0.3D, 0.3D, 0.3D, 0.02D);
		serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
				pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
				8, 0.4D, 0.4D, 0.4D, 0.0D);

		player.sendSystemMessage(Component.literal("✦ " + nick + " возвышен у алтаря: фигурка тира "
				+ tier + " → " + (tier + 1) + ".").withStyle(ChatFormatting.GOLD));
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel) {
			PantheonData data = PantheonData.get(serverLevel);

			player.sendSystemMessage(Component.literal("── Чердак Бессмертных ──").withStyle(ChatFormatting.GOLD));
			player.sendSystemMessage(Component.literal("Подписчиков на полках: " + data.size()
					+ " (в строю: " + data.activeCount() + ")").withStyle(ChatFormatting.YELLOW));

			List<Subscriber> latest = data.latest(3);

			if (latest.isEmpty()) {
				player.sendSystemMessage(Component.literal("Полки пусты. Смастери печать, назови её в наковальне и щёлкни по постаменту.")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else {
				for (Subscriber subscriber : latest) {
					player.sendSystemMessage(Component.literal("  · " + subscriber.name()
							+ " — тир " + subscriber.tier()
							+ ", с " + Text.date(subscriber.addedAt())).withStyle(ChatFormatting.GRAY));
				}
			}

			if (player instanceof ServerPlayer serverPlayer) {
				AuraTier tier = AuraService.tierOf(serverPlayer);
				int near = AuraService.countNear(serverLevel, data, serverPlayer);
				player.sendSystemMessage(Component.literal("Очков ауры рядом: " + near
						+ (tier == null ? " · аура спит" : " · " + tier.title()
								+ " (" + tier.effect().value().getDisplayName().getString() + ")"))
						.withStyle(ChatFormatting.GRAY));
			}

			player.sendSystemMessage(Component.literal("Святилищ с именами: " + data.shrines().size()
					+ " · даров принесено: " + data.totalOfferings()
					+ " · " + Milestones.nextHint(data)).withStyle(ChatFormatting.GRAY));

			level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.5F, 1.4F);
		}

		return InteractionResult.SUCCESS;
	}
}
