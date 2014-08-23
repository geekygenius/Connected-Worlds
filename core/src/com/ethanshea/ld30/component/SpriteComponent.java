package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteComponent extends Component {
	public Sprite sprite;
	public SpriteComponent(Sprite s){
		this.sprite = s;
	}
}
