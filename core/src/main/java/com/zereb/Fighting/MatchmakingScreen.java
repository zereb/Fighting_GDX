package com.zereb.Fighting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.zereb.Fighting.net.ClientController;
import com.zereb.Fighting.net.GameServer;

import java.io.IOException;

public class MatchmakingScreen implements Screen {

    private final Viewport viewport;
    private final Table table;
    private final Stage stage;
    private final Main main;

    private final Label info;
    private ClientController clientController;

    private TextButton btnHost;
    private TextButton btnClient;
    private TextButton btnBack;

    public MatchmakingScreen(Main main, boolean lan) {
        this.main = main;
        viewport = new FitViewport(Main.WIDTH, Main.HEIGHT);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        info = new Label("", ResourseManager.INSTANCE.skin());


        btnBack = new TextButton("BACK", ResourseManager.INSTANCE.skin(), "elements");

        btnBack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log(getClass().getName(), "back");
                dispose();
                if (clientController != null)
                    clientController.dispose();
                if (main.gameServer != null)
                    main.gameServer.dispose(main);

                main.setScreen(new MatchmakingScreen(main, lan));
            }
        });


        table = new Table();
        table.setFillParent(true);
        table.setBackground(new Image(ResourseManager.INSTANCE.loadTexture("bg2.png")).getDrawable());
//        table.setColor(Color.GRAY);
        table.pad(10f);
        table.row();
        table.add(info);
        table.row().pad(10);




        if (lan) createUiForLan();
        else Gdx.app.postRunnable(this::createUiForNet);


//        table.setDebug(Main.isDebug);
        table.pack();

        stage.addActor(table);


    }

    public void createUiForLan() {
        info.setText("Choose mode");

        btnClient = new TextButton("Client", ResourseManager.INSTANCE.skin(), "elements");
        btnHost = new TextButton("Host", ResourseManager.INSTANCE.skin(), "elements");


        btnHost.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                info.setText("Creating server...");
                try {
                    main.gameServer = new GameServer();
                    clientController = new ClientController(main);
                    info.setText("Created, waiting for opponent...");
                } catch (IOException e) {
                    e.printStackTrace();
                    info.setText(e + " , try again");
                    btnBack.setVisible(true);
                }
            }
        });

        btnClient.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                info.setText("Searching lan for servers...");
                try {
                    clientController = new ClientController(main);
                } catch (Exception e) {
                    info.setText("Error while connecting, trying again");
                    btnBack.setVisible(true);
                    e.printStackTrace();
                }

                info.setText("Error while connecting, try again");
            }
        });


        btnClient.pad(10);
        btnHost.pad(10);
        info.setAlignment(Align.center);
        info.setWrap(true);
        HorizontalGroup hg = new HorizontalGroup().pad(10);
        hg.space(10);
        hg.addActor(btnHost);
        hg.addActor(btnClient);

        table.add(hg);
        table.row().padTop(100);

    }

    public void createUiForNet() {
        String ip = "127.0.0.1";
//        String ip = "192.168.0.110";
//        ip = "176.115.90.112";
//        ip = "132.226.201.39";
        ip = "81.30.63.176";
        try {
            clientController = new ClientController(ip, main);
            info.setText("Connected, waiting for opponent");
        } catch (IOException e) {
            info.setText("Could not connect to the main server");

            e.printStackTrace();
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        main.batch.setProjectionMatrix(stage.getCamera().combined);
        main.batch.begin();
//        main.batch.draw(bg, Main.WIDTH / 2f - bg.getWidth() / 2f, 0);
        main.batch.end();

        stage.act(delta);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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

        stage.dispose();
    }

}
