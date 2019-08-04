package niellebeck.remotemusicplayer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;

public class CustomWebSocketServer extends WebSocketServer {

	private static final String COMMAND_PLAY = "play";
	private static final String COMMAND_NAVIGATE = "navigate";
	private static final String COMMAND_NAVIGATE_UP = "navigateUp";
	private static final String COMMAND_PAUSE = "pause";
	private static final String COMMAND_UNPAUSE = "unpause";
	private static final String COMMAND_NEXT = "next";
	private static final String COMMAND_PREV = "prev";
	
	private Controller controller;
	
	public CustomWebSocketServer(Controller controller, InetSocketAddress addr) {
		super(addr);
		this.controller = controller;
	}
	
	private String generateResponseJson() {
		Gson gson = new Gson();
		Map<String, List<String>> response = new HashMap<String, List<String>>();
		response.put("songs", controller.getSongsInCurrentDir());
		response.put("childDirs", controller.getChildDirsInCurrentDir());
		return gson.toJson(response);
	}
	
	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		System.out.println("Got message from client: " + message);
		conn.send(generateResponseJson());
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		
	}

	@Override
	public void onStart() {
		
	}

}
