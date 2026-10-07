package ru.chrdk.pantheon.client.figurine;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

/**
 * Фигурка подписчика: обычная гуманоидная модель, которую разворачиваем «на ноги».
 *
 * <p>Модели в Minecraft строятся в перевёрнутой системе координат (голова — в минус по Y),
 * поэтому, как и ванильная статуя медноголема, фигурка переворачивается через {@code zRot}.
 */
public class FigurineModel extends Model<Unit> {
	public FigurineModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
	}

	@Override
	public void setupAnim(Unit ignored) {
		this.root.zRot = (float) Math.PI;
	}
}
