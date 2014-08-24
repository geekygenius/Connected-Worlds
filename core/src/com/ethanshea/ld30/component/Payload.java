package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class Payload extends Component {
	public Entity load;
	public Payload(Entity load){
		this.load = load;
	}
}
