package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;

public class PlanetRenderer extends IteratingSystem {
	private ShapeRenderer renderer;
	private Camera cam;
	
	public PlanetRenderer(Camera cam) {
		super(Family.getFamilyFor(Position.class, Radius.class));
		renderer = new ShapeRenderer();
		this.cam = cam;
	}
	
	@Override
	public void processEntity(Entity entity, float deltaTime) {
		renderer.setProjectionMatrix(cam.combined);
		renderer.begin(ShapeType.Filled);
		renderer.setColor(.3f, .3f, .3f, 1);
		Position pos = entity.getComponent(Position.class);
		renderer.circle(pos.x, pos.y, entity.getComponent(Radius.class).size);
		renderer.end();
	}
}
