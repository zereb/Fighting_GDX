package com.zereb.Fighting;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.zereb.Fighting.net.ClientController;
import com.zereb.Fighting.net.GameServer;

import java.io.IOException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {



	public SpriteBatch batch;
	public ShapeRenderer sr;
	public static int WIDTH = 480, HEIGHT = 800;
	public static boolean insideAd = false;
	public GameServer gameServer;

    @Override
	public void create() {
		batch = new SpriteBatch();
		float screenRatio = (float) Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
//		Gdx.app.log("Screen resolution", "Width = " + Gdx.graphics.getWidth() + " Height = " + Gdx.graphics.getHeight());
//		Gdx.app.log("Screen ratio", String.valueOf(screenRatio));
//		HEIGHT = (int) (WIDTH / screenRatio);
		Gdx.app.log("W / H", WIDTH + " / " + HEIGHT);

		batch = new SpriteBatch();
		sr = new ShapeRenderer();
		sr.setAutoShapeType(true);


		this.setScreen(new MatchmakingScreen(this, false));
	}

	@Override
	public void render() {
		super.render();
//		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//		batch.begin();
//		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}