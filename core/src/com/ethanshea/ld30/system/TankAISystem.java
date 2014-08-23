package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.ethanshea.ld30.component.Destination;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selection;
import com.ethanshea.ld30.component.Surface;

public class TankAISystem extends IteratingSystem {

	public TankAISystem() {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class));
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Rotation rot = entity.getComponent(Rotation.class);
		Destination d = entity.getComponent(Destination.class);
		if ((Math.abs(rot.r - d.r) > 2) && (d.planet.equals(entity.getComponent(Surface.class).surface))) {
			float diff = d.r - rot.r;
			int dir = (diff <= -180) || ((diff > 0) && (diff <= 180)) ? 1 : -1;
			rot.r += 30f * dir / entity.getComponent(Surface.class).surface.getComponent(Radius.class).size;
			if (rot.r < -180)
				rot.r += 360;
			if (rot.r > 180)
				rot.r -= 360;
		}
	}
}
