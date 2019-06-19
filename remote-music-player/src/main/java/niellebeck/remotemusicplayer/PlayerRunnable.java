package niellebeck.remotemusicplayer;

import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class PlayerRunnable implements Runnable {

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