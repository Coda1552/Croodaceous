package coda.croodaceous.common.network;

import coda.croodaceous.common.entities.TripGerbil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Sent from the server to the client to update the client-side partner
 **/
public class ClientBoundTripGerbilPartnerPacket {

    protected int entity;
    protected int partner;

    /**
     * @param entityId the entity ID
     * @param partnerId the entity ID of the partner, or -1 to remove
     */
    public ClientBoundTripGerbilPartnerPacket(final int entityId, final int partnerId) {
        this.entity = entityId;
        this.partner = partnerId;
    }

    /**
     * Reads the raw packet data from the data stream.
     *
     * @param buf the PacketBuffer
     * @return a new instance of a ClientBoundTripGerbilPartnerPacket based on the PacketBuffer
     */
    public static ClientBoundTripGerbilPartnerPacket fromBytes(final FriendlyByteBuf buf) {
        final int entityId = buf.readInt();
        final int partnerId = buf.readInt();
        return new ClientBoundTripGerbilPartnerPacket(entityId, partnerId);
    }

    /**
     * Writes the raw packet data to the data stream.
     *
     * @param msg the ClientBoundTripGerbilPartnerPacket
     * @param buf the PacketBuffer
     */
    public static void toBytes(final ClientBoundTripGerbilPartnerPacket msg, final FriendlyByteBuf buf) {
        buf.writeInt(msg.entity);
        buf.writeInt(msg.partner);
    }

    /**
     * Handles the packet when it is received.
     *
     * @param message         the ClientBoundTripGerbilPartnerPacket
     * @param contextSupplier the NetworkEvent.Context supplier
     */
    public static void handlePacket(final ClientBoundTripGerbilPartnerPacket message, final Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            context.enqueueWork(() -> {
                // load client level
                Level clientLevel = ClientNetworkUtils.getClientLevel();
                // locate entity and partner
                TripGerbil entity = (TripGerbil) clientLevel.getEntity(message.entity);
                TripGerbil partner = (TripGerbil) clientLevel.getEntity(message.partner);
                // add as partners
                if(entity != null) {
                    entity.setClientPartner(partner);
                }
            });
        }
        context.setPacketHandled(true);
    }
}
