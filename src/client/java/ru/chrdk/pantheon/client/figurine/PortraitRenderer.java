package ru.chrdk.pantheon.client.figurine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import ru.chrdk.pantheon.block.AtticShrineBlock;
import ru.chrdk.pantheon.block.PortraitBlockEntity;

/**
 * Рисует в раме голову подписчика: настоящий скин игрока или процедурный аватар по нику.
 */
public class PortraitRenderer implements BlockEntityRenderer<PortraitBlockEntity, PortraitRenderState> {
	/** Размер головы: 1.0 — череп в натуральную величину (0.5 блока). */
	private static final float HEAD_SCALE = 0.9F;

	private final PortraitModel model;
	private final PlayerSkinRenderCache skinCache;

	public PortraitRenderer(BlockEntityRendererProvider.Context context) {
		this.model = new PortraitModel(context.bakeLayer(PortraitLayers.HEAD));
		this.skinCache = context.playerSkinRenderCache();
	}

	@Override
	public PortraitRenderState createRenderState() {
		return new PortraitRenderState();
	}

	@Override
	public void extractRenderState(PortraitBlockEntity blockEntity, PortraitRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.nick = blockEntity.getSubscriber();

		BlockState blockState = blockEntity.getBlockState();
		state.facing = blockState.hasProperty(AtticShrineBlock.FACING)
				? blockState.getValue(AtticShrineBlock.FACING)
				: Direction.NORTH;

		if (state.nick.isEmpty()) {
			state.texture = null;
			return;
		}

		state.texture = FigurineSkins.resolve(state.nick, this.skinCache).texture();
	}

	@Override
	public void submit(PortraitRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraState) {
		if (state.texture == null || state.nick.isEmpty()) {
			return;
		}

		poseStack.pushPose();
		// Точка отсчёта модели головы — на уровне подбородка (череп растёт вверх от неё),
		// поэтому ставим её так, чтобы затылок лёг на задник рамы, а лицо смотрело из проёма.
		poseStack.translate(0.5D, 0.28D, 0.5D);
		poseStack.rotateDegrees(Axis.YP, yawFor(state.facing));
		// Чуть вглубь рамы: задник занимает дальнюю четверть блока.
		poseStack.translate(0.0D, 0.0D, 0.03D);
		poseStack.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE);

		submitNodeCollector.submitModel(
				this.model,
				Unit.INSTANCE,
				poseStack,
				state.texture,
				state.lightCoords,
				OverlayTexture.NO_OVERLAY,
				0);

		poseStack.popPose();
	}

	/** Портрет смотрит туда же, куда смотрел игрок, ставивший раму. */
	private static float yawFor(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180.0F;
			case WEST -> 90.0F;
			case EAST -> -90.0F;
			default -> 0.0F;
		};
	}
}
