package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.Constants;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.*;

public class TankAI extends IteratingSystem {
	Family surfaceObj = Family.getFamilyFor(Rotation.class, Surface.class);
	private Engine engine;

	public TankAI() {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Destination.class, Speed.class, Direction.class,
				TankID.class, Fighting.class));
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Ownership own = entity.getComponent(Ownership.class);
		Rotation rot = entity.getComponent(Rotation.class);
		Direction direction = entity.getComponent(Direction.class);
		Destination d = entity.getComponent(Destination.class);
		if (d.planet.equals(entity.getComponent(Surface.class).surface)) {
			moveTowards(entity, d.r);
		} else {
			// Interplanet travel
			Position from = entity.getComponent(Surface.class).surface.getComponent(Position.class);
			Position to = d.planet.getComponent(Position.class);
			if (moveTowards(entity, (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x)))) {
				// Blast off!
				Game.blastoff.play(own.isUser() ? 1 : .5f);
				if (entity.hasComponent(Selection.class))
					entity.getComponent(Selection.class).selected = false;
				engine.removeEntity(entity);
				engine.addEntity(Game.mkRocket(entity, d));
			}
		}

		BulletCooldown cool = entity.getComponent(BulletCooldown.class);
		cool.cooldown--;

		// Collision decection/Shoot at enimies
		Entity planet = entity.getComponent(Surface.class).surface;
		Fighting fight = entity.getComponent(Fighting.class);
		boolean fighting = false;
		for (Entry<Entity> e : engine.getEntitiesFor(surfaceObj)) {
			// A bug lies here. I have no idea what causes it.
			if (e == null)
				continue;
			if (e.value == null)
				continue;
			if (e.value.getComponent(Surface.class) == null)
				continue;
			if (e.value.getComponent(Surface.class).surface == null)
				continue;
			// </bugfix>
			if (e.value.getComponent(Surface.class).surface.equals(planet)) {
				if (Math.abs(e.value.getComponent(Rotation.class).r - rot.r) < 1) {
					if (e.value.hasComponent(DoorID.class)) {
						Destination dest = e.value.getComponent(Destination.class);
						entity.add(dest);
						rot.r = dest.r;
						entity.getComponent(Surface.class).surface = dest.planet;
					}
				}

				// Can we shoot at someone?
				if (e.value.hasComponent(Ownership.class) && own.isEnemyOf(e.value.getComponent(Ownership.class))
						&& (e.value.getComponent(Rotation.class).r - rot.r + 360 % 180 < Constants.SHOOTING_DISTANCE)) {
					fighting = true;
					fight.target = e.value;
					if (cool.cooldown < 0) {
						engine.addEntity(Game.mkBullet(rot.r, planet, own.ownership, direction.right));
						Game.shoot.play();
						cool.cooldown = Constants.BULLET_COOLDOWN;
					}
				} else {
				}
			}
		}
		fight.fighting = fighting;

		// Planet capturing
		Ownership planetOwner = entity.getComponent(Surface.class).surface.getComponent(Ownership.class);
		planetOwner.ownership += own.ownership * Constants.CAPTURE_RATE;
		if (Math.abs(planetOwner.ownership) > 1)
			planetOwner.ownership = own.ownership;
	}

	private boolean moveTowards(Entity entity, float dest) {
		Rotation rot = entity.getComponent(Rotation.class);
		Speed spd = entity.getComponent(Speed.class);
		Direction direction = entity.getComponent(Direction.class);
		Destination destination = entity.getComponent(Destination.class);
		if (Math.abs(rot.r - dest) > 2) {
			float diff = dest - rot.r;
			int dir = (diff <= -180) || ((diff > 0) && (diff <= 180)) ? 1 : -1;
			direction.right = dir < 0;
			float inc = 1 / entity.getComponent(Surface.class).surface.getComponent(Radius.class).size;
			spd.speed += inc * dir;
			if (Math.abs(spd.speed) > inc * 60) {
				spd.speed = inc * 60 * dir;
			}
			rot.r += spd.speed;
			if (rot.r < -180)
				rot.r += 360;
			if (rot.r > 180)
				rot.r -= 360;
			destination.arrived = false;
			return false;
		} else {
			spd.speed = 0;
			destination.arrived = true;
			return true;
		}
	}
}
