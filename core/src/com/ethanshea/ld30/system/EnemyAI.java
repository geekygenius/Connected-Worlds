package com.ethanshea.ld30.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.ethanshea.ld30.component.FactoryID;
import com.ethanshea.ld30.component.Ownership;
import com.ethanshea.ld30.component.TankID;

public class EnemyAI extends EntitySystem implements EntityListener {
	public int counter;
	public int factoryCount;
	public int tankCount;

	public void update(float deltaTime) {
		counter++;
		if (counter % 10 != 0)
			return;

		// AI:
		// Maintian a ratio between tanks and factories
		// If there are too many tanks, make a factory
		// Too many factories = a tank
		// Ratio should be irrational, so it can never be met
		
		//Accomplish tasks in this order:
		//Destroy factories
		//Fight tanks
		//Build
		//Send tanks to explore through the doors
		
		//Add going on the offencive later
		
	}

	@Override
	public void entityAdded(Entity entity) {
		editCount(entity, 1);
	}

	@Override
	public void entityRemoved(Entity entity) {
		editCount(entity, -1);
	}

	public void editCount(Entity entity, int dir) {
		if (!entity.hasComponent(Ownership.class))
			return;
		if (entity.getComponent(Ownership.class).isEnemy())
			return;
		if (entity.hasComponent(TankID.class)) {
			tankCount += dir;
			return;
		}
		if (entity.hasComponent(FactoryID.class)) {
			factoryCount += dir;
			return;
		}
	}
}
