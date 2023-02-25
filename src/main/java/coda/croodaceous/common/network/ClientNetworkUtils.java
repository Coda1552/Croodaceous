package coda.croodaceous.common.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class ClientNetworkUtils {

    public static Level getClientLevel() {
        return net.minecraft.client.Minecraft.getInstance().level;
    }

    public static Optional<Player> getClientPlayer() {
        return Optional.ofNullable(net.minecraft.client.Minecraft.getInstance().player);
    }
}
