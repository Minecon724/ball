package pl.minecon724.ball.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;

public class HelpCommand extends Command {
	public static final String helpString = 
			"""
			--- POMOC ---
			Komendy: Zobacz tab
			
			Sila:
			- reguluj kolkiem
			- tracisz 10% na sekunde
			  i jak sie ruszasz
			- aby uzyc, stan blisko pilki i uderz lapka
			
			i to tyle wsm
			--- POMOC ---""";
	
	public static final Component helpComponent = Component.text(helpString);

	public HelpCommand() {
		super("help");
		
		this.setDefaultExecutor((sender, context) ->
					sender.sendMessage(helpComponent));
	}

}
