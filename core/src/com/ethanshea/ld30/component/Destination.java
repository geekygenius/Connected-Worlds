package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Entity;

public class Destination extends Rotation {
	public Destination(float r, Entity planet) {
		super(r);
		this.planet = planet;
	}

	public Entity planet; 
	public boolean arrived = true;
}
