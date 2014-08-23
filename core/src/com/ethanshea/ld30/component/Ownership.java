package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;

public class Ownership extends Component {
	public static final Color good = new Color(0, .3f, .6f, 1);// Blue
	public static final Color evil = new Color(.6f, .3f, 0, 1);// Red
	public float ownership;// 1 is us, -1 is them
	
	public Ownership(float own){
		ownership = own;
	}

	public Color getColor() {
		return new Color(ownership < 0 ? (1 - ownership) * .15f + .3f: .3f, .3f, ownership > 0 ? ownership * .15f + .3f: .3f, 1f);
	}
	
	public Color getTint() {
		return new Color(ownership < 0 ? (1 - ownership) * .15f + .85f: .85f, .85f, ownership > 0 ? ownership * .15f + .85f: .85f, 1f);
	}

	public boolean isUser() {
		return ownership > .5f;
	}
	public boolean isEnemy() {
		return ownership < -.5f;
	}
	public boolean isUndecided() {
		return !(isUser() || isEnemy());
	}
}
