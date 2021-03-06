package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.ethanshea.ld30.component.Center;
import com.ethanshea.ld30.component.Position;
import com.ethanshea.ld30.component.Radius;
import com.ethanshea.ld30.component.Rotation;
import com.ethanshea.ld30.component.Selection;
import com.ethanshea.ld30.component.Surface;

public class SecectionManager extends IteratingSystem {
	Position start = new Position(0, 0);
	Position end = new Position(0, 0);
	Rectangle box = new Rectangle();
	Camera cam;
	ShapeRenderer render = new ShapeRenderer();

	public SecectionManager(Camera cam) {
		super(Family.getFamilyFor(Selection.class, Rotation.class, Surface.class));
		this.cam = cam;
	}

	public void update(float delta) {
		render.setProjectionMatrix(cam.combined);
		render.begin(ShapeType.Line);
		render.setColor(.1f, .8f, .1f, 1);
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			Vector3 pos = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
			if (Gdx.input.justTouched()) {
				start.x = pos.x;
				start.y = pos.y;
			}
			end.x = pos.x;
			end.y = pos.y;
			box.set(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.abs(end.x - start.x),
					Math.abs(end.y - start.y));
			render.rect(box.x, box.y, box.width, box.height);
		}
		super.update(delta);
		render.end();
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		Selection select = entity.getComponent(Selection.class);
		if (!(select.selected || Gdx.input.isButtonPressed(Input.Buttons.LEFT)))
			return;
		float rad = ((float) Math.PI / 180) * entity.getComponent(Rotation.class).r;
		float sx = (float) Math.cos(rad);
		float sy = (float) Math.sin(rad);
		Entity surface = entity.getComponent(Surface.class).surface;
		float size = surface.getComponent(Radius.class).size;
		Position c = surface.getComponent(Position.class);
		Position pos = new Position(sx * (size + 8) + c.x, sy * (size + 8) + c.y);

		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			select.selected = box.contains(pos.x, pos.y);
		}
		if (select.selected) {
			render.rect(pos.x - 20, pos.y - 20, 40, 40);
		}
	}
}
