package com.zereb.Fighting.server;

import com.zereb.Fighting.net.GameServer;

import java.io.IOException;

/** Launches the server application. */
public class ServerLauncher {
	public static void main(String[] args) {
		try {
			new GameServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO Implement server application.
	}
}