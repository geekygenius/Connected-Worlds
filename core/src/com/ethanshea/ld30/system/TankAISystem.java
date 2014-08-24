package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.Constants;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.*;

public class TankAISystem extends IteratingSystem {
	Family surfaceObj = Family.getFamilyFor(Rotation.class, Surface.class, Ownership.class);
	private Engine engine;

	public TankAISystem() {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class, Speed.class,
				Direction.class, TankID.class));
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Rotation rot = entity.getComponent(Rotation.class);
		Direction direction = entity.getComponent(Direction.class);
		Destination d = entity.getComponent(Destination.class);
		if (d.planet.equals(entity.getComponent(Surface.class).surface)) {
			moveTowards(entity,d.r);
		} else {
			// Interplanet travelsurface
			Position from = entity.getComponent(Surface.class).surface.getComponent(Position.class);
			Position to = d.planet.getComponent(Position.class);
			if (moveTowards(entity,(float) Math.toDegrees(Math.atan2(to.y-from.y, to.x-from.x)))){
				//Blast off!
				entity.getComponent(Selection.class).selected = false;
				engine.removeEntity(entity);
				engine.addEntity(Game.mkRocket(entity,d));
			}
		}

		BulletCooldown cool = entity.getComponent(BulletCooldown.class);
		cool.cooldown--;

		// Collision decection/Shoot at enimies
		Entity planet = entity.getComponent(Surface.class).surface;
		Ownership own = entity.getComponent(Ownership.class);
		for (Entry<Entity> e : engine.getEntitiesFor(surfaceObj)) {
			if (e.value.getComponent(Surface.class).surface.equals(planet)) {
				if (Math.abs(e.value.getComponent(Rotation.class).r - rot.r) < 1) {
					if (e.value.hasComponent(DoorID.class)) {
						Destination dest = e.value.getComponent(Destination.class);
						rot.r = dest.r;
						entity.getComponent(Surface.class).surface = dest.planet;
					}
				}

				// Can we shoot at someone?
				if ((cool.cooldown < 0) && (own.isEnemyOf(e.value.getComponent(Ownership.class)))
						&& (e.value.getComponent(Rotation.class).r - rot.r + 360 % 180 < Constants.SHOOTING_DISTANCE)) {
					engine.addEntity(Game.mkBullet(rot.r, planet, own.ownership, direction.right));
					cool.cooldown = Constants.BULLET_COOLDOWN;
				}
			}
		}

		// Planet capturing
		float owner = entity.getComponent(Ownership.class).ownership;
		own.ownership += owner * Constants.CAPTURE_RATE;
		if (Math.abs(own.ownership) > 1)
			own.ownership = owner;
	}

	private boolean moveTowards(Entity entity, float dest) {
		Rotation rot = entity.getComponent(Rotation.class);
		Speed spd = entity.getComponent(Speed.class);
		Direction direction = entity.getComponent(Direction.class);
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
			return false;
		} else {
			spd.speed = 0;
			return true;
		}
	}
}
