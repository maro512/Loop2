package com.loop.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.loop.game.LoopGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		LoopGame main=new LoopGame();
		main.getClient().setServerAddress("10.0.2.2"); // Adres na maszynie wirtualnej (i tak nie działa)
		initialize(main, config);
	}
}
