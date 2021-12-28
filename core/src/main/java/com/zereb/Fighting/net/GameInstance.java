package com.zereb.Fighting.net;

import com.badlogic.gdx.math.Vector2;
import com.zereb.Fighting.Main;
import com.zereb.Fighting.Player;
import com.zereb.Fighting.net.packets.GameInstanceSnapshot;


public class GameInstance {

    private final int gameID;
    private boolean isStrted = false;
    private final RemoteClient client1;
    private final RemoteClient client2;

    private final Vector2 ball;
    private final Vector2 ballVel;

    public GameInstance(int id, RemoteClient p1, RemoteClient p2) {
        gameID = id;
        client1 = p1;
        client2 = p2;
        ball = new Vector2(Main.WIDTH / 2f, Main.HEIGHT / 2f);
        ballVel = new Vector2(0, 0);
        client1.player = new Player(Main.WIDTH / 2f, GameServer.POD_Y); // bot
        client2.player = new Player(Main.WIDTH / 2f, Main.HEIGHT - GameServer.POD_Y); //top
    }

    public RemoteClient getOpponent(RemoteClient client) {
        if(client1 == client)
            return this.client2;

        else if(this.client2 == client)
            return client1;
        return null;
    }

    public void update(){
        if (!isStrted) return;

        GameInstanceSnapshot snapshot = new GameInstanceSnapshot();

        ballVel.clamp(0, GameServer.POD_HEIGHT * 30);

        ball.x -= ballVel.x * GameServer.delta;
        ball.y -= ballVel.y * GameServer.delta;

        if (ball.y < 0) {
            ball.x = Main.WIDTH / 2f;
            ball.y = Main.HEIGHT / 2f;
            ballVel.y = 300;
            ballVel.x = 0;
            client2.player.score++;
            snapshot.isScored = true;
        }
        if (ball.y > Main.HEIGHT){
            ball.x = Main.WIDTH / 2f;
            ball.y = Main.HEIGHT / 2f;
            ballVel.y = -300;
            ballVel.x = 0;
            client1.player.score++;
            snapshot.isScored = true;
        }

        if (ball.x < 0 || ball.x > Main.WIDTH) ballVel.x = -ballVel.x;


        client1.player.update(client1.inputState);
        client2.player.update(client2.inputState);

        if (client1.player.box.contains(ball)){
            ballVel.y = -ballVel.y;
            ballVel.x = (ball.x - client1.player.pos.x) * -4f;
        }

        if (client2.player.box.contains(ball)){
            ballVel.y = -ballVel.y;
            ballVel.x = (ball.x - client1.player.pos.x) * 4f;
        }

        snapshot.ball = ball.cpy();
        snapshot.players.put(client1.getConnection().getID(), client1.player);
        snapshot.players.put(client2.getConnection().getID(), client2.player);

        client1.connection.sendUDP(snapshot);
        client2.connection.sendUDP(snapshot);
    }

    public void start(){
        System.out.println("game: " + gameID + " started");
        ball.x = Main.WIDTH / 2f;
        ball.y = Main.HEIGHT / 2f;

        ballVel.y = -200;
        isStrted = true;
    }

    public boolean isStrted(){
        return isStrted;
    }

    public boolean containsClient(RemoteClient client) {
        return client1 == client || this.client2 == client;
    }

}
