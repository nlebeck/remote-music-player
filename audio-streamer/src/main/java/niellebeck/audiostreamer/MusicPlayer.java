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
	
    private class PlayerRunnable implements Runnable {

		@Override
		public void run() {
			File file = new File(BASE_DIR + "\\test.mp3");
			Media media = new Media(file.toURI().toString());
			MediaPlayer player = new MediaPlayer(media);
			System.out.println(player.getStatus());
			player.play();
			
			while (true) {
				System.out.println("Hello there");
				System.out.println(player.getStatus());
				try {
					Thread.sleep(1000);
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
			response.getWriter().println("<h1>Files</h1>");
			
			File baseDir = new File(BASE_DIR);
			String[] files = baseDir.list();
			for (String file : files) {
				response.getWriter().println(file);
				response.getWriter().println("<p>");
			}
			
			response.getWriter().println("<a href=\"dummy_link\">test link</a>");
		}
    	
    }
    
    private Server jettyServer;
	
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
