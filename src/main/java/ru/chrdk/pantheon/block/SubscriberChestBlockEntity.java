package ru.chrdk.pantheon.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import ru.chrdk.pantheon.gameplay.Offerings;
import ru.chrdk.pantheon.registry.PantheonContent;

/**
 * Сундук подписчика: личный ларь с биркой. Пока в него не заглянули — светится,
 * манит; на бирку вешается та же именная печать, что и на постамент.
 */
public class SubscriberChestBlockEntity extends BaseContainerBlockEntity implements ShrineBlockEntity {
	public static final int SIZE = 27;
	private static final Codec<String> NICK_CODEC = Codec.STRING;

	private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private String subscriber = "";
	private int tier = 1;
	private long offeredAt;
	private int offerings;
	private boolean opened;

	public SubscriberChestBlockEntity(BlockPos pos, BlockState state) {
		super(PantheonContent.SUBSCRIBER_CHEST_ENTITY, pos, state);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.chrdk_pantheon.subscriber_chest");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory inventory) {
		return new ChestMenu(MenuType.GENERIC_9x3, syncId, inventory, this, 3);
	}

	// ── «Святилище» ─────────────────────────────────────────────────────────

	@Override
	public String getSubscriber() {
		return subscriber;
	}

	@Override
	public int getTier() {
		return tier;
	}

	@Override
	public void setSubscriber(String subscriber, int tier) {
		this.subscriber = subscriber == null ? "" : subscriber;
		this.tier = Math.max(1, Math.min(5, tier));
		setChanged();
	}

	@Override
	public long getOfferedAt() {
		return offeredAt;
	}

	@Override
	public void setOfferedAt(long gameTime) {
		this.offeredAt = gameTime;
		setChanged();
	}

	@Override
	public int getOfferings() {
		return offerings;
	}

	@Override
	public void setOfferings(int offerings) {
		this.offerings = Math.max(0, offerings);
		setChanged();
	}

	public boolean isOpened() {
		return opened;
	}

	/** Первое открытие: сундук перестаёт светиться. */
	public void markOpened(ServerLevel level, BlockPos pos, BlockState state) {
		if (opened) {
			return;
		}

		opened = true;
		setChanged();

		if (state.hasProperty(SubscriberChestBlock.OPENED) && !state.getValue(SubscriberChestBlock.OPENED)) {
			BlockState updated = state.setValue(SubscriberChestBlock.OPENED, true);
			level.setBlockAndUpdate(pos, updated);
			level.sendBlockUpdated(pos, state, updated, net.minecraft.world.level.block.Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.7F, 1.2F);
		}
	}

	/** «Приветственный подарок»: положим пару даров внутрь, если ларей пуст. */
	public void addWelcomeGift(int tier, RandomSource random) {
		boolean empty = true;

		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				empty = false;
				break;
			}
		}

		if (!empty) {
			return;
		}

		int slot = 0;

		for (ItemStack gift : Offerings.roll(tier, random)) {
			while (slot < SIZE && !items.get(slot).isEmpty()) {
				slot++;
			}

			if (slot >= SIZE) {
				return;
			}

			items.set(slot, gift);
			slot++;
		}

		setChanged();
	}

	/** Всё, что лежало внутри, выпадает при сносе ларя. */
	public void dropContents(net.minecraft.world.level.Level level, BlockPos pos) {
		for (int slot = 0; slot < SIZE; slot++) {
			ItemStack stack = items.get(slot);

			if (!stack.isEmpty()) {
				net.minecraft.world.level.block.Block.popResource(level, pos, stack);
				items.set(slot, ItemStack.EMPTY);
			}
		}
	}

	// ── Сохранение ──────────────────────────────────────────────────────────

	@Override
	protected void loadAdditional(ValueInput data) {
		super.loadAdditional(data);
		ContainerHelper.loadAllItems(data, items);
		subscriber = data.read("subscriber", NICK_CODEC).orElse("");
		tier = data.getIntOr("tier", 1);
		offeredAt = data.getLongOr("offered_at", 0L);
		offerings = data.getIntOr("offerings", 0);
		opened = data.getBooleanOr("opened", false);
	}

	@Override
	protected void saveAdditional(ValueOutput data) {
		super.saveAdditional(data);
		ContainerHelper.saveAllItems(data, items);

		if (!subscriber.isEmpty()) {
			data.store("subscriber", NICK_CODEC, subscriber);
			data.store("tier", Codec.INT, tier);
		}

		if (offeredAt > 0L) {
			data.putLong("offered_at", offeredAt);
		}

		if (offerings > 0) {
			data.putInt("offerings", offerings);
		}

		if (opened) {
			data.putBoolean("opened", true);
		}
	}
}
