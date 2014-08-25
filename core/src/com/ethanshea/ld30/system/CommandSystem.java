package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.Constants;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.*;

public class CommandSystem extends IteratingSystem {
	int buildSelect;
	Engine engine;
	Family planet = Family.getFamilyFor(Position.class, Radius.class);
	Family factory = Family.getFamilyFor(Rotation.class, Surface.class, Ownership.class, Center.class, FactoryID.class);
	Camera cam;
	Entity currentPlanet;
	float currentAngle;
	ShapeRenderer render = new ShapeRenderer();
	ShapeRenderer hud = new ShapeRenderer();
	SpriteBatch batch = new SpriteBatch();
	int orderNum;

	public CommandSystem(Camera cam) {
		super(Family.getFamilyFor(Surface.class, Rotation.class, Selection.class, Destination.class, Direction.class));
		this.cam = cam;
		this.hud = hud;
	}

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	public void update(float delta) {
		currentPlanet = null;
		orderNum = 0;
		Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
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

		batch.begin();
		batch.draw(Game.storeImg, 800 - 64, 480 - 128);
		batch.end();

		Gdx.gl.glLineWidth(2);
		hud.begin(ShapeType.Line);
		hud.setColor(.8f, 1, .2f, .5f);
		hud.rect(800 - 64 + 4, buildSelect * -64 + 4 + 480 - 64, 60, 60);
		hud.end();
		Gdx.gl.glLineWidth(1);

		float scrX = Gdx.input.getX() * (800f / Gdx.graphics.getWidth());
		float scrY = Gdx.input.getY() * (480f / Gdx.graphics.getHeight());

		if ((scrX > (800 - 64)) && (scrY < 128) && (Gdx.input.isButtonPressed(0))) {
			buildSelect = (int) (scrY / 64);
		} else {
			// Update objects
			super.update(delta);

			if (Gdx.input.justTouched() && Gdx.input.isButtonPressed(0)) {
				if (currentPlanet != null) {
					// Place factories
					if (buildSelect == 1) {
						if (currentPlanet.getComponent(Ownership.class).ownership > .5f
								&& Game.user.money > Constants.FACTORY_PRICE
								&& currentPlanet.getComponent(FactoryCount.class).count < Constants.MAX_FACTORIES) {
							engine.addEntity(Game.mkFactory(currentAngle, currentPlanet, 1));
							Game.user.money -= Constants.FACTORY_PRICE;
							Game.build.play();
							return;
						} else {
							Game.invalid.play();
						}
					}

					// Make tanks
					if (buildSelect == 0) {
						if (Game.user.money > Constants.TANK_PRICE) {
							// Are we over a factory that we own?
							for (Entry<Entity> e : engine.getEntitiesFor(factory)) {
								Center c = e.value.getComponent(Center.class);
								if (Game.distanceSq(c.x, c.y, pos.x, pos.y) < 30 * 30) {
									engine.addEntity(Game.mkTank(currentAngle, currentPlanet,
											Gdx.input.isKeyPressed(Keys.A) ? -1 : 1));
									Game.user.money -= Constants.TANK_PRICE;
									Game.build.play();
									return;
								}
							}
						}
						Game.invalid.play();
					}
				}
				buildSelect = -1;
			}
		}
	}

	public void processEntity(Entity entity, float deltaTime) {
		Destination d = entity.getComponent(Destination.class);
		if ((currentPlanet != null) && (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.justTouched())) {
			// Set the destination
			if (entity.getComponent(Selection.class).selected) {
				d.planet = currentPlanet;
				// Stagger the tanks behind one another
				d.r = currentAngle + entity.getComponent(Direction.class).asFloat() * -5 * orderNum;
				d.arrived = false;

				if (orderNum == 0)
					Game.direct.play();
				orderNum++;
			}
		}
	}
}
