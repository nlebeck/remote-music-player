package niellebeck.remotemusicplayer;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

/**
 * A music player that will eventually play music files selected by a web
 * interface.
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
public class MusicPlayer extends Application {

	private static final String CONFIG_FILE_NAME = "config.xml";

	private class PlayerRunnable implements Runnable {

		private Controller controller;
		
		public PlayerRunnable(Controller controller) {
			this.controller = controller;
		}
		
		@Override
		public void run() {
			MediaPlayer player = null;

			while (true) {
				String newSong = controller.checkForSongChange();
				if (newSong != null) {
					if (player != null) {
						player.stop();
					}

					File file = new File(newSong);
					Media media = new Media(file.toURI().toString());
					player = new MediaPlayer(media);
					player.play();
				}

				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class TestHandler extends AbstractHandler {

		private Controller controller;
		
		public TestHandler(Controller controller) {
			this.controller = controller;
		}
		
		@Override
		public void handle(String target,
				Request baseRequest,
				HttpServletRequest request,
				HttpServletResponse response)
						throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			System.out.println("Received request; target = " + target + ", query string = " + baseRequest.getQueryString());

			Map<String, String> queryParams = parseQueryString(baseRequest.getQueryString());
			String action = queryParams.get("action");
			if (action != null) {
				if (action.equals("navigate")) {
					String qpTarget = queryParams.get("target");
					String decodedTarget = URLDecoder.decode(qpTarget, "UTF-8");
					String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + decodedTarget;
					File targetFile = new File(targetPath);
					if (targetFile.isDirectory()) {
						currentRelativeDir = currentRelativeDir + File.separator + decodedTarget;
					}
				}
				else if (action.equals("navigateUp")) {
					if (!currentRelativeDir.equals("")) {
						File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
						String parentDir = currentDirFile.getParent();
						String parentRelativeDir = parentDir.substring(baseDir.length());
						currentRelativeDir = parentRelativeDir;
					}
				}
				else if (action.equals("play")) {
					String qpTarget = queryParams.get("target");
					String decodedTarget = URLDecoder.decode(qpTarget, "UTF-8");
					String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + decodedTarget;
					if (isMusicFile(targetPath)) {
						controller.changeSong(targetPath);
					}
				}
			}
			
			response.getWriter().println("<html>");
			response.getWriter().println("<body>");
			response.getWriter().println("<h1>Current directory: " + currentRelativeDir + "</h1>");
			
			if (!currentRelativeDir.equals("")) {
				response.getWriter().println("<a href=\"/?action=navigateUp\">Up</a>");
				response.getWriter().println("<p>");
			}
			
			File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
			String[] children = currentDirFile.list();
			for (String child : children) {
				String childPath = baseDir + File.separator + currentRelativeDir + File.separator + child;
				File childFile = new File(childPath);
				String linkedAction = null;
				if (childFile.isDirectory()) {
					linkedAction = "navigate";
				}
				else if (isMusicFile(childPath) && childFile.exists()) {
					linkedAction = "play";
				}
				if (linkedAction != null) {
					String encodedChild = URLEncoder.encode(child, "UTF-8");
					response.getWriter().println("<a href=\"/?action=" + linkedAction + "&target=" + encodedChild + "\">" + child + "</a>");
				}
				response.getWriter().println("<p>");
			}

			response.getWriter().println("</body>");
			response.getWriter().println("</html>");
		}
		
		private Map<String, String> parseQueryString(String queryString) {
			if (queryString == null) {
				return new HashMap<String, String>();
			}
			
			Map<String, String> result = new HashMap<String, String>();
			String[] split = queryString.split("&");
			for(String kvPair : split) {
				String[] kvPairSplit = kvPair.split("=");
				if (kvPairSplit.length == 2) {
					String key = kvPairSplit[0];
					String value = kvPairSplit[1];
					result.put(key, value);
				}
			}
			return result;
		}

		private boolean isMusicFile(String fileName) {
			String[] split = fileName.split("\\.");
			String fileType = split[split.length - 1];
			for (String type : musicFileTypes) {
				if (fileType.equalsIgnoreCase(type)) {
					return true;
				}
			}
			return false;
		}
	}

	private Server jettyServer;
	private Controller controller;
	
	/*
	 * Accessed by only the Jetty server
	 */
	private String currentRelativeDir = "";
	
	/*
	 * Assigned once at startup and never modified again
	 */
	private String baseDir;
	private String[] musicFileTypes;

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
		baseDir = config.getBaseDir();
		musicFileTypes = config.getMusicFileTypes();
		int port = config.getPort();
		
		controller = new Controller();

		jettyServer = new Server(port);
		jettyServer.setHandler(new TestHandler(controller));
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread playerThread = new Thread(new PlayerRunnable(controller));
		playerThread.setDaemon(true);
		playerThread.start();
	}

	@Override
	public void stop() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
