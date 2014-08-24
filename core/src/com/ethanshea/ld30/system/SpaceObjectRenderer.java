package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.SpriteComponent;

public class SpaceObjectRenderer extends IteratingSystem {
	Camera cam;
	SpriteBatch batch;

	public SpaceObjectRenderer(Camera cam, SpriteBatch batch) {
		super(Family.getFamilyFor(Position.class, Rotation.class, SpriteComponent.class));
		this.cam = cam;
		this.batch = batch;
	}

	public void update(float deltaTime) {
		batch.begin();
		super.update(deltaTime);
		batch.end();
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Sprite s = entity.getComponent(SpriteComponent.class).sprite;
		s.setRotation(entity.getComponent(Rotation.class).r);
		Position pos = entity.getComponent(Position.class);
		s.setPosition(pos.x, pos.y);
		s.draw(batch);
	}
}
