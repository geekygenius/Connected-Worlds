package com.ethanshea.ld30;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.component.BulletID;
import com.ethanshea.ld30.component.Direction;
import com.ethanshea.ld30.component.Health;
import com.ethanshea.ld30.component.Height;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Surface;

public class BulletMovment extends IteratingSystem {
	final Family target = Family.getFamilyFor(Health.class, Surface.class, Rotation.class);
	Engine engine;

	public BulletMovment() {
		super(Family.getFamilyFor(BulletID.class, Rotation.class, Height.class, Surface.class));
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		System.out.println("Processing bullet");
		Rotation rot = entity.getComponent(Rotation.class);
		rot.r += 100 * entity.getComponent(Direction.class).asFloat()
				/ entity.getComponent(Surface.class).surface.getComponent(Radius.class).size;
		if (rot.r < -180)
			rot.r += 360;
		if (rot.r > 180)
			rot.r -= 360;

		Height height = entity.getComponent(Height.class);
		height.height -= Constants.BULLET_GRAVITY;
		if (height.height < 0) {
			engine.removeEntity(entity);
			return;
		}

		Entity planet = entity.getComponent(Surface.class).surface;
		for (Entry<Entity> e : engine.getEntitiesFor(target)) {
			if (e.value.getComponent(Surface.class).surface.equals(planet)) {
				if (Math.abs(e.value.getComponent(Rotation.class).r - rot.r) < 1) {
					Health h = e.value.getComponent(Health.class);
					h.health -= Constants.BULLET_DAMAGE;
					if (h.health <= 0) {
						engine.removeEntity(e.value);
					}
					engine.removeEntity(entity);
				}
			}
		}
	}
}
