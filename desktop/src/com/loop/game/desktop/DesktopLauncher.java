package com.loop.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.loop.game.LoopGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = LoopGame.WIDTH;
		config.height = LoopGame.HEIGHT;
		config.title = LoopGame.TITLE;
		new LwjglApplication(new LoopGame(), config);
	}
}
