package niellebeck.remotemusicplayer;

/**
 * Class that handles interaction between the Jetty server and the MediaPlayer
 * thread.
 */
public class Controller {

	private boolean pendingSongChange = false;
	private String currentSongPath = null;
	
	public synchronized void changeSong(String path) {
		currentSongPath = path;
		pendingSongChange = true;
	}
	
	public synchronized String checkForSongChange() {
		if (pendingSongChange) {
			pendingSongChange = false;
			return currentSongPath;
		}
		return null;
	}
}
