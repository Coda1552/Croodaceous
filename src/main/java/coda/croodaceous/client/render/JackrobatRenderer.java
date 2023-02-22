package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.model.JackrobatModel;
import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.Jackrobat;
import coda.croodaceous.common.entities.Liyote;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class JackrobatRenderer extends GeoEntityRenderer<Jackrobat> {

	public JackrobatRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new JackrobatModel());
	}


	
}