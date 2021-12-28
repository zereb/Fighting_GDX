package com.zereb.Fighting.net;

import com.esotericsoftware.kryonet.Connection;
import com.zereb.Fighting.Player;

public class RemoteClient {

    public final Connection connection;
    private String name;
    private ClientState clientState;
    private int currentGameID;
    public Player player;
    public InputState inputState;

    public enum InputState{
        IDLE,
        LEFT,
        RIGHT
    }

    public enum ClientState {
        NAMELESS,
        IDLE,
        QUEUED,
        INGAME,
        READY
    }

    public RemoteClient(Connection c) {
        connection = c;
        clientState = ClientState.NAMELESS;
        currentGameID = -1;
    }


    public Connection getConnection() {
        return connection;
    }

    public String name() {
        return name;
    }

    public ClientState clientState() {
        return clientState;
    }

    public int getGameID() {
        return currentGameID;
    }


    public void setGameID(int id) {
        currentGameID = id;
    }

    public void setName(String nameToSet) {
        if (clientState == ClientState.NAMELESS) {
            name = nameToSet;
            clientState = ClientState.IDLE;
        } else {
            System.out.println("Tried to set a name for a client that already has one!");
        }
    }

    public void setClientState(ClientState state) {
        clientState = state;
    }

    @Override
    public String toString() {
        return "RemoteClient{" +
                "connection=" + connection +
                ", name='" + name + '\'' +
                ", clientState=" + clientState +
                ", currentGameID=" + currentGameID +
                '}';
    }

}
