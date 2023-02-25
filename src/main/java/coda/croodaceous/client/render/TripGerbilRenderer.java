package coda.croodaceous.client.render;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.client.layer.BearowlEyesLayer;
import coda.croodaceous.client.layer.TripGerbilTailLayer;
import coda.croodaceous.client.model.BearowlModel;
import coda.croodaceous.client.model.SimpleGeoModel;
import coda.croodaceous.common.entities.Bearowl;
import coda.croodaceous.common.entities.TripGerbil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class TripGerbilRenderer extends GeoEntityRenderer<TripGerbil> {

	public TripGerbilRenderer(EntityRendererProvider.Context mgr) {
		super(mgr, new SimpleGeoModel<>(CroodaceousMod.MOD_ID, "trip_gerbil"));
		this.addLayer(new TripGerbilTailLayer(this, new Vector3f(1.0F, 1.0F, 1.0F)));
	}


	
}