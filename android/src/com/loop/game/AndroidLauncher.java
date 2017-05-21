package com.loop.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.loop.game.LoopGame;
import com.loop.game.net.ClientTest;
import com.loop.game.net.LoopServer;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new LoopGame(), config);
		try
		{
			ClientTest.run(
                    LoopServer.getSSLContext(getResources().openRawResource(
                            getResources().getIdentifier("trusta","raw", getPackageName())),new char[]{'l', 'o', 'o', 'p', '2', '0', '1', '7'})
                    ,new String[0]);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
