package pl.minecon724.ball;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;

public class BallEntity extends Entity {
	private Long lastTick;
	
	final private double gravity = -9.807;
	public Vec velocity = new Vec(ThreadLocalRandom.current().nextDouble(-10, 10),0,ThreadLocalRandom.current().nextDouble(-10, 10));

	public BallEntity() {
		super(EntityType.FALLING_BLOCK);
		
		final FallingBlockMeta meta = (FallingBlockMeta) this.getEntityMeta();
		
		this.setNoGravity(true);
		meta.setBlock(Block.WHITE_WOOL);
		velocity = new Vec(0,0,0);
	}
	
	public void update(long time) {
		if (lastTick == null) {
			lastTick = time;
			return;
		}
		
		final double deltaTime = time - lastTick;
		lastTick = time;
		
		final Instance instance = this.getInstance();
		final Pos initialPosition = this.getPosition();
		
		// load chunk
		if (!this.getInstance().isChunkLoaded(initialPosition))
			try {
				this.getInstance().loadChunk(initialPosition).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		
		// new pos
		
		this.velocity = this.velocity.add(0, gravity*(deltaTime/1000), 0);
		
		Pos newPosition = initialPosition.add(velocity.mul(deltaTime/1000));
		
		Point pointBottom = newPosition;
		Point pointTop = newPosition.add(0, 1, 0);
		
		Point pointEast = newPosition.add(0.5, 0.5, 0);
		Point pointWest = pointEast.sub(1, 0, 0);
		
		Point pointSouth = newPosition.add(0, 0.5, 0.5);
		Point pointNorth = pointSouth.sub(0, 0, 1);
		
		if (this.getInstance().getBlock(pointBottom).isSolid() || this.getInstance().getBlock(pointTop).isSolid()) {
			newPosition = newPosition.withY(initialPosition.y());
			this.velocity = this.velocity.mul(0.97, -0.8, 0.97);
		}
		if (this.getInstance().getBlock(pointEast).isSolid() || this.getInstance().getBlock(pointWest).isSolid()) {
			newPosition = newPosition.withX(initialPosition.x());
			this.velocity = this.velocity.mul(-0.8, 1, 1);
		}
		if (this.getInstance().getBlock(pointNorth).isSolid() || this.getInstance().getBlock(pointSouth).isSolid()) {
			newPosition = newPosition.withZ(initialPosition.z());
			this.velocity = this.velocity.mul(1, 1, -0.8);
		}
		
		//if (newPosition.y() < 40) {
		//	newPosition = newPosition.withY(40);
		//	this.velocity = this.velocity.mul(0.97, -0.8, 0.97);
		//}
		
		for (Player player : instance.getPlayers()) {
			PhysicsPlayer physicsPlayer = (PhysicsPlayer) player;
			Entity entity = player;
			
			if (this.getBoundingBox().intersectEntity(newPosition, entity)) {
				Vec diff = newPosition.withY(0).asVec().sub(entity.getPosition().withY(0).asVec());
				this.velocity = physicsPlayer.getMovement();
			}
			
		}
		
		// ocnfirm
		this.teleport(newPosition);
		
		double distance = initialPosition.distance(newPosition) * (1000/deltaTime);
		
		if (distance > 23) {
			ParticlePacket particlePacket = ParticleCreator.createParticlePacket(Particle.FLAME, newPosition.x(), newPosition.y(), newPosition.z(), .5f, .5f, .5f, (int)Math.ceil((distance-15)/5));
			this.getInstance().sendGroupedPacket(particlePacket);
		}
		
		//System.out.println(newPosition);
	}
}
