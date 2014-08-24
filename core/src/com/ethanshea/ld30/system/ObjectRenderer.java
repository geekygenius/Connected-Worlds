package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ethanshea.ld30.component.Direction;
import com.ethanshea.ld30.component.Height;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.SpriteComponent;
import com.ethanshea.ld30.component.Surface;

public class ObjectRenderer extends IteratingSystem {
	Camera cam;
	SpriteBatch batch;

	public ObjectRenderer(Camera cam, SpriteBatch batch) {
		super(Family.getFamilyFor(Rotation.class, Surface.class,
				SpriteComponent.class));
		this.cam = cam;
		this.batch = batch;
	}

	public void update(float deltaTime) {
		batch.setProjectionMatrix(cam.combined);
		batch.begin();
		super.update(deltaTime);
		batch.end();
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Sprite s = entity.getComponent(SpriteComponent.class).sprite;
		float deg = entity.getComponent(Rotation.class).r;
		s.setRotation(deg - 90);
		float rad = ((float) Math.PI / 180) * deg;
		
		if (entity.hasComponent(Direction.class)){
				s.setFlip(entity.getComponent(Direction.class).right, false);
		}
		
		Entity surface = entity.getComponent(Surface.class).surface;
		float size = surface.getComponent(Radius.class).size;
		Position c = surface.getComponent(Position.class);
		
		if (entity.hasComponent(Height.class)){
			size += entity.getComponent(Height.class).height;
		}
		size -= 1;
		
		float sx = (float) Math.cos(rad);
		float sy = (float) Math.sin(rad);
		s.setPosition(sx * size + c.x - s.getWidth() / 2, sy * size + c.y);
		s.draw(batch);
	}
}
