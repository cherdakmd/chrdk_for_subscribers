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

/** Данные постамента: чьё имя впечатано. */
public class PedestalBlockEntity extends BlockEntity {
	private static final Codec<String> NICK_CODEC = Codec.STRING;
	private String subscriber = "";

	public PedestalBlockEntity(BlockPos pos, BlockState state) {
		super(PantheonContent.PEDESTAL_ENTITY, pos, state);
	}

	public String getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber == null ? "" : subscriber;
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput data) {
		super.loadAdditional(data);
		subscriber = data.read("subscriber", NICK_CODEC).orElse("");
	}

	@Override
	protected void saveAdditional(ValueOutput data) {
		super.saveAdditional(data);

		if (!subscriber.isEmpty()) {
			data.store("subscriber", NICK_CODEC, subscriber);
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
