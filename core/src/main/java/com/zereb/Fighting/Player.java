package com.zereb.Fighting;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.zereb.Fighting.net.GameServer;
import com.zereb.Fighting.net.RemoteClient;

public class Player {

    public final Vector2 pos = new Vector2();
    public final Rectangle box = new Rectangle();
    public int score = 0;

    private float vel = 0f;

    public Player(){}

    public Player(float x, float y){
        pos.y = y;
        pos.x = x;
        box.setCenter(pos.x, pos.y);
        box.setWidth(GameServer.POD_WIDTH);
        box.setHeight(GameServer.POD_HEIGHT);
    }

    public void clientUpdate(){
        box.setCenter(pos.x, pos.y);
    }

    public void update(RemoteClient.InputState inputState){
        pos.x += vel * GameServer.delta;
        if (inputState == RemoteClient.InputState.IDLE) vel = 0f;
        if (inputState == RemoteClient.InputState.RIGHT) vel = 260f;
        if (inputState == RemoteClient.InputState.LEFT) vel = -260f;

        box.setCenter(pos.x, pos.y);
    }
}
