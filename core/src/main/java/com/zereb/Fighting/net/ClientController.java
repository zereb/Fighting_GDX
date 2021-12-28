package com.zereb.Fighting.net;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.zereb.Fighting.GameScreen;
import com.zereb.Fighting.Main;
import com.zereb.Fighting.net.packets.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ClosedSelectorException;

public class ClientController {

    public Client client;
    private final Main main;
    private GameScreen gameScreen;
    public static final String version = "1.0";

    public ClientController(String ip, Main main) throws IOException {
        this.main = main;
        client = new Client();
        client.start();
        Kryo kryo = client.getKryo();
        kryo.setRegistrationRequired(false);
        client.connect(5000, ip, 54555, 54777);

        setupController();
    }

    public ClientController(Main main) throws IOException {
        this.main = main;
        client = new Client();
        client.start();
        InetAddress address = client.discoverHost(54777, 5000);
        Kryo kryo = client.getKryo();
        kryo.setRegistrationRequired(false);

        client.connect(5000, address, 54555, 54777);

        setupController();
    }


    public void setupController() {
//        client.sendTCP(new RegisterName(Save.INSTANCE.name));

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
//                System.out.println(object);
            }
        });
        Listener.TypeListener typeListener = new Listener.TypeListener();

        typeListener.addTypeHandler(GameSetup.class, ((connection, gameSetup) -> {
            Gdx.app.log("Game setup", gameSetup.toString());
            Gdx.app.postRunnable(() -> {
                gameScreen = new GameScreen(main, this, gameSetup);
                main.setScreen(gameScreen);
            });
        }));

        typeListener.addTypeHandler(GameInstanceSnapshot.class, (connection, snapshot) -> {
            gameScreen.parse(snapshot);
        });

        typeListener.addTypeHandler(GameEndDisconnect.class, ((connection, s) -> {
//            Gdx.app.postRunnable(() -> gameScreen.opponentDisconnected());
        }));

        typeListener.addTypeHandler(Error.class, ((connection, error) -> {
            error.printStackTrace();
        }));


        client.addListener(typeListener);
    }


    public void dispose() {
        try {
            client.close();
            client = null;
        } catch (ClosedSelectorException e) {
            e.printStackTrace();
        }
    }

}
