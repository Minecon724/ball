package pl.minecon724.ball;

import net.minestom.server.color.DyeColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player.Hand;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;

public class Painter {
	public Painter(GlobalEventHandler globalEventHandler) {
		globalEventHandler.addListener(PlayerBlockInteractEvent.class, event -> {
			
			Material item = event.getPlayer().getItemInHand(event.getHand()).material();
			Block block = event.getBlock();
			
			if (item == Material.WHITE_DYE || item == Material.BLACK_DYE || item == Material.BUCKET) {
				Block dyed = getDyed(block, item);
				event.getInstance().setBlock(event.getBlockPosition(), dyed);
				
				if (dyed != block) event.getPlayer().sendPacketToViewersAndSelf(
						new EntityAnimationPacket(event.getPlayer().getEntityId(),
								event.getHand() == Hand.MAIN ?
										EntityAnimationPacket.Animation.SWING_MAIN_ARM :
										EntityAnimationPacket.Animation.SWING_OFF_HAND));
			}
			
		});
		
		globalEventHandler.addListener(PlayerEntityInteractEvent.class, event -> {
			
			Entity entity = event.getTarget();
			if (!(entity instanceof BallEntity)) return;
			BallEntity ball = (BallEntity) entity;
			
			Material item = event.getPlayer().getItemInHand(event.getHand()).material();
			Block block = ((FallingBlockMeta) ball.getEntityMeta()).getBlock();
			
			if (item == Material.WHITE_DYE || item == Material.BLACK_DYE || item == Material.BUCKET) {
				Block dyed = getDyed(block, item);
				
				Pos pos = ball.getPosition();
				Vec velocity = ball.velocity;
				
				ball.remove();
				BallEntity newBall = new BallEntity(dyed);
				newBall.velocity = velocity;
				newBall.setInstance(event.getInstance(), pos);
				
				if (dyed != block) event.getPlayer().sendPacketToViewersAndSelf(
						new EntityAnimationPacket(event.getPlayer().getEntityId(),
								event.getHand() == Hand.MAIN ?
										EntityAnimationPacket.Animation.SWING_MAIN_ARM :
										EntityAnimationPacket.Animation.SWING_OFF_HAND));
			}
			
		});
	}
	
	public Block getDyed(Block block, Material dye) {
		DyeColor color = (dye == Material.WHITE_DYE ? DyeColor.WHITE : (dye == Material.BLACK_DYE ? DyeColor.BLACK : null));
		
		if (block == Block.WHITE_STAINED_GLASS || block == Block.BLACK_STAINED_GLASS) {
			switch (color) {
			case WHITE:
				block = Block.WHITE_STAINED_GLASS;
				break;
			case BLACK:
				block = Block.BLACK_STAINED_GLASS;
				break;
			default:
				break;
			}
		} else if (block == Block.WHITE_CONCRETE_POWDER || block == Block.BLACK_CONCRETE_POWDER) {
			switch (color) {
			case WHITE:
				block = Block.WHITE_CONCRETE_POWDER;
				break;
			case BLACK:
				block = Block.BLACK_CONCRETE_POWDER;
				break;
			default:
				break;
			}
		} else if (block == Block.WHITE_WOOL || block == Block.BLACK_WOOL) {
			switch (color) {
			case WHITE:
				block = Block.WHITE_WOOL;
				break;
			case BLACK:
				block = Block.BLACK_WOOL;
				break;
			default:
				break;
			}
		}
		
		return block;
	}
}
