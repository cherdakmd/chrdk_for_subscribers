package ru.chrdk.pantheon.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import ru.chrdk.pantheon.registry.PantheonContent;

/** Данные постамента: чьё имя впечатано и какого размера фигурку ставить. */
public class PedestalBlockEntity extends BlockEntity {
	private static final Codec<String> NICK_CODEC = Codec.STRING;
	private String subscriber = "";
	private int tier = 1;

	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		super(PantheonContent.PEDESTAL_ENTITY, pos, state);
	}

	public String getSubscriber() {
		return subscriber;
	}

	public int getTier() {
		return tier;
	}

	public void setSubscriber(String subscriber, int tier) {
		this.subscriber = subscriber == null ? "" : subscriber;
		this.tier = Math.max(1, Math.min(5, tier));
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput data) {
		super.loadAdditional(data);
		subscriber = data.read("subscriber", NICK_CODEC).orElse("");
		tier = data.read("tier", Codec.INT).orElse(1);
	}

	@Override
	protected void saveAdditional(ValueOutput data) {
		super.saveAdditional(data);

		if (!subscriber.isEmpty()) {
			data.store("subscriber", NICK_CODEC, subscriber);
			data.store("tier", Codec.INT, tier);
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
