package pl.minecon724.ball;

import java.io.StringReader;
import java.util.concurrent.ThreadLocalRandom;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientUpdateSignPacket;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Rotation;
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
		ComponentLogger logger = MinecraftServer.LOGGER;
		
		// Config
		MinecraftServer.setBrandName("Velocity");
		MinecraftServer.getConnectionManager().setPlayerProvider(new PhysicsPlayerProvider());
		MinecraftServer.setCompressionThreshold(0);
		//VelocityProxy.enable("rrXQ5MTvSles");
		
		// Register commands
		commandManager.register(new HelpCommand());
		commandManager.register(new BallCommand());
		commandManager.register(new FlyCommand());
		commandManager.register(new AboutCommand());
		
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
			player.getInventory().setItemStack(0, ItemStack.of(Material.WHITE_CONCRETE_POWDER));
			player.getInventory().setItemStack(1, ItemStack.of(Material.WHITE_STAINED_GLASS));
			player.getInventory().setItemStack(2, ItemStack.of(Material.STONE_SLAB));
			player.getInventory().setItemStack(3, ItemStack.of(Material.OAK_SIGN));
			player.getInventory().setItemStack(4, ItemStack.of(Material.STONE));
			
			player.getInventory().setItemStack(7, ItemStack.of(Material.WHITE_DYE));
			player.getInventory().setItemStack(8, ItemStack.of(Material.BLACK_DYE));
			
			Pos spawnPoint = new Pos(
					ThreadLocalRandom.current().nextInt(-75, 75),
					3,
					ThreadLocalRandom.current().nextInt(-75, 75));
			player.setRespawnPoint(spawnPoint);
			
			instanceContainer.loadChunk(spawnPoint.add(0, 5, 5)).thenAccept(chunk -> {
				new BallEntity().setInstance(instanceContainer, spawnPoint.add(0, 5, 5));
			});
		});
		
		globalEventHandler.addListener(EventListener.builder(ItemDropEvent.class)
				.handler(event -> {
					event.setCancelled(true);
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerStartDiggingEvent.class)
				.handler(event -> {
					if (event.getBlock() == Block.GRASS_BLOCK) return;
					
					event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerBlockPlaceEvent.class)
				.handler(event -> {
					event.consumeBlock(false);
					
					if (event.getBlock() == Block.OAK_SIGN) {
						
						if (event.getBlockFace() != BlockFace.TOP && event.getBlockFace() != BlockFace.BOTTOM) {
							String facing = event.getBlockFace().toString().toLowerCase();
							event.setBlock(Block.OAK_WALL_SIGN.withProperty("facing", facing));
						} else {
							float yaw = (int)event.getPlayer().getPosition().yaw();
							yaw = 180 + yaw;
							
							Integer rotation = (int) (yaw * 16.0F / 360.0F + 0.5D) & 15;
							System.out.println(rotation);
							
							event.setBlock(Block.OAK_SIGN.withProperty("rotation", rotation.toString()));
						}
							
						event.getPlayer().sendPacket(
								new OpenSignEditorPacket(event.getBlockPosition()));
					}
					
				}).build());
		
		globalEventHandler.addListener(EventListener.builder(PlayerBlockBreakEvent.class)
				.handler(event -> {
					event.setCancelled(true);
				}).build());
		
		globalEventHandler.addListener(PlayerPacketEvent.class, event -> {
			if (event.getPacket() instanceof ClientUpdateSignPacket) {
				ClientUpdateSignPacket packet = (ClientUpdateSignPacket) event.getPacket();
				
				Instance instance = event.getPlayer().getInstance();
				Block sign = instance.getBlock(packet.blockPosition());
				
				if (sign.compare(Block.OAK_SIGN) || sign.compare(Block.OAK_WALL_SIGN)) {
					System.out.println(packet.lines());
					
					MiniMessage mm = MiniMessage.builder().tags(TagResolver.builder()
							.resolver(StandardTags.rainbow())
							.build()).build();
					
					String[] lines = new String[4];
					for (int i=0;i<4;i++) {
						// about that...
						lines[i] = GsonComponentSerializer.gson().serialize(
								mm.deserialize("<rainbow>" + packet.lines().get(i).replace("<", "\\<").replace(">", "\\>") + "</rainbow>"));
					}
					
					NBT nbt = NBT.Compound(root -> {
						root.put("Text1", NBT.String(lines[0]));
						root.put("Text2", NBT.String(lines[1]));
						root.put("Text3", NBT.String(lines[2]));
						root.put("Text4", NBT.String(lines[3]));
					});
					sign = sign.withNbt((NBTCompound)nbt);
					System.out.println(sign.nbt());
					instance.setBlock(packet.blockPosition(), sign);
				}
			}
		});
		
		scheduler.scheduleTask(new Runnable() {
			@Override
			public void run() {
				instanceContainer.saveChunksToStorage();
				logger.info("Instance saved");
			}
		}, TaskSchedule.immediate(), TaskSchedule.minutes(5), ExecutionType.ASYNC);
		
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
		
		new Painter(globalEventHandler);
		new Logger(logger, globalEventHandler);
		
		// Start the server
		mcServer.start("127.0.0.1", 37166);
	}
}
