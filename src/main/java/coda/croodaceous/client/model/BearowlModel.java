package coda.croodaceous.client.model;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.BearowlEntity;
import net.minecraft.resources.ResourceLocation;

public class BearowlModel extends SimpleGeoModel<BearowlEntity> {
	private final ResourceLocation texture_sleeping = new ResourceLocation(CroodaceousMod.MOD_ID, "textures/entity/bearowl_sleeping.png");
	
	public BearowlModel() {
		super(CroodaceousMod.MOD_ID, "bearowl");
	}
	
	@Override
	public ResourceLocation getTextureLocation(BearowlEntity entity) {
		return entity.isBearowlSleeping() ? texture_sleeping : super.getTextureLocation(entity);
	}
}