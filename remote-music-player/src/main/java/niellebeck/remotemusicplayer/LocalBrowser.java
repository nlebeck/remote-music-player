package niellebeck.remotemusicplayer;

import com.github.strikerx3.jxinput.XInputButtonsDelta;
import com.github.strikerx3.jxinput.XInputComponentsDelta;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

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
		int currentRow = 0;
		
		gridPane.add(new Label("Navigation"), 0, currentRow++);
		gridPane.add(new Label("Current directory: " + controller.getCurrentRelativeDir()), 0, currentRow++);
		gridPane.add(new Label("Current song: " + controller.getCurrentSong()), 0, currentRow++);
		
		// Start of labels in DIR area
		int currentDirItem = 0;
		Label upLabel = new Label("Up");
		underlineIfSelectedDir(upLabel, currentDirItem);
		currentDirItem++;
		gridPane.add(upLabel, 0, currentRow++);
		for (String childDir : controller.getChildDirsInCurrentDir()) {
			Label childDirLabel = new Label(childDir);
			underlineIfSelectedDir(childDirLabel, currentDirItem);
			currentDirItem++;
			gridPane.add(childDirLabel, 0, currentRow++);
		}
		// End of labels in DIR area
		
		gridPane.add(new Label("Songs"), 0, currentRow++);
		
		// Start of labels in SONGS area
		int currentSongItem = 0;
		for (String song : controller.getSongsInCurrentDir()) {
			Label songLabel = new Label(song);
			underlineIfSelectedSong(songLabel, currentSongItem);
			currentSongItem++;
			gridPane.add(songLabel, 0, currentRow++);
		}
		// End of labels in SONGS area
		
		scene.setRoot(gridPane);
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
	
	private void moveCursorDown() {
		selectedItem++;
		if (selectedArea == SelectedArea.DIRS && selectedItem >= getNumItemsInDirsArea()) {
			if (controller.getSongsInCurrentDir().size() > 0) {
				selectedArea = SelectedArea.SONGS;
				selectedItem = 0;
			}
			else {
				selectedItem = getNumItemsInDirsArea() - 1;
			}
		}
		else if (selectedArea == SelectedArea.SONGS && selectedItem >= controller.getSongsInCurrentDir().size()) {
			selectedItem = controller.getSongsInCurrentDir().size() - 1;
		}
	}
	
	private void moveCursorUp() {
		selectedItem--;
		if (selectedItem < 0) {
			if (selectedArea == SelectedArea.DIRS) {
				selectedItem = 0;
			}
			else if (selectedArea == SelectedArea.SONGS) {
				selectedArea = SelectedArea.DIRS;
				selectedItem = getNumItemsInDirsArea() - 1;
			}
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
	
}
