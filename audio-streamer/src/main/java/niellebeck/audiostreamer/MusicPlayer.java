package niellebeck.audiostreamer;

import java.io.File;
import java.io.IOException;

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
	
	private static final String BASE_DIR = "C:\\Users\\niell\\Git\\testfolder";
	private static final String[] MUSIC_FILE_TYPES = {"mp3", "m4a"};
	
    private class PlayerRunnable implements Runnable {

		@Override
		public void run() {
			MediaPlayer player = null;
			
			while (true) {
				String newSong = checkForSongChange();
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

		@Override
		public void handle(String target,
						   Request baseRequest,
						   HttpServletRequest request,
						   HttpServletResponse response)
				throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			response.getWriter().println("<html>");
			response.getWriter().println("<body>");
			
			String currentPath = convertRelativeUrlToFilePath(target);
			File currentFile = new File (currentPath);
			System.out.println("Current path: " + currentPath);
			
			if (currentFile.isDirectory()) {
				response.getWriter().println("<h1>" + currentPath + "</h1>");
				
				String[] files = currentFile.list();
				for (String file : files) {
					String filePath = currentFile.getAbsolutePath() + File.separator + file;
					String relativeUrl = convertFilePathToRelativeUrl(filePath);
					response.getWriter().println("<a href=\"" + relativeUrl + "\">" + file + "</a>");
					response.getWriter().println("<p>");
				}
			}
			else {
				if (!currentFile.exists()) {
					response.getWriter().println("<h1>File " + currentPath + " does not exist</h1>");
				}
				else if (isMusicFile(currentPath)) {
					response.getWriter().println("<h1>Playing file " + currentPath + "</h1>");
					changeSong(currentPath);
				}
				else {
					response.getWriter().println("<h1>File " + currentPath + " is not a valid music file</h1>");
				}
			}
			
			response.getWriter().println("</body>");
			response.getWriter().println("</html>");
		}
		
		private boolean isMusicFile(String fileName) {
			String[] split = fileName.split("\\.");
			String fileType = split[split.length - 1];
			for (String type : MUSIC_FILE_TYPES) {
				if (fileType.equalsIgnoreCase(type)) {
					return true;
				}
			}
			return false;
		}
		
		private String convertRelativeUrlToFilePath(String url) {
			String[] split = url.split("/");
			StringBuilder sb = new StringBuilder();
			sb.append(BASE_DIR);
			sb.append(File.separator);
			for (int i = 0; i < split.length; i++) {
				String str = split[i];
				if (!str.isEmpty()) {
					sb.append(str);
					if (i < split.length - 1) {
						sb.append(File.separator);
					}
				}
			}
			return sb.toString();
		}
    	
		private String convertFilePathToRelativeUrl(String filePath) {
			if (filePath.indexOf(BASE_DIR) != 0) {
				System.err.println("Error: file path " + filePath + " does not begin with base directory");
				return null;
			}
			String relativePath = filePath.substring(BASE_DIR.length());
			
			String splitStr = File.separator;
			if (File.separator.equals("\\")) {
				splitStr = "\\\\";
			}
			
			String[] split = relativePath.split(splitStr);
			StringBuilder sb = new StringBuilder();
			sb.append("/");
			for (int i = 0; i < split.length; i++) {
				String str = split[i];
				if (!str.isEmpty()) {
					sb.append(str);
					if (i < split.length - 1) {
						sb.append("/");
					}
				}
			}
			return sb.toString();
		}
    }
    
    private Server jettyServer;
    private boolean pendingSongChange = false;
    private String currentSongPath = null;
    
    private synchronized void changeSong(String path) {
    	currentSongPath = path;
    	pendingSongChange = true;
    }
    
    private synchronized String checkForSongChange() {
    	if (pendingSongChange) {
    		pendingSongChange = false;
    		return currentSongPath;
    	}
    	return null;
    }
	
	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) {
		Label label = new Label("Hello world!");
		Scene scene = new Scene(new StackPane(label), 640, 480);
		stage.setScene(scene);
		stage.show();
		
		jettyServer = new Server(8080);
		jettyServer.setHandler(new TestHandler());
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Thread playerThread = new Thread(new PlayerRunnable());
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
