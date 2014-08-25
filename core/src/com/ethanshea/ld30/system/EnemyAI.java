package com.ethanshea.ld30.system;

import java.util.ArrayList;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.ethanshea.ld30.Constants;
import com.ethanshea.ld30.Game;
import com.ethanshea.ld30.component.*;

public class EnemyAI extends EntitySystem implements EntityListener {
	private static final Family planetFamily = Family.getFamilyFor(Position.class, Ownership.class, Radius.class);
	private static final Family factoryFamily = Family.getFamilyFor(FactoryID.class);
	private static final Family tankFamily = Family.getFamilyFor(TankID.class, Fighting.class, Destination.class,
			Ownership.class);
	public int counter;
	public int factoryCount;
	public int tankCount;
	public Engine engine;
	public final float tankFactoryRatio = 1.25f;

	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		this.engine = engine;
	}

	public void update(float deltaTime) {
		counter++;
		if (counter % 10 != 0)
			return;

		// AI:
		// Accomplish tasks with this priority:
		// Destroy factories
		// Fight tanks

		// Add going on the offencive later

		// Build
		if ((tankCount / ((float) (factoryCount)) > tankFactoryRatio) || factoryCount == 0) {
			// Factory
			if (Game.computer.money > Constants.FACTORY_PRICE) {
				Entity planet = randomControlledPlanet();
				if (planet == null)
					return;
				if (planet.getComponent(FactoryCount.class).count < Constants.MAX_FACTORIES) {
					engine.addEntity(Game.mkFactory((float) (Game.randomAngle()), planet, -1));
					planet.getComponent(FactoryCount.class).count++;
					Game.computer.money -= Constants.FACTORY_PRICE;
				}
			}
		} else {
			// Tank
			if (Game.computer.money > Constants.TANK_PRICE) {
				Entity factory = randomControlledFactory();
				if (factory == null)
					return;
				Entity tank = Game.mkTank(factory.getComponent(Rotation.class).r,
						factory.getComponent(Surface.class).surface, -1);
				engine.addEntity(tank);
				Game.computer.money -= Constants.FACTORY_PRICE;
			}
		}

		// Move tanks around
		for (Entry<Entity> e : engine.getEntitiesFor(tankFamily)) {
			if (e.value.getComponent(Ownership.class).isEnemy()) {
				Destination d = e.value.getComponent(Destination.class);
				System.out.println(e.value.getComponent(Fighting.class).fighting);
				if (e.value.getComponent(Fighting.class).fighting) {
					d.r = e.value.getComponent(Fighting.class).target.getComponent(Rotation.class).r;
					d.arrived = false;
				}else{
					if (Math.random()<.05f){
						//Go to an enemy planet
						Entity planet = randomUserControlledPlanet();
						if (planet!=null){
							d.r = Game.randomAngle();
							d.planet = planet;
							d.arrived = false;
							continue;
						}
					}
					if (d.arrived) {
						d.r = Game.randomAngle();
						d.planet = e.value.getComponent(Surface.class).surface;
						d.arrived = false;
					}
				}
			}
		}
	}

	public Entity randomControlledPlanet() {
		ArrayList<Entity> matches = new ArrayList<Entity>();
		for (Entry<Entity> e : engine.getEntitiesFor(planetFamily)) {
			if (e.value.getComponent(Ownership.class).isEnemy()) {
				matches.add(e.value);
			}
		}
		if (matches.size() == 0)
			return null;
		return matches.get((int) (Math.random() * matches.size()));
	}
	
	public Entity randomUserControlledPlanet() {
		ArrayList<Entity> matches = new ArrayList<Entity>();
		for (Entry<Entity> e : engine.getEntitiesFor(planetFamily)) {
			if (e.value.getComponent(Ownership.class).isUser()) {
				matches.add(e.value);
			}
		}
		if (matches.size() == 0)
			return null;
		return matches.get((int) (Math.random() * matches.size()));
	}

	public Entity randomControlledFactory() {
		ArrayList<Entity> matches = new ArrayList<Entity>();
		for (Entry<Entity> e : engine.getEntitiesFor(factoryFamily)) {
			if (e.value.getComponent(Ownership.class).isEnemy()) {
				matches.add(e.value);
			}
		}
		if (matches.size() == 0)
			return null;
		return matches.get((int) (Math.random() * matches.size()));
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
		if (!entity.getComponent(Ownership.class).isEnemy())
			return;

		if (entity.hasComponent(Payload.class)) return;
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
