package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class Surface extends Component {

	public Surface(Entity sur) {
		surface = sur;
	}
	public Entity surface; 
}
