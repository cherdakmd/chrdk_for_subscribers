package ru.chrdk.pantheon.client.figurine;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import ru.chrdk.pantheon.PantheonMod;

/**
 * Развёртка для портрета: только голова и слой волос/шапки.
 *
 * <p>UV берём как у ванильной PlayerModel, поэтому передняя грань головы попадает ровно
 * на область лица в текстуре скина, а слой шляпы — на слой волос.
 */
public final class PortraitLayers {
	public static final ModelLayerLocation HEAD = new ModelLayerLocation(PantheonMod.id("portrait_head"), "main");

	private PortraitLayers() {
	}

	public static LayerDefinition createHead() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
				PartPose.ZERO);
		root.addOrReplaceChild("hat",
				CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 64);
	}
}
