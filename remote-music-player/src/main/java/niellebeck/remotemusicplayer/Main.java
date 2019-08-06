package niellebeck.remotemusicplayer;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Server;
import org.java_websocket.server.WebSocketServer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * The entry point for the application.
 * <p>
 * I based the JavaFX boilerplate code on the HelloFX example linked in the
 * JavaFX documentation:
 * https://github.com/openjfx/samples/blob/master/HelloFX/Maven/hellofx/src/main/java/HelloFX.java.
 * <p>
 * I based the Jetty boilerplate code on the "Maven and Jetty" chapter of the
 * Jetty documentation:
 * https://www.eclipse.org/jetty/documentation/current/maven-and-jetty.html.
 * <p>
 * By Niel Lebeck
 */
public class Main extends Application {

	private static final String CONFIG_FILE_NAME = "config.xml";

	private WebSocketServer webSocketServer;
	private Server jettyServer;
	private Controller controller;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) {
		Label label = new Label("Hello world!");
		Scene scene = new Scene(new StackPane(label), 640, 480);
		stage.setScene(scene);
		stage.show();
		
		ConfigFileParser config = new ConfigFileParser(CONFIG_FILE_NAME);
		String baseDir = config.getBaseDir();
		String[] musicFileTypes = config.getMusicFileTypes();
		int httpPort = config.getHttpPort();
		int webSocketPort = config.getWebSocketPort();
		
		String ipAddress = null;
		try {
			ipAddress = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		controller = new Controller(baseDir, musicFileTypes);
		
		jettyServer = new Server(httpPort);
		jettyServer.setHandler(new WebSocketHttpHandler(ipAddress, webSocketPort));
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		InetSocketAddress addr = new InetSocketAddress(webSocketPort);
		webSocketServer = new CustomWebSocketServer(controller, addr);
		webSocketServer.start();

		Thread playerThread = new Thread(new PlayerRunnable(controller));
		playerThread.setDaemon(true);
		playerThread.start();
	}

	@Override
	public void stop() {
		try {
			jettyServer.stop();
			webSocketServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
