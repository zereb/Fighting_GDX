package com.zereb.Fighting.net.packets;

import com.badlogic.gdx.math.Vector2;
import com.zereb.Fighting.Player;

import java.util.HashMap;
import java.util.Vector;

public class GameInstanceSnapshot {

    public HashMap<Integer, Player> players = new HashMap<>();
    public Vector2 ball = new Vector2();
    public boolean isScored = false;


    @Override
    public String toString() {
        return "GameInstanceSnapshot{" +
                "players=" + players +
                ", bal=" + ball +
                '}';
    }
}
