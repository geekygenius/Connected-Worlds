package com.ethanshea.ld30.component;

import com.badlogic.ashley.core.Component;

public class Health extends Component {
	public int health = 100;
	public Health(int amt){
		health = amt;
	}
}
