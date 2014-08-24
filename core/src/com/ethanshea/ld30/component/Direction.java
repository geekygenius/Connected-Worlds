package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;

public class Direction extends Component {
	public boolean right;

	public Direction(boolean dir) {
		right = dir;
	}

	public float asFloat() {
		return right ? -1 : 1;
	}
}
