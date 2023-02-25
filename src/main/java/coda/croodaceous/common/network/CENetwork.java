package coda.croodaceous.common.network;

import coda.croodaceous.CroodaceousMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class CENetwork {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(CroodaceousMod.MOD_ID, "channel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int messageId = 0;
        CHANNEL.registerMessage(messageId++, ClientBoundTripGerbilPartnerPacket.class, ClientBoundTripGerbilPartnerPacket::toBytes, ClientBoundTripGerbilPartnerPacket::fromBytes, ClientBoundTripGerbilPartnerPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

    }
}
