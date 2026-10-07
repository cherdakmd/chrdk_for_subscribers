package ru.chrdk.pantheon.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Общее содержимое любого святилища: кто здесь увековечен, какого тира и сколько даров принял.
 * Наследники — постамент под фигурку и портретная рама.
 */
public abstract class AbstractShrineBlockEntity extends BlockEntity implements ShrineBlockEntity {
	private static final Codec<String> NICK_CODEC = Codec.STRING;

	private String subscriber = "";
	private int tier = 1;
	private long offeredAt;
	private int offerings;

	protected AbstractShrineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

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

	@Override
	protected void loadAdditional(ValueInput data) {
		super.loadAdditional(data);
		subscriber = data.read("subscriber", NICK_CODEC).orElse("");
		tier = data.read("tier", Codec.INT).orElse(1);
		offeredAt = data.getLongOr("offered_at", 0L);
		offerings = data.getIntOr("offerings", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput data) {
		super.saveAdditional(data);

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
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}
}
