package niellebeck.remotemusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that mediates interaction between the web server and the MediaPlayer
 * thread.
 */
public class Controller {

	/*
	 * These variables are accessed from both the web server and the
	 * PlayerRunnable threads, so they are always accessed inside synchronized
	 * methods
	 */
	private boolean pendingSongChange = false;
	private String currentSongPath = null;
	
	private boolean pendingPause = false;
	private boolean pendingUnpause = false;
	
	private String currentSong = "";
	private String songRelativeDir = "";
	
	/*
	 * These variables are accessed only from the web server thread
	 */
	private String currentRelativeDir = "";
	private boolean songPaused = false;
	
	/*
	 * These variables are assigned once at startup and never modified again
	 */
	private String baseDir;
	private String[] musicFileTypes;
	
	public Controller(String baseDir, String[] musicFileTypes) {
		this.baseDir = baseDir;
		this.musicFileTypes = musicFileTypes;
	}
	
	private synchronized void changeSong(String songName, String relativeDir) {
		String songPath = baseDir + File.separator + relativeDir + File.separator + songName;
		currentSong = songName;
		songRelativeDir = relativeDir;
		currentSongPath = songPath;
		pendingSongChange = true;
		songPaused = false;
	}
	
	public synchronized String checkForSongChange() {
		if (pendingSongChange) {
			pendingSongChange = false;
			return currentSongPath;
		}
		return null;
	}
	
	public synchronized void pauseSong() {
		if (!songPaused) {
			songPaused = true;
			pendingPause = true;
		}
	}
	
	public synchronized void unpauseSong() {
		if (songPaused) {
			songPaused = false;
			pendingUnpause = true;
		}
	}
	
	public synchronized boolean checkForPause() {
		if (pendingPause) {
			pendingPause = false;
			return true;
		}
		return false;
	}
	
	public synchronized boolean checkForUnpause() {
		if (pendingUnpause) {
			pendingUnpause = false;
			return true;
		}
		return false;
	}
	
	public void reportSongDone() {
		playNextSong();
	}
	
	public synchronized void playNextSong() {
		List<String> songs = getSongsInDir(songRelativeDir);
		for (int i = 0; i < songs.size() - 1; i++) {
			if (songs.get(i).equals(currentSong)) {
				String nextSong = songs.get(i + 1);
				changeSong(nextSong, songRelativeDir);
				break;
			}
		}
	}
	
	public synchronized void playPrevSong() {
		List<String> songs = getSongsInDir(songRelativeDir);
		for (int i = 1; i < songs.size(); i++) {
			if (songs.get(i).equals(currentSong)) {
				String prevSong = songs.get(i - 1);
				changeSong(prevSong, songRelativeDir);
				break;
			}
		}
	}
	
	public void navigateUp() {
		if (!currentRelativeDir.equals("")) {
			File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
			String parentDir = currentDirFile.getParent();
			String parentRelativeDir = parentDir.substring(baseDir.length());
			currentRelativeDir = parentRelativeDir;
		}
	}
	
	public void navigate(String target) {
		String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + target;
		File targetFile = new File(targetPath);
		if (targetFile.isDirectory()) {
			currentRelativeDir = currentRelativeDir + File.separator + target;
		}
	}
	
	public void playSong(String target) {
		changeSong(target, currentRelativeDir);
	}
	
	public boolean songIsPaused() {
		return songPaused;
	}
	
	public String getCurrentRelativeDir() {
		return currentRelativeDir;
	}
	
	public List<String> getSongsInCurrentDir() {
		return getSongsInDir(currentRelativeDir);
	}
	
	private List<String> getSongsInDir(String relativeDir) {
		List<String> songs = new ArrayList<String>();
		File currentDirFile = new File(baseDir + File.separator + relativeDir);
		String[] children = currentDirFile.list();
		for (String child : children) {
			String childPath = baseDir + File.separator + relativeDir + File.separator + child;
			File childFile = new File(childPath);
			if (isMusicFile(childPath) && childFile.exists()) {
				songs.add(child);
			}
		}
		return songs;
	}
	
	public List<String> getChildDirsInCurrentDir() {
		List<String> childDirs = new ArrayList<String>();
		File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
		String[] children = currentDirFile.list();
		for (String child : children) {
			String childPath = baseDir + File.separator + currentRelativeDir + File.separator + child;
			File childFile = new File(childPath);
			if (childFile.isDirectory()) {
				childDirs.add(child);
			}
		}
		return childDirs;
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
