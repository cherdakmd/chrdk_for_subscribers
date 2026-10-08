package ru.chrdk.pantheon.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;

import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * Персональная фигурка подписчика — предмет ритуального крафта (v0.6).
 *
 * <p>Фигурка хранит ник и тир в компоненте {@link FigurineData}: имя переносится с печати
 * при отливке в верстаке, а тир растёт от возвышений у алтаря. Фигурка водружается на
 * постамент, где обновляет летопись, а снятие возвращает фигурку с тем же ником и тиром.
 *
 * <p>Понижение тира — Чердачным резцом через наковальню: фигурка в одной руке,
 * резец в другой, правый клик по наковальне.
 */
public final class SubscriberFigurineItem extends Item {
	public SubscriberFigurineItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	/** Фигурка с именем и тиром: имя — как на печати, тир — для размера и ауры. */
	public static ItemStack named(String nick, int tier) {
		ItemStack stack = new ItemStack(PantheonContent.SUBSCRIBER_FIGURINE);
		stack.set(PantheonContent.FIGURINE, new FigurineData(nick, clampTier(tier)));
		stack.set(DataComponents.CUSTOM_NAME,
				Component.literal("Фигурка " + nick).withStyle(ChatFormatting.GOLD));
		return stack;
	}

	/** Ник с фигурки или null, если фигурка пустая. Компонент — истина, имя на наковальне — лишь для вида. */
	public static String nameOf(ItemStack stack) {
		if (!isFigurine(stack)) {
			return null;
		}

		FigurineData data = stack.get(PantheonContent.FIGURINE);

		if (data != null && !data.nick().isBlank()) {
			return data.nick();
		}

		// Фигурки из v0.5.x хранили имя только в имени предмета.
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			String name = stack.getHoverName().getString().trim();
			String nick = name.startsWith("Фигурка ") ? name.substring(8).trim() : name;
			return nick.isEmpty() ? null : nick;
		}

		return null;
	}

	/** Тир фигурки (1..5). */
	public static int tierOf(ItemStack stack) {
		if (!isFigurine(stack)) {
			return 1;
		}

		FigurineData data = stack.get(PantheonContent.FIGURINE);
		return data == null ? 1 : clampTier(data.tier());
	}

	public static boolean isFigurine(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == PantheonContent.SUBSCRIBER_FIGURINE;
	}

	public static int clampTier(int tier) {
		return Math.max(1, Math.min(5, tier));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
			Consumer<Component> consumer, TooltipFlag flag) {
		String nick = nameOf(stack);

		if (nick == null) {
			consumer.accept(Component.translatable("item.chrdk_pantheon.subscriber_figurine.unnamed")
					.withStyle(ChatFormatting.DARK_GRAY));
		} else {
			consumer.accept(Component.translatable("item.chrdk_pantheon.subscriber_figurine.tier", tierOf(stack))
					.withStyle(ChatFormatting.GRAY));
		}
	}

	/**
	 * Понижение тира через наковальню: фигурка в одной руке, Чердачный резец в другой.
	 * Без резца — обычная наковальня, заявка не перехватывается.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();

		if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof AnvilBlock)) {
			return InteractionResult.PASS;
		}

		Player player = context.getPlayer();

		if (player == null) {
			return InteractionResult.PASS;
		}

		InteractionHand otherHand = context.getHand() == InteractionHand.MAIN_HAND
				? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack chisel = player.getItemInHand(otherHand);

		if (!chisel.is(PantheonContent.ATTIC_CHISEL)) {
			return InteractionResult.PASS;
		}

		ItemStack figurine = context.getItemInHand();
		String nick = nameOf(figurine);

		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		if (nick == null) {
			player.sendSystemMessage(Component.literal("У фигурки нет имени — опускать нечего.")
					.withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		int tier = tierOf(figurine);

		if (tier <= 1) {
			player.sendSystemMessage(Component.literal("Фигурка " + nick + " и так первого тира — ниже некуда.")
					.withStyle(ChatFormatting.DARK_GRAY));
			return InteractionResult.SUCCESS;
		}

		player.setItemInHand(context.getHand(), named(nick, tier - 1));
		chisel.shrink(1);

		serverLevel.playSound(null, context.getClickedPos(), SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.7F, 1.2F);
		player.sendSystemMessage(Component.literal("✧ Фигурка " + nick + " опущена резцом до тира " + (tier - 1) + ".")
				.withStyle(ChatFormatting.GRAY));
		return InteractionResult.SUCCESS;
	}
}
