package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.ethanshea.ld30.component.Destination;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selection;
import com.ethanshea.ld30.component.Speed;
import com.ethanshea.ld30.component.Surface;

public class TankAISystem extends IteratingSystem {

	public TankAISystem() {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class, Speed.class));
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Rotation rot = entity.getComponent(Rotation.class);
		Destination d = entity.getComponent(Destination.class);
		Speed spd = entity.getComponent(Speed.class);
		if ((Math.abs(rot.r - d.r) > 2) && (d.planet.equals(entity.getComponent(Surface.class).surface))) {
			float diff = d.r - rot.r;
			int dir = (diff <= -180) || ((diff > 0) && (diff <= 180)) ? 1 : -1;
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

		} else {
			spd.speed = spd.speed>0 ? Float.MIN_NORMAL: -Float.MIN_NORMAL; //Do this to proserve the sign
		}
	}
}
