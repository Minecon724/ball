package pl.minecon724.ball;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.entity.Player;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.player.PlayerConnection;

public class PhysicsPlayerProvider implements PlayerProvider {

	@Override
	public @NotNull Player createPlayer(@NotNull UUID uuid, @NotNull String username,
			@NotNull PlayerConnection connection) {
		return new PhysicsPlayer(uuid, username, connection);
	}

}
