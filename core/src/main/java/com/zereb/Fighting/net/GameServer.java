package com.zereb.Fighting.net;

import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.zereb.Fighting.net.packets.*;
import com.zereb.Fighting.Main;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class GameServer {

    private final Map<Connection, RemoteClient> remoteClients = new HashMap<>();
    private final Map<Integer, GameInstance> activeGames = new HashMap<>();
//    private final Thread updateThread;
    private final Server server;
    private int gamesCreated = 0;
    private float targetDelta = 0.034f;
    private float mmTimer = 0;

    public static float POD_WIDTH = 48;
    public static float POD_HEIGHT = 15;
    public static float POD_Y = 60;
    public static float delta;


    public GameServer() throws IOException {
        server = new Server();
        server.start();
        server.bind(54555, 54777);

        Kryo kryo = server.getKryo();
        kryo.setRegistrationRequired(false);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
//                if (!(object instanceof FrameworkMessage.KeepAlive))
//                    System.out.println("Got packet: " + object.toString());

                RemoteClient remoteClient = remoteClients.get(connection);

                if (object instanceof KeyPressed){
                    int key = ((KeyPressed) object).key;

                    if (key == -1)
                        remoteClient.inputState = RemoteClient.InputState.LEFT;
                    if (key == 1)
                        remoteClient.inputState = RemoteClient.InputState.RIGHT;
                }

                if (object instanceof KeyReleased){
                    int key = ((KeyReleased) object).key;

                    if (key == -1 && remoteClient.inputState == RemoteClient.InputState.LEFT)
                        remoteClient.inputState = RemoteClient.InputState.IDLE;
                    if (key == 1 && remoteClient.inputState == RemoteClient.InputState.RIGHT)
                        remoteClient.inputState = RemoteClient.InputState.IDLE;
                }

                //set client state to READY and if both clients are ready start game
                if (object instanceof Ready){
                    remoteClient.setClientState(RemoteClient.ClientState.READY);
                    GameInstance gameInstance = activeGames.get(remoteClient.getGameID());
                    if (gameInstance.getOpponent(remoteClient).clientState() == RemoteClient.ClientState.READY){
                        gameInstance.start();
                    }
                }
            }

            @Override
            public void disconnected(Connection connection) {
                System.out.println("Client disconnected with ID: " + connection.getID());
                removeClient(connection);
            }

            @Override
            public void connected(Connection connection) {
                addClient(connection);
                System.out.println("Client connected with ID: " + connection.getID());
                remoteClients.get(connection).setName("player" + connection.getID());
                queueClientMatchmaking(connection);
            }
        });


        long sleep = (long) (1000 * targetDelta);
        long time = 0L;
        AtomicLong currentTime = new AtomicLong(System.nanoTime());


        ScheduledExecutorService updateLoop = Executors.newSingleThreadScheduledExecutor();
        updateLoop.scheduleAtFixedRate(() -> {
            long newTime = TimeUtils.nanoTime();
            long frameTime = TimeUtils.timeSinceNanos(currentTime.get());
            currentTime.set(newTime);
            GameServer.delta = frameTime / 1000000000f;

            mmTimer += targetDelta;
            if (mmTimer > 3f){
                mmTimer = 0f;
                attemptMatchmake();
            }

            activeGames.values().forEach(GameInstance::update);

        },0L, sleep, TimeUnit.MILLISECONDS);

//        updateThread = new Thread(() -> {
//            while (true) {
//
//                try {
//                    Thread.sleep(sleep);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    Thread.currentThread().interrupt();
//                    return;
//                }
//            }
//        });

//        updateThread.start();

    }

    public void addClient(Connection connection) {
        if (!remoteClients.containsKey(connection))
            remoteClients.put(connection, new RemoteClient(connection));
        else
            System.out.println("Client with connection ID: " + connection.getID() + " is already connected!");
    }


    public void removeClient(Connection connection) {
        if (remoteClients.containsKey(connection)) {

            RemoteClient clientToRemove = remoteClients.get(connection);

            if (clientToRemove.clientState() == RemoteClient.ClientState.INGAME) {
                //Check if the client being removed is in a game
                if (activeGames.containsKey(clientToRemove.getGameID()) && activeGames.get(clientToRemove.getGameID()).containsClient(clientToRemove)) {

                    //If the opponent is still connected, then send them to the main menu
                    RemoteClient opponent = activeGames.get(clientToRemove.getGameID()).getOpponent(clientToRemove);
                    if (opponent.getConnection().isConnected()) {
                        opponent.getConnection().sendTCP(new GameEndDisconnect());
                        opponent.setClientState(RemoteClient.ClientState.IDLE);
                        opponent.setGameID(-1);
                    }

                    closeGame(clientToRemove.getGameID());
                }
            }

            remoteClients.remove(connection);
        } else {
            System.out.println("Connection with ID: " + connection.getID() + " is not in client list!");
        }
    }

    public void closeGame(int id) {
        if (!activeGames.containsKey(id)) {
            System.out.println("Tried to close a game that doesn't exist! ID: " + id);
            return;
        }
        System.out.println("Closed game with ID" + id);
        activeGames.remove(id);
    }


    public void queueClientMatchmaking(Connection connection) {
        if (remoteClients.containsKey(connection)) {
            RemoteClient client = remoteClients.get(connection);

            //Only queue the client for matchmaking if they are idle
            if (client.clientState() == RemoteClient.ClientState.IDLE) {
                client.setClientState(RemoteClient.ClientState.QUEUED);
                System.out.println(client.name() + " has queued for matchmaking!");
            } else {
                System.out.println("Tried to set state to QUEUED for client " + client.name() + " with state: " + client.clientState());
            }

        } else {
            System.out.println("A remote client with the provided connection does not exist! ID: " + connection.getID());
        }
    }


    public void attemptMatchmake() {
        //Return if there aren't enough players on the server
        if (remoteClients.size() < 2)
            return;

        ArrayList<RemoteClient> players = new ArrayList<>();

        //Try and pair queued players with eachother
        for (RemoteClient client : remoteClients.values()) {
            if (client.clientState() == RemoteClient.ClientState.QUEUED) {

                players.add(client);

                if (players.size() == 2) {
                    //Create a GameSetup packet to send to each client
                    GameSetup gameSetup = new GameSetup();
                    gameSetup.gameID = gamesCreated;

                    gameSetup.yourId = players.get(0).getConnection().getID();
                    gameSetup.enemyId = players.get(1).getConnection().getID();
                    gameSetup.invertedCamera = false;
                    players.get(0).getConnection().sendTCP(gameSetup);

                    //Send the packet to the second player
                    gameSetup.yourId = players.get(1).getConnection().getID();
                    gameSetup.enemyId = players.get(0).getConnection().getID();
                    gameSetup.invertedCamera = true;
                    players.get(1).getConnection().sendTCP(gameSetup);

                    //Change the state for each player to be ingame
                    players.get(0).setClientState(RemoteClient.ClientState.INGAME);
                    players.get(1).setClientState(RemoteClient.ClientState.INGAME);

                    //Change the gameID for each player to the ID of the new game
                    players.get(0).setGameID(gamesCreated);
                    players.get(1).setGameID(gamesCreated);

                    //Create a game instance and add it to the list of all active game instances
                    GameInstance newGame = new GameInstance(gamesCreated, players.get(0), players.get(1));
                    activeGames.put(gamesCreated, newGame);

                    //Output matchup to log
                    System.out.println("Game " + gamesCreated + ": " + players.get(0).name() + " vs " + players.get(1).name());
                    gamesCreated++;

                    //Clear the players list and carry on running, so multiple games can be created each function call
                    players = new ArrayList<>();
                }
            }
        }

    }


    public void dispose(Main main) {
        try {
            server.close();
        } catch (ClosedSelectorException e) {
            e.printStackTrace();
        }
    }
}