package niellebeck.remotemusicplayer;

import com.github.strikerx3.jxinput.XInputButtonsDelta;
import com.github.strikerx3.jxinput.XInputComponentsDelta;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/**
 * A class encapsulating the logic to control the music player from the local
 * computer using an XInput device.
 * <p>
 * This class uses the JXInput library (https://github.com/StrikerX3/JXInput)
 * to read XInput input. I referenced the JXInput README when writing the
 * JXInput code.
 */
public class LocalBrowser {

	private enum SelectedArea {
		DIRS, SONGS
	}
	
	private static final int LEFT_COLUMN_WIDTH = 480;
	private static final int HEADER_FONT_SIZE = 32;
	private static final int NORMAL_FONT_SIZE = 16;
	
	private static final int NUM_VISIBLE_DIR_ITEMS = 14;
	private static final int NUM_VISIBLE_SONG_ITEMS = 14;
	
	private Scene scene;
	private Controller controller;
	private XInputDevice device;
	private Thread thread;
	private volatile boolean shouldStop;
	
	private SelectedArea selectedArea;
	private int selectedDirItem;
	private int selectedSongItem;
	
	private int minVisibleDirItem;
	private int minVisibleSongItem;
	
	private String cachedCurrentRelativeDir;
	
	public LocalBrowser(Scene scene, Controller controller) {
		this.scene = scene;
		this.controller = controller;
		this.device = initDevice();
		shouldStop = false;
		
		selectedArea = SelectedArea.DIRS;
		selectedDirItem = 0;
		selectedSongItem = 0;
		
		minVisibleDirItem = 0;
		minVisibleSongItem = 0;
		
		cachedCurrentRelativeDir = "";
	}
	
	public XInputDevice initDevice() {
		try {
			XInputDevice[] devices = XInputDevice.getAllDevices();
			if (devices.length >= 1) {
				return devices[0];
			}
		} catch (XInputNotLoadedException e) {
			System.err.println("Error initializing XInput device");
		}
		return null;
	}
	
	public void start() {
		thread = new Thread() {
			@Override
			public void run() {
				/*
				 * I referenced this page when figuring out how to stop the
				 * thread: https://docs.oracle.com/javase/8/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html.
				 */
				while(!shouldStop) {
					update();
					try {
						Thread.sleep(33);
					} catch (InterruptedException e) {
						System.err.println("Error sleeping.");
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}
	
	public void stop() {
		shouldStop = true;
	}
	
	// Called on the LocalBrowser's background thread
	public void update() {
		checkForDirectoryChange();
		
		if (device != null && device.poll()) {
			XInputComponentsDelta delta = device.getDelta();
			XInputButtonsDelta buttonsDelta = delta.getButtons();
			if (buttonsDelta.isPressed(XInputButton.A)) {
				handleA();
			}
			else if (buttonsDelta.isPressed(XInputButton.X)) {
				handleX();
			}
			else if (buttonsDelta.isPressed(XInputButton.Y)) {
				handleY();
			}
			else if (buttonsDelta.isPressed(XInputButton.DPAD_UP)) {
				handleUp();
			}
			else if (buttonsDelta.isPressed(XInputButton.DPAD_DOWN)) {
				handleDown();
			}
			else if (buttonsDelta.isPressed(XInputButton.DPAD_LEFT)) {
				handleLeft();
			}
			else if (buttonsDelta.isPressed(XInputButton.DPAD_RIGHT)) {
				handleRight();
			}
			else if (buttonsDelta.isPressed(XInputButton.START)) {
				handleStart();
			}
			else if (buttonsDelta.isPressed(XInputButton.LEFT_SHOULDER)) {
				handleLeftShoulder();
			}
			else if (buttonsDelta.isPressed(XInputButton.RIGHT_SHOULDER)) {
				handleRightShoulder();
			}
		}
		
		/*
		 * This page taught me how to schedule code to run on the JavaFX
		 * Application Thread:
		 * https://noblecodemonkeys.com/switching-to-the-gui-thread-in-javafx/.
		 */
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				drawDisplay(controller.getStateSnapshot());
			}
		});
	}
	
	private void checkForDirectoryChange() {
		if (controller.getCurrentRelativeDir() != cachedCurrentRelativeDir) {
			resetSelection();
		}
		cachedCurrentRelativeDir = controller.getCurrentRelativeDir();
	}
	
	// Called on the JavaFX Application Thread
	private void drawDisplay(Controller.ControllerState snapshot) {
		GridPane gridPane = new GridPane();
		gridPane.getColumnConstraints().add(new ColumnConstraints(LEFT_COLUMN_WIDTH));
		
		int currentRow = 0;

		gridPane.add(createHeaderLabel("Status"), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel(snapshot.paused ? "Paused" : "Unpaused"), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Volume: " + String.format("%.2f", snapshot.volume)), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Current directory: " + snapshot.currentRelativeDir), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Current song: " + snapshot.currentSong), 0, currentRow++, 2, 1);
		
		int twoColumnStartRow = currentRow;
		
		gridPane.add(createHeaderLabel("Navigation"), 0, currentRow++);
		
		// Start of labels in DIR area
		int maxVisibleDirItem = Math.min(minVisibleDirItem + NUM_VISIBLE_DIR_ITEMS - 1, getNumItemsInDirsAreaFromSnapshot(snapshot) - 1);
		for (int i = minVisibleDirItem; i <= maxVisibleDirItem; i++) {
			Label dirLabel = createNormalLabel(getDirItemFromSnapshot(snapshot, i));
			underlineIfSelectedDir(dirLabel, i);
			gridPane.add(dirLabel, 0, currentRow++);
		}
		// End of labels in DIR area
		
		currentRow = twoColumnStartRow;
		
		gridPane.add(createHeaderLabel("Songs"), 1, currentRow++);
		
		// Start of labels in SONGS area
		int maxVisibleSongItem = Math.min(minVisibleSongItem + NUM_VISIBLE_SONG_ITEMS - 1, getNumItemsInSongsAreaFromSnapshot(snapshot) - 1);
		for (int i = minVisibleSongItem; i <= maxVisibleSongItem; i++) {
			Label songLabel = createNormalLabel(getSongItemFromSnapshot(snapshot, i));
			underlineIfSelectedSong(songLabel, i);
			gridPane.add(songLabel, 1, currentRow++);
		}
		// End of labels in SONGS area
		
		scene.setRoot(gridPane);
	}
	
	private static Label createHeaderLabel(String text) {
		Label headerLabel = new Label(text);
		headerLabel.setFont(new Font(HEADER_FONT_SIZE));
		return headerLabel;
	}
	
	private static Label createNormalLabel(String text) {
		Label label = new Label(text);
		label.setFont(new Font(NORMAL_FONT_SIZE));
		return label;
	}
	
	private void underlineIfSelectedDir(Label label, int currentDirItem) {
		if (isSelectedDir(currentDirItem)) {
			label.setUnderline(true);
		}
	}
	
	private void underlineIfSelectedSong(Label label, int currentSongItem) {
		if (isSelectedSong(currentSongItem)) {
			label.setUnderline(true);
		}
	}
	
	private boolean isSelectedDir(int currentDirItem) {
		return selectedArea == SelectedArea.DIRS && selectedDirItem == currentDirItem;
	}
	
	private boolean isSelectedSong(int currentSongItem) {
		return selectedArea == SelectedArea.SONGS && selectedSongItem == currentSongItem;
	}
	
	private int getNumItemsInDirsArea() {
		return controller.getChildDirsInCurrentDir().size() + 1;
	}
	
	private int getNumItemsInDirsAreaFromSnapshot(Controller.ControllerState snapshot) {
		return snapshot.childDirs.size() + 1;
	}
	
	private String getDirItemFromSnapshot(Controller.ControllerState snapshot, int index) {
		if (index == 0) {
			return "Up";
		}
		else {
			return snapshot.childDirs.get(index - 1);
		}
	}
	
	private void handleDirItemAction(int index) {
		if (selectedDirItem == 0) {
			controller.navigateUp();
		}
		else {
			int selectedChildDir = selectedDirItem - 1;
			controller.navigate(controller.getChildDirsInCurrentDir().get(selectedChildDir));
		}
		
		resetSelection();
	}

	private int getNumItemsInSongsArea() {
		return controller.getSongsInCurrentDir().size();
	}
	
	private int getNumItemsInSongsAreaFromSnapshot(Controller.ControllerState snapshot) {
		return snapshot.songs.size();
	}
	
	private String getSongItemFromSnapshot(Controller.ControllerState snapshot, int index) {
		return snapshot.songs.get(index);
	}
	
	private void handleSongItemAction(int index) {
		controller.playSong(controller.getSongsInCurrentDir().get(index));
	}
	
	private void resetSelection() {
		selectedArea = SelectedArea.DIRS;
		selectedDirItem = 0;
		selectedSongItem = 0;
		
		minVisibleDirItem = 0;
		minVisibleSongItem = 0;
	}
	
	private void moveCursorDown() {
		if (selectedArea == SelectedArea.DIRS) {
			selectedDirItem++;
			if (selectedDirItem >= getNumItemsInDirsArea()) {
				selectedDirItem = getNumItemsInDirsArea() - 1;
			}
			if (selectedDirItem > minVisibleDirItem + NUM_VISIBLE_DIR_ITEMS - 1) {
				minVisibleDirItem++;
			}
		}
		else if (selectedArea == SelectedArea.SONGS) {
			selectedSongItem++;
			if (selectedSongItem >= getNumItemsInSongsArea()) {
				selectedSongItem = getNumItemsInSongsArea() - 1;
			}
			if (selectedSongItem > minVisibleSongItem + NUM_VISIBLE_SONG_ITEMS - 1) {
				minVisibleSongItem++;
			}
		}
	}
	
	private void moveCursorUp() {
		if (selectedArea == SelectedArea.DIRS) {
			selectedDirItem--;
			if (selectedDirItem < 0) {
				selectedDirItem = 0;
			}
			if (selectedDirItem < minVisibleDirItem) {
				minVisibleDirItem--;
			}
		}
		else if (selectedArea == SelectedArea.SONGS) {
			selectedSongItem--;
			if (selectedSongItem < 0) {
				selectedSongItem = 0;
			}
			if (selectedSongItem < minVisibleSongItem) {
				minVisibleSongItem--;
			}
		}
	}
	
	private void handleA() {
		if (selectedArea == SelectedArea.DIRS) {
			handleDirItemAction(selectedDirItem);
		}
		else if (selectedArea == SelectedArea.SONGS) {
			handleSongItemAction(selectedSongItem);
		}
	}
	
	private void handleX() {
		controller.decreaseVolume();
	}
	
	private void handleY() {
		controller.increaseVolume();
	}
	
	private void handleStart() {
		if (controller.songIsPaused()) {
			controller.unpauseSong();
		}
		else {
			controller.pauseSong();
		}
	}
	
	private void handleUp() {
		moveCursorUp();
	}
	
	private void handleDown() {
		moveCursorDown();
	}
	
	private void handleLeft() {
		selectedArea = SelectedArea.DIRS;
	}
	
	private void handleRight() {
		if (getNumItemsInSongsArea() > 0) {
			selectedArea = SelectedArea.SONGS;
		}
	}
	
	private void handleLeftShoulder() {
		controller.playPrevSong();
	}
	
	private void handleRightShoulder() {
		controller.playNextSong();
	}
	
}
