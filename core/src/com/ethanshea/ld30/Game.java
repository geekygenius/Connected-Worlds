package com.ethanshea.ld30;

import java.util.ArrayList;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ethanshea.ld30.component.Center;
import com.ethanshea.ld30.component.Destination;
import com.ethanshea.ld30.component.Ownership;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selection;
import com.ethanshea.ld30.component.SpriteComponent;
import com.ethanshea.ld30.component.Surface;
import com.ethanshea.ld30.system.ObjectRenderer;
import com.ethanshea.ld30.system.OrderSystem;
import com.ethanshea.ld30.system.PlanetRenderer;
import com.ethanshea.ld30.system.SecectionManager;

public class Game extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Engine engine;
	Family planet;
	OrthographicCamera camera;
	Texture tankImg;
	Texture doorImg;
	Texture factoryImg;

	@Override
	public void create() {
		batch = new SpriteBatch();
		engine = new Engine();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		camera.position.x = 0;
		camera.position.y = 15000;
		camera.zoom = 2;

		Gdx.input.setInputProcessor(this);

		tankImg = new Texture(Gdx.files.internal("tank.png"));
		doorImg = new Texture(Gdx.files.internal("door.png"));
		factoryImg = new Texture(Gdx.files.internal("factory.png"));

		engine.addSystem(new OrderSystem(camera));
		engine.addSystem(new PlanetRenderer(camera));
		engine.addSystem(new ObjectRenderer(camera, batch));
		engine.addSystem(new SecectionManager(camera));

		genLevel();
		// engine.addEntity(mkTank(0, p));
	}

	private class PlanetSystem {
		public PlanetSystem(Entity planet, Entity door) {
			this.planet = planet;
			this.door = door;
		}

		public Entity planet;
		public Entity door;
	}

	private void genLevel() {
		ArrayList<PlanetSystem> planets = new ArrayList<PlanetSystem>();
		Entity home = mkPlanet(0, 15000, 250);
		home.getComponent(Ownership.class).ownership = 1;
		planets.add(new PlanetSystem(home, mkDoor(135, home)));
		Entity enemy = mkPlanet(15000, 0, 250);
		enemy.getComponent(Ownership.class).ownership = -1;
		planets.add(new PlanetSystem(enemy, mkDoor(-45, enemy)));
		genLoop: while (planets.size() < 15) {
			float x = (float) (Math.random() * 15000);
			float y = (float) (Math.random() * 15000);
			for (PlanetSystem e : planets) {
				Position pos = e.planet.getComponent(Position.class);
				float detect = 100 + e.planet.getComponent(Radius.class).size;
				if ((pos.x - x) * (pos.x - x) + (pos.y - y) * (pos.y - y) < detect * detect) {
					continue genLoop;
				}
			}
			// It's not close to another planet, go ahead and make it.
			Entity p = mkPlanet(x, y, 100 + ((float) Math.random() * 300));
			Entity door = mkDoor(((float) Math.random() * 360) - 180, p);
			if (planets.size() > 1) {
				Destination d = door.getComponent(Destination.class);
				d.planet = planets.get(planets.size() - 1).planet;
				d.r = planets.get(planets.size() - 1).door.getComponent(Rotation.class).r + 180;
				if (d.r > 180) {
					d.r -= 360;
				}
			}
			planets.add(new PlanetSystem(p, door));
		}
		Destination d = planets.get(planets.size() - 1).door.getComponent(Destination.class);
		d.planet = planets.get(0).planet;
		d.r = planets.get(0).door.getComponent(Rotation.class).r + 180;
		if (d.r > 180) {
			d.r -= 360;
		}

		for (PlanetSystem sys : planets) {
			engine.addEntity(sys.planet);
			engine.addEntity(sys.door);
		}
	}

	float accum;

	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

		// Input
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		float MOVMENT_SPEED = 15;
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			camera.position.y += MOVMENT_SPEED * camera.zoom;
		} else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			camera.position.y -= MOVMENT_SPEED * camera.zoom;
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			camera.position.x += MOVMENT_SPEED * camera.zoom;
		} else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			camera.position.x -= MOVMENT_SPEED * camera.zoom;
		}

		// Update
		accum += Gdx.graphics.getDeltaTime();
		// engine.update(Gdx.graphics.getDeltaTime());
		engine.update(accum);
	}

	public Entity mkPlanet(float x, float y, float size) {
		Entity e = new Entity();
		e.add(new Position(x, y));
		e.add(new Radius(size));
		e.add(new Ownership(0));
		return e;
	}

	public Entity mkDoor(float pos, Entity planet) {
		Entity e = new Entity();
		Sprite s = new Sprite(doorImg);
		s.setOrigin(16, 0);
		e.add(new SpriteComponent(s));
		e.add(new Rotation(pos));
		e.add(new Surface(planet));
		e.add(new Destination(0, null));
		return e;
	}

	public Entity mkTank(float pos, Entity planet) {
		Entity e = new Entity();
		Sprite s = new Sprite(tankImg);
		s.setOrigin(16, 0);
		e.add(new SpriteComponent(s));
		e.add(new Rotation(pos));
		e.add(new Surface(planet));
		e.add(new Selection());
		e.add(new Center(0, 0));
		e.add(new Destination(0, null));
		return e;
	}

	public void dispose() {
		batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		camera.zoom *= Math.pow(2, amount);
		if (camera.zoom < 1 / 16f)
			camera.zoom = 1 / 16f;
		if (camera.zoom > 16f)
			camera.zoom = 16f;
		return true;
	}
}
