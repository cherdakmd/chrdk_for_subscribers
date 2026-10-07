package ru.chrdk.pantheon.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import ru.chrdk.pantheon.data.PantheonData;
import ru.chrdk.pantheon.data.Subscriber;
import ru.chrdk.pantheon.item.Seals;
import ru.chrdk.pantheon.util.Text;

/**
 * Общая механика «святилищ»: постамент с фигуркой и портретная рама.
 *
 * <p>Ритуал один и тот же: переименованную в наковальне печать щёлкаем по святилищу —
 * имя впечатывается и попадает в летопись. Shift + правый клик — снять и забрать печать.
 */
public abstract class AtticShrineBlock extends BaseEntityBlock {
	public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	protected AtticShrineBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(OCCUPIED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(OCCUPIED, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		// Святилище смотрит на игрока, который его поставил.
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	/** Подсказка, когда святилище пусто. */
	protected abstract Component emptyHint();

	/** Сообщение, когда имя впечатано. */
	protected abstract Component onEnshrined(String nick, int tier);

	/** Сообщение, когда фигурку сняли. */
	protected abstract Component onReleased(String nick);

	@Override
	public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ShrineBlockEntity shrine)) {
			return InteractionResult.PASS;
		}

		if (state.getValue(OCCUPIED)) {
			if (player.isShiftKeyDown()) {
				release(level, pos, state, shrine, player);
			} else {
				describe(level, player, shrine);
			}

			return InteractionResult.SUCCESS;
		}

		String nick = Seals.nameOf(stack);

		if (nick == null) {
			return InteractionResult.PASS;
		}

		if (level instanceof ServerLevel serverLevel) {
			enshrine(serverLevel, pos, state, shrine, player, stack, nick);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ShrineBlockEntity shrine)) {
			return InteractionResult.PASS;
		}

		if (!state.getValue(OCCUPIED)) {
			if (level instanceof ServerLevel) {
				player.sendSystemMessage(emptyHint().copy().withStyle(ChatFormatting.DARK_GRAY));
			}

			return InteractionResult.SUCCESS;
		}

		if (player.isShiftKeyDown()) {
			release(level, pos, state, shrine, player);
		} else {
			describe(level, player, shrine);
		}

		return InteractionResult.SUCCESS;
	}

	/** Печатает имя подписчика на святилище. */
	private void enshrine(ServerLevel level, BlockPos pos, BlockState state, ShrineBlockEntity shrine, Player player, ItemStack stack, String nick) {
		PantheonData data = PantheonData.get(level);
		Subscriber subscriber = data.find(nick).orElse(null);

		if (subscriber == null) {
			data.add(nick, "ручная печать", 1);
			subscriber = data.find(nick).orElse(null);
		}

		int tier = subscriber == null ? 1 : subscriber.tier();
		shrine.setSubscriber(nick, tier);
		level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, true));
		// Отправляем клиентам данные (ник и тир), иначе фигурка не появится сразу.
		level.sendBlockUpdated(pos, state, level.getBlockState(pos), Block.UPDATE_ALL);
		stack.shrink(1);

		level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.3F);
		level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 0.7F);
		level.sendParticles(ParticleTypes.SMOKE,
				pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
				14, 0.25D, 0.2D, 0.25D, 0.01D);
		level.sendParticles(ParticleTypes.END_ROD,
				pos.getX() + 0.5D, pos.getY() + 1.2D, pos.getZ() + 0.5D,
				8, 0.2D, 0.3D, 0.2D, 0.02D);

		player.sendSystemMessage(onEnshrined(nick, tier).copy().withStyle(ChatFormatting.GOLD));
	}

	/** Снимает фигурку и возвращает печать с именем. */
	private void release(Level level, BlockPos pos, BlockState state, ShrineBlockEntity shrine, Player player) {
		if (!(level instanceof ServerLevel)) {
			return;
		}

		String nick = shrine.getSubscriber();
		shrine.setSubscriber("", 1);
		level.setBlockAndUpdate(pos, state.setValue(OCCUPIED, false));
		level.sendBlockUpdated(pos, state, level.getBlockState(pos), Block.UPDATE_ALL);

		if (nick.isEmpty()) {
			return;
		}

		player.getInventory().placeItemBackInInventory(Seals.named(nick), Prediction.SERVER_ONLY);
		level.playSound(null, pos, SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
		player.sendSystemMessage(onReleased(nick).copy().withStyle(ChatFormatting.GRAY));
	}

	/** Рассказывает, кто здесь увековечен. */
	private void describe(Level level, Player player, ShrineBlockEntity shrine) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		String nick = shrine.getSubscriber();

		if (nick.isEmpty()) {
			player.sendSystemMessage(Component.literal("Святилище занято, но имя стёрлось.").withStyle(ChatFormatting.DARK_GRAY));
			return;
		}

		Subscriber subscriber = PantheonData.get(serverLevel).find(nick).orElse(null);

		if (subscriber == null) {
			player.sendSystemMessage(Component.literal("Фигурка: " + nick).withStyle(ChatFormatting.GOLD));
			return;
		}

		String status = subscriber.active() ? "в строю" : "потух";
		player.sendSystemMessage(Component.literal("Фигурка: " + subscriber.name()
				+ " · тир " + subscriber.tier()
				+ " · на чердаке с " + Text.date(subscriber.addedAt())
				+ " · " + subscriber.source()
				+ " · " + status).withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(Component.literal("Shift + правый клик — снять и забрать печать.").withStyle(ChatFormatting.DARK_GRAY));
	}
}
