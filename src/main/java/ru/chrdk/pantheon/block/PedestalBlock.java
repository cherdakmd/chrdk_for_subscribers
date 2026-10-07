package ru.chrdk.pantheon.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.item.Seals;
import ru.chrdk.pantheon.util.Text;

/**
 * Постамент — место подписчика на чердаке.
 *
 * <p>Ритуал: пустую печать переименовываем в наковальне в ник подписчика, щёлкаем ею по постаменту —
 * печать «впечатывается», а над постаментом появляется статуэтка. Shift + правый клик снимает фигурку
 * и возвращает печать с именем (данные не теряются).
 */
public class PedestalBlock extends BaseEntityBlock {
	public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

	public PedestalBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(OCCUPIED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OCCUPIED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PedestalBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
			return InteractionResult.PASS;
		}

		if (state.getValue(OCCUPIED)) {
			if (player.isShiftKeyDown()) {
				release(level, pos, state, pedestal, player);
			} else {
				describe(level, player, pedestal);
			}

			return InteractionResult.SUCCESS;
		}

		String nick = Seals.nameOf(stack);

		if (nick == null) {
			return InteractionResult.PASS;
		}

		if (level instanceof ServerLevel serverLevel) {
			enshrine(serverLevel, pos, state, pedestal, player, stack, nick);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) {
			return InteractionResult.PASS;
		}

		if (!state.getValue(OCCUPIED)) {
			if (level instanceof ServerLevel) {
				player.sendSystemMessage(Component.literal("Постамент пуст. Возьми именную печать (переименуй пустую в наковальне) и щёлкни по нему.")
						.withStyle(ChatFormatting.DARK_GRAY));
			}

			return InteractionResult.SUCCESS;
		}

		if (player.isShiftKeyDown()) {
			release(level, pos, state, pedestal, player);
		} else {
			describe(level, player, pedestal);
		}

		return InteractionResult.SUCCESS;
	}

	/** Печатает имя подписчика на постаменте. */
	private void enshrine(ServerLevel level, BlockPos pos, BlockState state, PedestalBlockEntity pedestal, Player player, ItemStack stack, String nick) {
		PantheonData data = PantheonData.get(level);
		Subscriber subscriber = data.find(nick).orElse(null);

		if (subscriber == null) {
			data.add(nick, "ручная печать", 1);
			subscriber = data.find(nick).orElse(null);
		}

		pedestal.setSubscriber(nick);
		level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
		stack.shrink(1);

		level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.3F);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 0.7F);
		level.sendParticles(ParticleTypes.SMOKE,
				pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
				14, 0.25D, 0.2D, 0.25D, 0.01D);
		level.sendParticles(ParticleTypes.END_ROD,
				pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D,
				8, 0.2D, 0.3D, 0.2D, 0.02D);

		int tier = subscriber == null ? 1 : subscriber.tier();
		player.sendSystemMessage(Component.literal("✦ " + nick + " водружён на постамент (тир " + tier + "). Чердак помнит.")
				.withStyle(ChatFormatting.GOLD));
	}

	/** Снимает фигурку и возвращает печать с именем. */
	private void release(Level level, BlockPos pos, BlockState state, PedestalBlockEntity pedestal, Player player) {
		if (!(level instanceof ServerLevel)) {
			return;
		}

		String nick = pedestal.getSubscriber();
		pedestal.setSubscriber("");
		level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, false));

		if (nick.isEmpty()) {
			return;
		}

		player.getInventory().placeItemBackInInventory(Seals.named(nick), Prediction.SERVER_ONLY);
		level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
		player.sendSystemMessage(Component.literal("Фигурка " + nick + " снята с постамента, печать вернулась в руки.")
				.withStyle(ChatFormatting.GRAY));
	}

	/** Рассказывает, кто стоит на постаменте. */
	private void describe(Level level, Player player, PedestalBlockEntity pedestal) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		String nick = pedestal.getSubscriber();

		if (nick.isEmpty()) {
			player.sendSystemMessage(Component.literal("Постамент занят, но имя стёрлось.").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}

		Subscriber subscriber = PantheonData.get(serverLevel).find(nick).orElse(null);

		if (subscriber == null) {
			player.sendSystemMessage(Component.literal("Фигурка: " + nick).withStyle(ChatFormatting.GOLD));
			return;
		}

		String state = subscriber.active() ? "в строю" : "потух";
		player.sendSystemMessage(Component.literal("Фигурка: " + subscriber.name()
				+ " · тир " + subscriber.tier()
				+ " · на чердаке с " + Text.date(subscriber.addedAt())
				+ " · " + subscriber.source()
				+ " · " + state).withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(Component.literal("Shift + правый клик — снять фигурку и забрать печать.").withStyle(ChatFormatting.DARK_GRAY));
	}
}
