package ru.chrdk.pantheon.block;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.command.Catalog;
import ru.chrdk.pantheon.data.LedgerBook;
import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.util.Text;

/**
 * Обелиск Имён — каталог чердака. Правый клик печатает (или обновляет) Книгу Имён
 * и рассказывает, сколько имён уже на полках.
 */
public class ObeliskBlock extends Block {
	public ObeliskBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		PantheonData data = PantheonData.get(serverLevel);

		player.sendSystemMessage(Component.literal("── Обелиск Имён ──").withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(LedgerBook.summary(serverLevel));

		if (data.size() == 0) {
			player.sendSystemMessage(Component.literal("Полки пусты: летопись ещё не начата.").withStyle(ChatFormatting.DARK_GRAY));
		} else {
			Catalog.page(serverLevel, 1, player::sendSystemMessage);
			player.sendSystemMessage(Component.literal("Клик по имени — карточка подписчика (/attic info).")
					.withStyle(ChatFormatting.DARK_GRAY));
		}

		ItemStack book = LedgerBook.create(serverLevel);
		player.getInventory().placeItemBackInInventory(book, Prediction.SERVER_ONLY);
		player.sendSystemMessage(Component.literal("Книга Имён обновлена — читай в руке.").withStyle(ChatFormatting.GOLD));

		level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.3F);
		level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.4F, 1.6F);
		return InteractionResult.SUCCESS;
	}
}
