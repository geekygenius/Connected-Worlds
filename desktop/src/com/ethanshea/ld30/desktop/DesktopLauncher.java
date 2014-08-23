package com.ethanshea.ld30.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ethanshea.ld30.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Emergence";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new Game(), config);
	}
}
