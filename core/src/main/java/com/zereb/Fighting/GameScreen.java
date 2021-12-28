package com.zereb.Fighting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.zereb.Fighting.net.ClientController;
import com.zereb.Fighting.net.GameServer;
import com.zereb.Fighting.net.packets.*;

public class GameScreen implements Screen {
    private final Main main;
    private final ClientController clientController;
    private final GameSetup gameSetup;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Player enemy = new Player(Main.WIDTH / 2f, Main.HEIGHT - GameServer.POD_Y);
    private final Player player = new Player(Main.WIDTH / 2f, GameServer.POD_Y);
    private final Vector2 ball = new Vector2(0, 0);

    public GameScreen(Main main, ClientController clientController, GameSetup gameSetup) {
        this.main = main;
        this.clientController = clientController;
        this.gameSetup = gameSetup;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.WIDTH, Main.HEIGHT);
        viewport = new FitViewport(Main.WIDTH, Main.HEIGHT, camera);
        if (gameSetup.invertedCamera)
            camera.zoom = camera.zoom * -1;

        int KEY_LEFT = -1;
        int KEY_RIGHT = 1;
        if (gameSetup.invertedCamera){
            KEY_RIGHT = -1;
            KEY_LEFT = 1;
        }


        int finalKEY_LEFT = KEY_LEFT;
        int finalKEY_RIGHT = KEY_RIGHT;
        Gdx.input.setInputProcessor(new InputAdapter(){

            public boolean keyDown (int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A)
                    clientController.client.sendUDP(new KeyPressed(finalKEY_LEFT));
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D)
                    clientController.client.sendUDP(new KeyPressed(finalKEY_RIGHT));

                if (keycode == Input.Keys.SPACE)
                    Gdx.app.log("ping: ", clientController.client.getReturnTripTime() + " ");
                return false;
            }

            public boolean keyUp (int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A)
                    clientController.client.sendUDP(new KeyReleased(finalKEY_LEFT));
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D)
                    clientController.client.sendUDP(new KeyReleased(finalKEY_RIGHT));


                return false;
            }


        });
    }

    @Override
    public void show() {
        clientController.client.sendUDP(new Ready());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0, 0.0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.clientUpdate();
        enemy.clientUpdate();

        main.sr.setProjectionMatrix(camera.combined);
        main.sr.begin();
        main.sr.setColor(Color.WHITE);
        main.sr.set(ShapeRenderer.ShapeType.Filled);
        main.sr.rect(player.box.x, player.box.y, player.box.width, player.box.height);
        main.sr.rect(enemy.box.x, enemy.box.y, enemy.box.width, enemy.box.height);
        main.sr.circle(ball.x, ball.y, 3);
        main.sr.end();
    }

    public void parse(GameInstanceSnapshot snapshot) {
        ball.x = snapshot.ball.x;
        ball.y = snapshot.ball.y;

        player.pos.x = snapshot.players.get(gameSetup.yourId).pos.x;
        player.pos.y = snapshot.players.get(gameSetup.yourId).pos.y;
        enemy.pos.x = snapshot.players.get(gameSetup.enemyId).pos.x;
        enemy.pos.y = snapshot.players.get(gameSetup.enemyId).pos.y;

        if (snapshot.isScored) {
            player.score = snapshot.players.get(gameSetup.yourId).score;
            enemy.score = snapshot.players.get(gameSetup.enemyId).score;

            Gdx.app.log("score", player.score + " : " + enemy.score);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

}
