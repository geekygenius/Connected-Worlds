package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;

public class Position extends Component {
	public Position(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float x;
	public float y;
}
