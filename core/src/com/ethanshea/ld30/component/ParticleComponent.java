package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;

public class ParticleComponent extends Component {
	public ParticleComponent(ParticleEffect effect2) {
		effect = effect2;
	}

	public ParticleEffect effect;
}
