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
			String newSongPath = controller.checkForSongChange();
			if (newSongPath != null) {
				if (player != null) {
					player.stop();
				}

				File file = new File(newSongPath);
				Media media = new Media(file.toURI().toString());
				player = new MediaPlayer(media);
				final MediaPlayer playerAlias = player;
				
				/*
				 * This StackOverflow answer told me how to detect when the
				 * MediaPlayer is done playing a song:
				 * https://stackoverflow.com/a/50499105.
				 * 
				 * This StackOverflow answer suggested disposing of the
				 * MediaPlayer in its OnEndOfMedia callback:
				 * https://stackoverflow.com/a/37608031.
				 */
				player.setOnEndOfMedia(new Runnable() {
					public void run() {
						controller.reportSongDone();
						playerAlias.dispose();
					}
				});
				
				player.play();
			}
			else if (controller.checkForPause()) {
				if (player != null) {
					player.pause();
				}
			}
			else if (controller.checkForUnpause()) {
				if (player != null) {
					player.play();
				}
			}
			
			if (player != null) {
				player.setVolume(controller.getVolume());
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