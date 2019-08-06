package niellebeck.remotemusicplayer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;

/**
 * A WebSocket server that supports the JavaScript/WebSocket client.
 */
public class CustomWebSocketServer extends WebSocketServer {

	private class ClientMessage {
		public String command;
		public String argument;
	}
	
	private class Response {
		public List<String> songs;
		public List<String> childDirs;
		public boolean paused;
		public String currentDir;
	}
	
	private Controller controller;
	private Set<WebSocket> connections;
	
	public CustomWebSocketServer(Controller controller, InetSocketAddress addr) {
		super(addr);
		this.controller = controller;
		this.connections = new HashSet<WebSocket>();
	}
	
	private String generateResponseJson() {
		Gson gson = new Gson();
		Response response = new Response();
		response.songs = controller.getSongsInCurrentDir();
		response.childDirs = controller.getChildDirsInCurrentDir();
		response.paused = controller.songIsPaused();
		response.currentDir = controller.getCurrentRelativeDir();
		return gson.toJson(response);
	}
	
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		connections.remove(conn);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Got message from client: " + message);
		boolean needsResponse = false;
		Gson gson = new Gson();
		ClientMessage clientMessage = gson.fromJson(message, ClientMessage.class);
		switch(clientMessage.command) {
		case "play":
			controller.playSong(clientMessage.argument);
			break;
		case "navigate":
			controller.navigate(clientMessage.argument);
			needsResponse = true;
			break;
		case "navigateUp":
			controller.navigateUp();
			needsResponse = true;
			break;
		case "pause":
			controller.pauseSong();
			needsResponse = true;
			break;
		case "unpause":
			controller.unpauseSong();
			needsResponse = true;
			break;
		case "prev":
			controller.playPrevSong();
			break;
		case "next":
			controller.playNextSong();
			break;
		case "connect":
			needsResponse = true;
			break;
		}
		
		if (needsResponse) {
			for (WebSocket connection : connections) {
				connection.send(generateResponseJson());
			}
		}
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		connections.add(conn);
	}

	@Override
	public void onStart() {
		
	}

}
