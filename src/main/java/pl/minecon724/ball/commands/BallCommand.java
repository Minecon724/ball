package pl.minecon724.ball.commands;

import java.util.concurrent.ThreadLocalRandom;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import pl.minecon724.ball.BallEntity;

public class BallCommand extends Command {

	public BallCommand() {
		super("ball");
		
		this.setDefaultExecutor((sender, context) -> {
			Player player = (Player) sender;
			ThreadLocalRandom random = ThreadLocalRandom.current();

			BallEntity ball = new BallEntity();
			ball.setInstance(player.getInstance(), player.getPosition().add(random.nextDouble(-4, 4), 5, random.nextDouble(-4, 4)));
			
			player.lookAt(ball.getPosition().withY(player.getPosition().y()+1));
		});
	}

}
