package pl.minecon724.ball.commands;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

public class AboutCommand extends Command {
	final String artifactId;
	final String version;
	final String timestamp;
	final String builder;
	
	Component component;

	public AboutCommand() {
		super("about");
		
		Map<String, String> build = new Yaml().load(
				this.getClass().getClassLoader().getResourceAsStream("build.yml"));
		
		artifactId = build.get("artifactId");
		version = build.get("version");
		builder = build.get("builder");
		timestamp = build.get("timestamp");
		
		component = Component.text(artifactId);
		component = component.append(Component.text(" " + version).color(NamedTextColor.GRAY));
		component = component.appendNewline();
		component = component.append(Component.text("Budowniczy: " + builder).color(NamedTextColor.DARK_GRAY));
		component = component.appendNewline();
		component = component.append(Component.text("Data kompilacji: " + timestamp).color(NamedTextColor.DARK_GRAY));
		
		this.setDefaultExecutor((sender, context) -> {
			sender.sendMessage(component);
		});
	}
	
}
