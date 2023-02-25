package coda.croodaceous.client.layer;

import coda.croodaceous.common.entities.TripGerbil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class TripGerbilTailLayer extends GeoLayerRenderer<TripGerbil> {
    // TODO fix the line rendering (incorrect positions? idk)
    private final Vector3f color;

    public TripGerbilTailLayer(IGeoRenderer<TripGerbil> entityRendererIn, final Vector3f color) {
        super(entityRendererIn);
        this.color = color;
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, TripGerbil entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        final TripGerbil partner = entity.getPartner();
        if(partner != null && entity.isLeader()) {
            // draw line between self and partner
            matrixStackIn.pushPose();
            int i = 0;

            float yRot = Mth.lerp(partialTicks, partner.yBodyRotO, partner.yBodyRot) * ((float)Math.PI / 180F);
            double dX = Mth.sin(yRot);
            double dZ = Mth.cos(yRot);
            double d2 = (double)i * 0.35D; // TODO replace with 0 ?

            final float dy = 0; //-0.15F;

            double startX = Mth.lerp(partialTicks, partner.xo, partner.getX()) - dZ * d2 - dX * 0.8D;
            double startY = partner.yo + (double)partner.getEyeHeight() + (partner.getY() - partner.yo) * partialTicks - 0.45D;
            double startZ = Mth.lerp(partialTicks, partner.zo, partner.getZ()) - dX * d2 + dZ * 0.8D;
            float offsetY = partner.isCrouching() ? -0.1875F : 0.0F;

            double d9 = Mth.lerp(partialTicks, partner.xo, partner.getX());
            double d10 = Mth.lerp(partialTicks, partner.yo, partner.getY()) + 0.25D;
            double d8 = Mth.lerp(partialTicks, partner.zo, partner.getZ());
            float f4 = (float)(startX - d9);
            float f5 = (float)(startY - d10) + offsetY + dy;
            float f6 = (float)(startZ - d8);
            VertexConsumer vertexconsumer1 = bufferIn.getBuffer(RenderType.lineStrip());
            PoseStack.Pose matrixStackIn$pose1 = matrixStackIn.last();

            for(int k = 0; k <= 16; ++k) {
                stringVertex(f4, f5, f6, vertexconsumer1, matrixStackIn$pose1, (float)k / 16.0F, (float)(k + 1.0F) / 16.0F, this.color);
            }

            matrixStackIn.popPose();
        }

    }

    public static void stringVertex(float x, float y, float z, VertexConsumer vertexConsumer, PoseStack.Pose pose, float startPercent, float endPercent, final Vector3f color) {
        float f = x * startPercent;
        float f1 = y * (startPercent * startPercent + startPercent) * 0.5F + 0.25F;
        float f2 = z * startPercent;
        float f3 = x * endPercent - f;
        float f4 = y * (endPercent * endPercent + endPercent) * 0.5F + 0.25F - f1;
        float f5 = z * endPercent - f2;
        float f6 = Mth.sqrt(f3 * f3 + f4 * f4 + f5 * f5);
        f3 /= f6;
        f4 /= f6;
        f5 /= f6;
        vertexConsumer.vertex(pose.pose(), f, f1, f2).color(color.x(), color.y(), color.z(), 1.0F).normal(pose.normal(), f3, f4, f5).endVertex();
    }
}
