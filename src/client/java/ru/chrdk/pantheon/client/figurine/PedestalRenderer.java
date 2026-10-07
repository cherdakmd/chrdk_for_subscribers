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
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import ru.chrdk.pantheon.block.AtticShrineBlock;
import ru.chrdk.pantheon.block.PedestalBlockEntity;

/**
 * Рисует фигурку подписчика на постаменте.
 *
 * <p>Модель игрока построена «вниз головой» (так устроены все модели Minecraft), поэтому
 * {@link FigurineModel} переворачивает её. Ноги фигурки ставим ровно на крышку постамента,
 * а размер зависит от «тира» подписчика.
 */
public class PedestalRenderer implements BlockEntityRenderer<PedestalBlockEntity, FigurineRenderState> {
	/** Доля роста игрока (2 блока) для фигурки: тир 1 — маленькая, тир 5 — почти в полный рост человека. */
	private static final float BASE_SCALE = 0.40F;
	private static final float TIER_SCALE_STEP = 0.04F;

	private final FigurineModel classic;
	private final FigurineModel slim;
	private final PlayerSkinRenderCache skinCache;

	public PedestalRenderer(BlockEntityRendererProvider.Context context) {
		this.classic = new FigurineModel(context.bakeLayer(FigurineLayers.CLASSIC));
		this.slim = new FigurineModel(context.bakeLayer(FigurineLayers.SLIM));
		this.skinCache = context.playerSkinRenderCache();
	}

	@Override
	public FigurineRenderState createRenderState() {
		return new FigurineRenderState();
	}

	@Override
	public void extractRenderState(PedestalBlockEntity blockEntity, FigurineRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

		state.nick = blockEntity.getSubscriber();
		state.tier = blockEntity.getTier();

		BlockState blockState = blockEntity.getBlockState();
		state.facing = blockState.hasProperty(AtticShrineBlock.FACING)
				? blockState.getValue(AtticShrineBlock.FACING)
				: Direction.NORTH;

		if (state.nick.isEmpty()) {
			state.texture = null;
			state.slim = false;
			return;
		}

		FigurineSkins.Resolution skin = FigurineSkins.resolve(state.nick, this.skinCache);
		state.texture = skin.texture();
		state.slim = skin.slim();
	}

	@Override
	public void submit(FigurineRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraState) {
		if (state.texture == null || state.nick.isEmpty()) {
			return;
		}

		float scale = BASE_SCALE + TIER_SCALE_STEP * Mth.clamp(state.tier, 1, 5);

		poseStack.pushPose();
		// Ноги модели лежат на 1.5 блока ниже начала координат (высота модели игрока),
		// поэтому поднимаем фигурку так, чтобы ступни встали на крышку постамента (y = 1).
		poseStack.translate(0.5D, 1.0D + 1.5D * scale, 0.5D);
		poseStack.rotateDegrees(Axis.YP, yawFor(state.facing));
		poseStack.scale(scale, scale, scale);

		submitNodeCollector.submitModel(
				state.slim ? this.slim : this.classic,
				Unit.INSTANCE,
				poseStack,
				state.texture,
				state.lightCoords,
				OverlayTexture.NO_OVERLAY,
				0);

		poseStack.popPose();
	}

	/** Фигурка стоит лицом туда же, куда смотрел игрок, ставивший постамент. */
	private static float yawFor(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180.0F;
			case WEST -> 90.0F;
			case EAST -> -90.0F;
			default -> 0.0F;
		};
	}
}
