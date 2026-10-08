package ru.chrdk.pantheon.client.figurine;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Данные одного кадра для отрисовки портрета. */
public class PortraitRenderState extends BlockEntityRenderState {
	public String nick = "";
	public Direction facing = Direction.NORTH;
	public @Nullable Identifier texture;
}
