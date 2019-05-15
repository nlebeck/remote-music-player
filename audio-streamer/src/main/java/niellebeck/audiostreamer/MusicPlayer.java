package niellebeck.audiostreamer;

import java.io.File;

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
 * By Niel Lebeck
 */
public class MusicPlayer extends Application {
	
    private class PlayerRunnable implements Runnable {

		@Override
		public void run() {
			File file = new File("C:\\Users\\niell\\Git\\test.mp3");
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
	
	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) {
		Label label = new Label("Hello world!");
		Scene scene = new Scene(new StackPane(label), 640, 480);
		stage.setScene(scene);
		stage.show();
		
		Thread playerThread = new Thread(new PlayerRunnable());
		playerThread.setDaemon(true);
		playerThread.start();
	}
}
