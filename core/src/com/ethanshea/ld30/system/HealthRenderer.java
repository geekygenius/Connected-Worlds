package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.ethanshea.ld30.component.Health;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Surface;

public class HealthRenderer extends IteratingSystem{
	Camera cam;
	ShapeRenderer render = new ShapeRenderer();
	
	public HealthRenderer(Camera cam) {
		super(Family.getFamilyFor(Health.class,Surface.class,Rotation.class));
		this.cam = cam;
	}
	
	public void update(float delta){
		render.setProjectionMatrix(cam.combined);
		super.update(delta);
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Health health = entity.getComponent(Health.class);
		if (health.health==health.max) return;
		
		//Project into graphical space
		Position c = entity.getComponent(Surface.class).surface.getComponent(Position.class);

		render.begin(ShapeType.Filled);
		render.identity();
		render.translate(c.x, c.y, 0);
		render.rotate(0, 0, 1, entity.getComponent(Rotation.class).r);
		float percent = health.health / (float) health.max;
		percent*=.8f;
		render.setColor(1-percent, percent, .3f, 1);
		render.rect(entity.getComponent(Surface.class).surface.getComponent(Radius.class).size - 8,-20, 3, 40);
		render.end();
	}
}
