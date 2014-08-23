package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.component.Destination;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selection;
import com.ethanshea.ld30.component.Surface;

public class OrderSystem extends IteratingSystem {
	Engine engine;
	Family planet = Family.getFamilyFor(Position.class, Radius.class);
	Camera cam;
	Entity currentPlanet;
	float currentAngle;
	ShapeRenderer render = new ShapeRenderer();

	public OrderSystem(Camera cam) {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class));
		this.cam = cam;
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	public void update(float delta) {
		Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
		currentPlanet = null;
		for (Entry<Entity> e : engine.getEntitiesFor(planet)) {
			Position center = e.value.getComponent(Position.class);

			final float RANGE = 50;
			float detect = RANGE + e.value.getComponent(Radius.class).size;
			if ((pos.x - center.x) * (pos.x - center.x) + (pos.y - center.y) * (pos.y - center.y) < detect * detect) {
				double angleRad = Math.atan2(pos.y - center.y, pos.x - center.x);
				currentPlanet = e.value;
				currentAngle = (float) (180 / Math.PI * angleRad);
				Gdx.gl.glLineWidth(2);
				render.setProjectionMatrix(cam.combined);
				render.begin(ShapeType.Line);
				render.setColor(.8f, 1, .2f, .5f);
				render.line(center.x, center.y, ((float) Math.cos(angleRad) * detect) + center.x,
						((float) Math.sin(angleRad) * detect) + center.y);
				render.end();
				Gdx.gl.glLineWidth(1);
				break;
			}
		}
		super.update(delta);
	}

	public void processEntity(Entity entity, float deltaTime) {
		Destination d = entity.getComponent(Destination.class);
		if ((currentPlanet != null) && (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))) {
			// Set the destination
			if (entity.getComponent(Selection.class).selected) {
				System.out.println("destination set.");
				d.planet = currentPlanet;
				d.r = currentAngle;
			}
		}
		// Go to the current destination
		Rotation rot = entity.getComponent(Rotation.class);
		if ((Math.abs(rot.r - d.r) > 5) && (d.planet.equals(entity.getComponent(Surface.class).surface))) {
			float diff = d.r - rot.r;
			int dir = (diff <= -180) || ((diff > 0) && (diff <= 180)) ? 1 : -1;
			rot.r += 10f * dir / entity.getComponent(Surface.class).surface.getComponent(Radius.class).size;
			if (rot.r<-180)
				rot.r+=360;
			if (rot.r>180)
				rot.r-=360;
		}
	}
}
