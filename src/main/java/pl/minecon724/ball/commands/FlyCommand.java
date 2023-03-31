package pl.minecon724.ball.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class FlyCommand extends Command {

	public FlyCommand() {
		super("fly");
		
		this.setDefaultExecutor((sender, context) -> {
			Player player = (Player) sender;
			
			boolean allowFlying = !player.isAllowFlying();
			player.setAllowFlying(allowFlying);
			if (!allowFlying) player.setFlying(false);
			
			player.sendActionBar(Component.text( (allowFlying ? "Wl" : "Wyl") + " latanie"));
		});
	}

}
