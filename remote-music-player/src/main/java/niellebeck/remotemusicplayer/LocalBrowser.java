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
	
	private static final int LEFT_COLUMN_WIDTH = 320;
	private static final int HEADER_FONT_SIZE = 20;
	private static final int NORMAL_FONT_SIZE = 12;
	
	private Scene scene;
	private Controller controller;
	private XInputDevice device;
	private Thread thread;
	private volatile boolean shouldStop;
	
	private SelectedArea selectedArea;
	private int selectedItem;
	
	public LocalBrowser(Scene scene, Controller controller) {
		this.scene = scene;
		this.controller = controller;
		this.device = initDevice();
		shouldStop = false;
		
		selectedArea = SelectedArea.DIRS;
		selectedItem = 0;
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
	
	// Called on the JavaFX Application Thread.
	public void update() {
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
				drawDisplay();
			}
		});
	}
	
	private void drawDisplay() {
		GridPane gridPane = new GridPane();
		gridPane.getColumnConstraints().add(new ColumnConstraints(LEFT_COLUMN_WIDTH));
		
		int currentRow = 0;

		gridPane.add(createHeaderLabel("Status"), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel(controller.songIsPaused() ? "Paused" : "Unpaused"), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Volume: " + String.format("%.2f", controller.getVolume())), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Current directory: " + controller.getCurrentRelativeDir()), 0, currentRow++, 2, 1);
		gridPane.add(createNormalLabel("Current song: " + controller.getCurrentSong()), 0, currentRow++, 2, 1);
		
		int twoColumnStartRow = currentRow;
		
		gridPane.add(createHeaderLabel("Navigation"), 0, currentRow++);
		
		// Start of labels in DIR area
		int currentDirItem = 0;
		Label upLabel = createNormalLabel("Up");
		underlineIfSelectedDir(upLabel, currentDirItem);
		currentDirItem++;
		gridPane.add(upLabel, 0, currentRow++);
		for (String childDir : controller.getChildDirsInCurrentDir()) {
			Label childDirLabel = createNormalLabel(childDir);
			underlineIfSelectedDir(childDirLabel, currentDirItem);
			currentDirItem++;
			gridPane.add(childDirLabel, 0, currentRow++);
		}
		// End of labels in DIR area
		
		currentRow = twoColumnStartRow;
		
		gridPane.add(createHeaderLabel("Songs"), 1, currentRow++);
		
		// Start of labels in SONGS area
		int currentSongItem = 0;
		for (String song : controller.getSongsInCurrentDir()) {
			Label songLabel = createNormalLabel(song);
			underlineIfSelectedSong(songLabel, currentSongItem);
			currentSongItem++;
			gridPane.add(songLabel, 1, currentRow++);
		}
		// End of labels in SONGS area
		
		scene.setRoot(gridPane);
	}
	
	private Label createHeaderLabel(String text) {
		Label headerLabel = new Label(text);
		headerLabel.setFont(new Font(HEADER_FONT_SIZE));
		return headerLabel;
	}
	
	private Label createNormalLabel(String text) {
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
		return selectedArea == SelectedArea.DIRS && selectedItem == currentDirItem;
	}
	
	private boolean isSelectedSong(int currentSongItem) {
		return selectedArea == SelectedArea.SONGS && selectedItem == currentSongItem;
	}
	
	private int getNumItemsInDirsArea() {
		return controller.getChildDirsInCurrentDir().size() + 1;
	}
	
	private int getNumItemsInSongsArea() {
		return controller.getSongsInCurrentDir().size();
	}
	
	private void moveCursorDown() {
		selectedItem++;
		if (selectedArea == SelectedArea.DIRS && selectedItem >= getNumItemsInDirsArea()) {
			selectedItem = getNumItemsInDirsArea() - 1;
		}
		else if (selectedArea == SelectedArea.SONGS && selectedItem >= getNumItemsInSongsArea()) {
			selectedItem = getNumItemsInSongsArea() - 1;
		}
	}
	
	private void moveCursorUp() {
		selectedItem--;
		if (selectedItem < 0) {
			selectedItem = 0;
		}
	}
	
	private void handleA() {
		if (selectedArea == SelectedArea.DIRS) {
			if (selectedItem == 0) {
				controller.navigateUp();
			}
			else {
				int selectedChildDir = selectedItem - 1;
				controller.navigate(controller.getChildDirsInCurrentDir().get(selectedChildDir));
			}
			
			selectedArea = SelectedArea.DIRS;
			selectedItem = 0;
		}
		else if (selectedArea == SelectedArea.SONGS) {
			controller.playSong(controller.getSongsInCurrentDir().get(selectedItem));
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
		controller.playPrevSong();
	}
	
	private void handleRight() {
		controller.playNextSong();
	}
	
	private void handleLeftShoulder() {
		selectedArea = SelectedArea.DIRS;
		selectedItem = 0;
	}
	
	private void handleRightShoulder() {
		if (getNumItemsInSongsArea() > 0) {
			selectedArea = SelectedArea.SONGS;
			selectedItem = 0;
		}
	}
	
}
