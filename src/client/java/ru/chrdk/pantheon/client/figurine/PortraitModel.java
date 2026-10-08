package ru.chrdk.pantheon.client.figurine;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

/** Модель портрета: голова подписчика. Ничего не анимируем — смотрит прямо. */
public class PortraitModel extends Model<Unit> {
	public PortraitModel(ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
	}
}
