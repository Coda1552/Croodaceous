package coda.croodaceous.client.render.layer;

import coda.croodaceous.CroodaceousMod;
import coda.croodaceous.common.entities.TripGerbil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class TripGerbilTailLayer extends GeoLayerRenderer<TripGerbil> {
    private static final ResourceLocation TRIP_GERBIL_TAIL_LOCATION = new ResourceLocation(CroodaceousMod.MOD_ID, "geo/entity/trip_gerbil_tail.geo.json");
    private final GeoModel tailModel;

    public TripGerbilTailLayer(IGeoRenderer<TripGerbil> entityRendererIn) {
        super(entityRendererIn);
        this.tailModel = getEntityModel().getModel(TRIP_GERBIL_TAIL_LOCATION);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, TripGerbil entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        final TripGerbil partner = entity.getPartner();
        if(partner != null && entity.isLeader()) {
            // draw line between self and partner
            matrixStackIn.pushPose();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            final float deltaY = 1.0F / 16.0F;

            final double startX = Mth.lerp(partialTicks, entity.xo, entity.getX());
            final double startY = Mth.lerp(partialTicks, entity.yo, entity.getY()) + deltaY;
            final double startZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
            final float offsetY = entity.isCrouching() ? -0.1875F : 0.0F;

            final double endX = Mth.lerp(partialTicks, partner.xo, partner.getX());
            final double endY = Mth.lerp(partialTicks, partner.yo, partner.getY()) + deltaY;
            final double endZ = Mth.lerp(partialTicks, partner.zo, partner.getZ());
            final float disX = (float)(startX - endX);
            final float disY = (float)(startY - endY) * -1 + offsetY + deltaY;
            final float disZ = (float)(startZ - endZ);
            final float dis = Mth.sqrt(disX * disX + disY * disY + disZ * disZ);

            // calculate rotations
            final Quaternion rotation = Vector3f.YP.rotation((float) Math.atan2(disX, disZ));
            rotation.mul(Vector3f.XP.rotation((float) Math.atan(-disY)));
            final Matrix4f rotationMatrix = new Matrix4f(rotation);

            // prepare to render
            final RenderType renderType = getRenderType(getEntityTexture(entity));
            final VertexConsumer vertexConsumer = bufferIn.getBuffer(renderType);

            for(int k = 0, n = (int) (dis * 16.0F / 4.5F); k <= n; ++k) {
                float startPercent = (float)k / n;
                modelVertex(matrixStackIn, bufferIn, renderType, vertexConsumer, packedLightIn, entity, partialTicks, disX, disY, disZ, startPercent, rotationMatrix);
            }

            matrixStackIn.popPose();
        }

    }

    private void modelVertex(PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, VertexConsumer vertexConsumer,
                             int packedLight, TripGerbil entity, float partialTick,
                             float disX, float disY, float disZ, float startPercent, Matrix4f rotation) {
        float x = disX * startPercent;
        float y = disY * startPercent;// (startPercent * startPercent + startPercent) * 0.5F + 0.25F;
        float z = disZ * startPercent;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPoseMatrix(rotation);

        getRenderer().render(tailModel,
                entity, partialTick, renderType, poseStack, bufferSource, vertexConsumer,
                packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0), 1, 1, 1, 1);
        poseStack.popPose();
    }
}
