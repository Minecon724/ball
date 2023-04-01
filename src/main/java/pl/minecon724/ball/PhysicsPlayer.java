package pl.minecon724.ball;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.inventory.PlayerInventoryItemChangeEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.PlayerConnection;

public class PhysicsPlayer extends Player {
	Vec movement = new Vec(0, 0, 0);
	long lastMove = System.currentTimeMillis();
	long lastTick = System.currentTimeMillis();
	
	double strength = 0;
	double strengthIncrement = 0.1;
	double strengthDecrementPerSecond = -0.1;

	public PhysicsPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
		
		this.eventNode().addListener(EventListener.builder(PlayerSpawnEvent.class)
				.handler(event -> {
					this.setHeldItemSlot((byte)4);
				}).build());
		
		this.eventNode().addListener(EventListener.builder(PlayerMoveEvent.class)
				.handler(event -> {
					try {
						movement = event.getNewPosition().sub(event.getEntity().getPosition()).asVec().mul(1000/(System.currentTimeMillis() - lastMove));
						lastMove = System.currentTimeMillis();
					} catch (ArithmeticException e) { }
					
					
					modStrength(-movement.length() * 0.002);
				}).build());
	
		this.eventNode().addListener(EventListener.builder(PlayerTickEvent.class)
				.handler(event -> {
					modStrength((System.currentTimeMillis() - lastTick)/1000D * strengthDecrementPerSecond);
					lastTick = System.currentTimeMillis();
				}).build());
		
		this.eventNode().addListener(EventListener.builder(PlayerHandAnimationEvent.class)
				.handler(event -> {
					this.getInstance().getEntities().forEach(entity -> {
						if (entity instanceof BallEntity) {
							BallEntity ball = (BallEntity) entity;
							double distance = event.getPlayer().getPosition().distance(entity.getPosition());
						
							if (distance < 2) {
								ball.velocity = ball.velocity.add(ball.getPosition().sub(this.getPosition()).mul(this.strength * (-0.5*distance + 1) *30).asVec());
								setStrength(0);
							}
						}
					});
				}).build());
		
		this.eventNode().addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class)
				.handler(event -> {
					PlayerInventory inventory = event.getPlayer().getInventory();
					
					int from = event.getPlayer().getHeldSlot();
					int to = event.getSlot();
					
					this.setHeldItemSlot((byte)4);
					
					if (from != 4 || to == 4) return;
					
					int mod = from-to;
					
					modStrength(strengthIncrement * mod);
					
					boolean retry = true;
					
					while (retry) {
						for (int l=0;l<Math.abs(mod);l++) {
							if (mod < 0) {
								
								ItemStack temp = inventory.getItemStack(0);
								for (int i=0;i<8;i++) {
									inventory.setItemStack(i, inventory.getItemStack(i+1));
								}
								inventory.setItemStack(8, temp);
								
							} else {
								
								ItemStack temp = inventory.getItemStack(8);
								for (int i=8;i>0;i--) {
									inventory.setItemStack(i, inventory.getItemStack(i-1));
								}
								inventory.setItemStack(0, temp);
							}
						}
						
						mod = mod > 0 ? 1 : -1;
						retry = false;
						
						for (int i=0;i<9;i++) {
							retry = inventory.getItemStack(i) != ItemStack.AIR;
							if (retry) break;
						}
						
						retry = retry && inventory.getItemStack(4) == ItemStack.AIR;
					}

				}).build());
	}
	
	public double modStrength(double add) {
		return setStrength(this.strength + add);
	}

	public double setStrength(double strength) {
		
		this.strength = Math.min(Math.max(strength, 0D), 1D);
		this.setExp((float)this.strength);
		
		return this.strength;
	}
	
	public Vec getMovement() {
		return this.movement;
	}
}
