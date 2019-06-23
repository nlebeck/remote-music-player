package niellebeck.remotemusicplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles interaction between the Jetty server and the MediaPlayer
 * thread.
 */
public class Controller {

	private boolean pendingSongChange = false;
	private String currentSongPath = null;
	
	private String currentRelativeDir = "";
	
	/*
	 * Assigned once at startup and never modified again
	 */
	private String baseDir;
	private String[] musicFileTypes;
	
	public Controller(String baseDir, String[] musicFileTypes) {
		this.baseDir = baseDir;
		this.musicFileTypes = musicFileTypes;
	}
	
	private synchronized void changeSong(String songPath) {
		currentSongPath = songPath;
		pendingSongChange = true;
	}
	
	public synchronized String checkForSongChange() {
		if (pendingSongChange) {
			pendingSongChange = false;
			return currentSongPath;
		}
		return null;
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
		String targetPath = baseDir + File.separator + currentRelativeDir + File.separator + target;
		if (isMusicFile(targetPath)) {
			changeSong(targetPath);
		}
	}
	
	public String getCurrentRelativeDir() {
		return currentRelativeDir;
	}
	
	public List<String> getSongsInCurrentDir() {
		List<String> songs = new ArrayList<String>();
		File currentDirFile = new File(baseDir + File.separator + currentRelativeDir);
		String[] children = currentDirFile.list();
		for (String child : children) {
			String childPath = baseDir + File.separator + currentRelativeDir + File.separator + child;
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
