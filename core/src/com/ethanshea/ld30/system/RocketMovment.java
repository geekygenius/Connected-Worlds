package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.ethanshea.ld30.Constants;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.Arrival;
import com.ethanshea.ld30.component.Destination;
import com.ethanshea.ld30.component.Payload;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Speed;
import com.ethanshea.ld30.component.Surface;

public class RocketMovment extends IteratingSystem {
	private Engine engine;

	public RocketMovment() {
		super(Family.getFamilyFor(Arrival.class, Position.class, Speed.class, Rotation.class,Destination.class));
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		// Increase speed until we reach the destination
		double angleRad = Math.toRadians(entity.getComponent(Rotation.class).r);
		Speed speed = entity.getComponent(Speed.class);
		speed.speed += Constants.ROCKET_ACCELLERATION;
		double velX = Math.cos(angleRad) * speed.speed;
		double velY = Math.sin(angleRad) * speed.speed;

		Position pos = entity.getComponent(Position.class);
		pos.x += velX;
		pos.y += velY;

		Position dest = entity.getComponent(Arrival.class);
		if (Game.distanceSq(pos.x, pos.y, dest.x, dest.y) < speed.speed * speed.speed * 40) {
			// We've arrived!
			Entity payload = entity.getComponent(Payload.class).load;
			payload.getComponent(Surface.class).surface = entity.getComponent(Destination.class).planet;
			payload.getComponent(Rotation.class).r = entity.getComponent(Destination.class).r;
			payload.add(entity.getComponent(Destination.class));
			engine.addEntity(payload);
			engine.removeEntity(entity);
		}
	}
}
