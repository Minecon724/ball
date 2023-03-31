package pl.minecon724.ball;

import java.util.concurrent.ThreadLocalRandom;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import pl.minecon724.ball.commands.*;

public class MainClass {
	public static final Integer TICKRATE = 128;
	
	public static void main(String[] args) {
		
		System.setProperty("minestom.tps", TICKRATE.toString());
		
		// Initialize the server & things
		MinecraftServer mcServer = MinecraftServer.init();
		InstanceManager instanceManager = MinecraftServer.getInstanceManager();
		GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
		SchedulerManager scheduler = MinecraftServer.getSchedulerManager();
		CommandManager commandManager = MinecraftServer.getCommandManager();
		
		// Config
		MinecraftServer.setBrandName("Velocity");
		MinecraftServer.getConnectionManager().setPlayerProvider(new PhysicsPlayerProvider());
		MinecraftServer.setCompressionThreshold(128);
		
		// Register commands
		commandManager.register(new HelpCommand());
		commandManager.register(new BallCommand());
		commandManager.register(new FlyCommand());
		
		// Create a dimenstion
		DimensionType dimension = DimensionType.builder(NamespaceID.from("dimension"))
				.skylightEnabled(true)
				.ambientLight(1f)
				.build();
		MinecraftServer.getDimensionTypeManager().addDimension(dimension);
		
		// Create instance
		InstanceContainer instanceContainer = instanceManager.createInstanceContainer(dimension);
		
		instanceContainer.setGenerator(unit -> {
			unit.modifier().fillHeight(0, 1, Block.GRASS_BLOCK);
		});
		
		instanceContainer.setTimeRate(0);
		instanceContainer.getWorldBorder().setDiameter(500);
		
		instanceContainer.getEntities().forEach(entity ->
			entity.remove());
		
		// Register the instnace
		globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
			event.setSpawningInstance(instanceContainer);
			Player player = event.getPlayer();
			
			player.setAllowFlying(true);
			player.getInventory().setItemStack(4, ItemStack.of(Material.STONE));
			
			Pos spawnPoint = new Pos(
					ThreadLocalRandom.current().nextInt(-75, 75),
					3,
					ThreadLocalRandom.current().nextInt(-75, 75));
			player.setRespawnPoint(spawnPoint);
			new BallEntity().setInstance(instanceContainer, spawnPoint.add(0, 5, 5));
		});
		
		globalEventHandler.addListener(ServerListPingEvent.class, event -> {
			ResponseData responseData = event.getResponseData();
			responseData.clearEntries();
			responseData.setPlayersHidden(true);
			responseData.setDescription(
					LegacyComponentSerializer.legacyAmpersand().deserialize("&#b8fbcfu&#b3f7e6w&#adf3fdu"));
			event.setResponseData(responseData);
		});
		
		globalEventHandler.addListener(EventListener.builder(ItemDropEvent.class)
				.handler(event -> {
					event.setCancelled(true);
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerStartDiggingEvent.class)
				.handler(event -> {
					if (event.getBlock() != Block.STONE) return;
					
					event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerBlockPlaceEvent.class)
				.handler(event -> {
					event.consumeBlock(false);
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerBlockBreakEvent.class)
				.handler(event -> {
					event.setCancelled(true);
				}).build());
		
		scheduler.scheduleTask(new Runnable() {
			@Override
			public void run() {
				instanceContainer.saveChunksToStorage();
				System.out.println("saved");
			}
		}, TaskSchedule.immediate(), TaskSchedule.seconds(30), ExecutionType.ASYNC);
		
		scheduler.scheduleTask(new Runnable() {
			@Override
			public void run() {
				final int used = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000);
				instanceContainer.getPlayers().forEach(player -> {
					Component footer = Component.text(used + " MB").color(NamedTextColor.GOLD)
							.append(Component.text(" | ").color(NamedTextColor.DARK_GRAY))
							.append(Component.text(player.getLatency()).append(Component.text(" ms")).color(NamedTextColor.AQUA));
					player.sendPlayerListHeaderAndFooter(Component.text(), footer);
				});
			}
		}, TaskSchedule.immediate(), TaskSchedule.seconds(10), ExecutionType.ASYNC);
		
		// Start the server
		mcServer.start("0.0.0.0", 25565);
	}
}
