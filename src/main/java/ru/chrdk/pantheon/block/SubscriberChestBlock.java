package ru.chrdk.pantheon.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.item.Seals;

/**
 * Сундук подписчика — ларь с биркой. Пустой правый клик открывает крышку,
 * именная печать вешает на ларь имя подписчика (внутри появляется приветственный
 * подарок), Shift + правый клик — снять бирку. Пока ларь не открывали, он светится.
 */
public class SubscriberChestBlock extends AtticShrineBlock {
	/** Открывали ли ларь: пока нет — он подсвечивается, как будто внутри ждут. */
	public static final BooleanProperty OPENED = BooleanProperty.create("opened");

	public SubscriberChestBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(OPENED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OPENED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SubscriberChestBlockEntity(pos, state);
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		// Печать — это бирка ларя; любой другой предмет просто открывает сундук.
		if (Seals.nameOf(stack) == null) {
			return InteractionResult.PASS;
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		if (level.getBlockEntity(pos) instanceof MenuProvider provider) {
			player.openMenu(provider);
		}

		if (level.getBlockEntity(pos) instanceof SubscriberChestBlockEntity chest) {
			String nick = chest.getSubscriber();

			if (nick.isEmpty()) {
				player.sendSystemMessage(Component.literal("Ларь без бирки: переименуй печать в наковальне и щёлкни ею по ларю.")
						.withStyle(ChatFormatting.DARK_GRAY));
			} else if (!chest.isOpened()) {
				player.sendSystemMessage(Component.literal("✦ Ларь " + nick + " открыт впервые — внутри лежит приветственный подарок.")
						.withStyle(ChatFormatting.GOLD));
			}

			chest.markOpened(serverLevel, pos, state);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (level.getBlockEntity(pos) instanceof SubscriberChestBlockEntity chest) {
			chest.dropContents(level, pos);
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	protected Component emptyHint() {
		return Component.literal("Пустой ларь. Переименуй печать в наковальне и щёлкни ею, чтобы подписать ларь подписчику.");
	}

	@Override
	protected Component onEnshrined(String nick, int tier) {
		return Component.literal("✦ Ларь подписан: " + nick + " (тир " + tier + "). Внутри оставлен приветственный подарок.");
	}

	@Override
	protected Component onReleased(String nick) {
		return Component.literal("Бирка " + nick + " снята с ларя.");
	}

	@Override
	protected void afterEnshrine(ServerLevel level, BlockPos pos, ShrineBlockEntity shrine, String nick, int tier) {
		if (!(shrine instanceof SubscriberChestBlockEntity chest)) {
			return;
		}

		chest.addWelcomeGift(tier, level.getRandom());
		level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.8F, 1.1F);
		level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.4F);

		// Ларь снова «манит»: его ещё не открывали.
		BlockState state = level.getBlockState(pos);

		if (state.getValue(OPENED)) {
			level.setBlockAndUpdate(pos, state.setValue(OPENED, false));
		}
	}
}
