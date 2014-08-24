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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ethanshea.ld30.component.*;
import com.ethanshea.ld30.system.CommandSystem;
import com.ethanshea.ld30.system.ObjectRenderer;
import com.ethanshea.ld30.system.PlanetRenderer;
import com.ethanshea.ld30.system.SecectionManager;
import com.ethanshea.ld30.system.TankAISystem;

public class Game extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	SpriteBatch hud;
	Engine engine;
	Family planet;
	OrthographicCamera camera;
	static BitmapFont font;
	static Texture tankImg;
	static Texture doorImg;
	static Texture factoryImg;
	static Texture bulletImg;
	public static Player user = new Player();
	public static Player computer = new Player();

	@Override
	public void create() {
		batch = new SpriteBatch();
		hud = new SpriteBatch();
		engine = new Engine();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		camera.position.x = 0;
		camera.position.y = 15000;
		camera.zoom = 2;
		Gdx.input.setInputProcessor(this);

		font = new BitmapFont(Gdx.files.internal("font.fnt"));
		font.setScale(1.1f, 1.5f);
		font.setColor(1f, 1, .5f, 1);
		tankImg = new Texture(Gdx.files.internal("tank.png"));
		doorImg = new Texture(Gdx.files.internal("door.png"));
		bulletImg = new Texture(Gdx.files.internal("bullet.png"));
		factoryImg = new Texture(Gdx.files.internal("factory.png"));

		engine.addSystem(new CommandSystem(camera));
		engine.addSystem(new TankAISystem());
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
				float detect = 300 + e.planet.getComponent(Radius.class).size;
				if (distanceSq(pos.x, pos.y, x, y) < detect * detect) {
					continue genLoop;
				}
			}
			// It's not close to another planet, go ahead and make it.
			Entity p = mkPlanet(x, y, 100 + ((float) Math.random() * 300));
			Entity door = mkDoor(((float) Math.random() * 360) - 180, p);

			planets.add(new PlanetSystem(p, door));
		}
		// Link the doors randomly
		for (PlanetSystem sys : planets) {
			Destination d = sys.door.getComponent(Destination.class);
			PlanetSystem dest = planets.get((int) (Math.random() * 15));
			d.planet = dest.planet;
			d.r = dest.door.getComponent(Rotation.class).r + 180;
			if (d.r > 180) {
				d.r -= 360;
			}
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

		camera.update();

		user.money += user.factories;
		computer.money += computer.factories;

		// Update
		accum += Gdx.graphics.getDeltaTime();
		// engine.update(Gdx.graphics.getDeltaTime());
		engine.update(accum);
		hud.begin();
		font.draw(hud, String.format("$%,d", user.money), 0, font.getCapHeight() - font.getDescent());
		hud.end();

	}

	public static Entity mkPlanet(float x, float y, float size) {
		Entity e = new Entity();
		e.add(new Position(x, y));
		e.add(new Radius(size));
		e.add(new Ownership(0));
		e.add(new FactoryCount());
		return e;
	}

	public static Entity mkImmobileObj(float pos, Entity planet) {
		Entity e = new Entity();
		e.add(new Rotation(pos));
		e.add(new Surface(planet));
		float rad = (float) (Math.PI / 180) * pos;
		float size = planet.getComponent(Radius.class).size;
		Position c = planet.getComponent(Position.class);
		e.add(new Center((float) (Math.cos(rad) * size + c.x), (float) (Math.sin(rad) * size + c.y)));
		return e;
	}

	public static Entity mkFactory(float pos, Entity planet, float ownership) {
		Entity e = mkImmobileObj(pos, planet);
		Ownership own = new Ownership(1);
		e.add(own);
		Sprite s = new Sprite(factoryImg);
		s.setOrigin(16, 0);
		s.setColor(own.getTint());
		e.add(new SpriteComponent(s));
		e.add(new FactoryID());
		return e;
	}

	public static Entity mkDoor(float pos, Entity planet) {
		Entity e = mkImmobileObj(pos, planet);
		Sprite s = new Sprite(doorImg);
		s.setOrigin(16, 0);
		e.add(new SpriteComponent(s));
		e.add(new Destination(0, null));
		e.add(new DoorID());
		return e;
	}

	public static Entity mkMobileObj(float pos, Entity planet,boolean dir) {
		Entity e = new Entity();
		e.add(new Rotation(pos));
		e.add(new Surface(planet));
		e.add(new Direction(dir));
		return e;
	}

	public static Entity mkTank(float pos, Entity planet, float owner) {
		Entity e = mkMobileObj(pos, planet,true);
		Ownership own = new Ownership(owner);
		Sprite s = new Sprite(tankImg);
		s.setOrigin(16, 0);
		s.setColor(own.getTint());
		e.add(new SpriteComponent(s));
		if (own.isUser())
			e.add(new Selection());
		e.add(own);
		e.add(new Destination(pos, planet));
		e.add(new TankID());
		e.add(new Speed());
		e.add(new BulletCooldown());
		return e;
	}

	public static Entity mkBullet(float pos, Entity planet, float owner, boolean right) {
		Entity e =  mkMobileObj(pos, planet,right);
		Ownership own = new Ownership(owner);
		Sprite s = new Sprite(bulletImg);
		s.setOriginCenter();
		s.setColor(own.getTint());
		e.add(new SpriteComponent(s));
		e.add(own);
		e.add(new Height(8));
		e.add(new BulletID());
		return e;
	}

	public static float distanceSq(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
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
		if (camera.zoom > 32f)
			camera.zoom = 32f;
		return true;
	}
}
