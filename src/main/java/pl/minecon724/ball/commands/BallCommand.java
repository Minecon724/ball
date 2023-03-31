package pl.minecon724.ball.commands;

import java.util.concurrent.ThreadLocalRandom;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import pl.minecon724.ball.BallEntity;

public class BallCommand extends Command {

	public BallCommand() {
		super("ball");
		
		this.setDefaultExecutor((sender, context) -> {
			Player player = (Player) sender;
			
			Point point = player.getTargetBlockPosition(6);
			
			if (point == null) {
				ThreadLocalRandom random = ThreadLocalRandom.current();
				point = player.getPosition().add(random.nextDouble(-4, 4), 5, random.nextDouble(-4, 4));
			}

			BallEntity ball = new BallEntity();
			ball.setInstance(player.getInstance(), point);
			
			player.lookAt(ball.getPosition().withY(player.getPosition().y()+1));
		});
	}

}
