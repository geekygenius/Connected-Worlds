package com.ethanshea.ld30;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selectable;
import com.ethanshea.ld30.component.SpriteComponent;
import com.ethanshea.ld30.component.Surface;
import com.ethanshea.ld30.system.ObjectRenderer;
import com.ethanshea.ld30.system.PlanetRenderer;

public class Game extends ApplicationAdapter {
	SpriteBatch batch;
	Engine engine;
	Family planet;
	OrthographicCamera camera;
	Texture tankImg;

	@Override
	public void create() {
		batch = new SpriteBatch();
		engine = new Engine();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		
		tankImg = new Texture(Gdx.files.internal("tank.png"));

		engine.addSystem(new PlanetRenderer(camera));
		engine.addSystem(new ObjectRenderer(camera,batch));
		
		Entity p = mkPlanet(400, 240, 100);
		engine.addEntity(p);
		engine.addEntity(mkTank(0,p));
	}

	float accum;
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		
		//Update
		accum+=Gdx.graphics.getDeltaTime();
		//engine.update(Gdx.graphics.getDeltaTime());
		engine.update(accum);
	}

	public Entity mkPlanet(float x, float y, float size) {
		Entity e = new Entity();
		e.add(new Position(x, y));
		e.add(new Radius(size));
		return e;
	}
	
	public Entity mkTank(float pos, Entity planet) {
		Entity e = new Entity();
		Sprite s = new Sprite(tankImg);
		s.setOrigin(16, 0);
		e.add(new SpriteComponent(s));
		e.add(new Rotation(pos));
		e.add(new Surface(planet));
		e.add(new Selectable());
		return e;
	}

	public void dispose() {
		batch.dispose();
	}
}
