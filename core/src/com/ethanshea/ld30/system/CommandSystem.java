package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.*;

public class CommandSystem extends IteratingSystem {
	Engine engine;
	Family planet = Family.getFamilyFor(Position.class, Radius.class);
	Family factory = Family.getFamilyFor(Rotation.class, Surface.class, Ownership.class,Center.class,FactoryID.class);
	Camera cam;
	Entity currentPlanet;
	float currentAngle;
	ShapeRenderer render = new ShapeRenderer();
	int orderNum;

	public CommandSystem(Camera cam) {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class,Direction.class));
		this.cam = cam;
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	public void update(float delta) {
		Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
		currentPlanet = null;
		orderNum = 0;
		for (Entry<Entity> e : engine.getEntitiesFor(planet)) {
			Position center = e.value.getComponent(Position.class);

			final float RANGE = 50;
			float detect = RANGE + e.value.getComponent(Radius.class).size;
			if (Game.distanceSq(pos.x, pos.y, center.x, center.y) < detect * detect) {
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
		if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(0) && currentPlanet != null) {
			// Place factories
			if (Game.user.money > 5000
					&& currentPlanet.getComponent(FactoryCount.class).count < 5
					&& (currentPlanet.getComponent(Ownership.class).ownership > .5f)
					&& (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input
							.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
				engine.addEntity(Game.mkFactory(currentAngle, currentPlanet,1));
				Game.user.factories++;
				Game.user.money-=5000;
			}

			// Make tanks
			if (Game.user.money > 2000
					&& (Gdx.input.isButtonPressed(Keys.SHIFT_LEFT) || Gdx.input.isButtonPressed(Keys.SHIFT_RIGHT))) {
				// Are we over a factory that we own?
				for (Entry<Entity> e : engine.getEntitiesFor(factory)) {
					Center c = e.value.getComponent(Center.class);
					if (Game.distanceSq(c.x, c.y, pos.x, pos.y) < 20 * 20) {
						engine.addEntity(Game.mkTank(currentAngle, currentPlanet,  Gdx.input.isKeyPressed(Keys.A)?-1:1));
						Game.user.money-=2000;
						break;
					}
				}
			}
		}

		// Update objects
		super.update(delta);
	}

	public void processEntity(Entity entity, float deltaTime) {
		Destination d = entity.getComponent(Destination.class);
		if ((currentPlanet != null) && (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))) {
			// Set the destination
			if (entity.getComponent(Selection.class).selected) {
				d.planet = currentPlanet;
				//Stagger the tanks behind one another
				d.r = currentAngle + entity.getComponent(Direction.class).asFloat()*-5*orderNum;
				
				orderNum++;
			}
		}
	}
}
