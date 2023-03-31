package pl.minecon724.ball;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;

public class Logger {
	
	public Logger(ComponentLogger logger, GlobalEventHandler globalEventHandler) {
		globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
			Player player = event.getPlayer();
			
			
			String name = PlainTextComponentSerializer.plainText().serialize(player.getName());
			
			logger.info(name + " logged in");
		});
		
		globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
			Player player = event.getPlayer();
			
			String name = PlainTextComponentSerializer.plainText().serialize(player.getName());
			
			logger.info(name + " disconnected");
		});
		
		globalEventHandler.addListener(PlayerChatEvent.class, event -> {
			String message = String.format("<%s> %s",
					PlainTextComponentSerializer.plainText().serialize(event.getPlayer().getName()),
					event.getMessage());
			
			logger.info(message);
		});
	}
	
}
