package niellebeck.remotemusicplayer;

import com.github.strikerx3.jxinput.XInputButtonsDelta;
import com.github.strikerx3.jxinput.XInputComponentsDelta;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;

import javafx.scene.Scene;

/**
 * A class encapsulating the logic to control the music player from the local
 * computer using an XInput device.
 * <p>
 * This class uses the JXInput library (https://github.com/StrikerX3/JXInput)
 * to read XInput input. I referenced the JXInput README when writing the
 * JXInput code.
 */
public class LocalBrowser {

	private Scene scene;
	private Controller controller;
	private XInputDevice device;
	private Thread thread;
	private volatile boolean shouldStop;
	
	public LocalBrowser(Scene scene, Controller controller) {
		this.scene = scene;
		this.controller = controller;
		this.device = initDevice();
		shouldStop = false;
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
	}
	
	private void handleA() {
		
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
		
	}
	
	private void handleDown() {
		
	}
	
	private void handleLeft() {
		controller.playPrevSong();
	}
	
	private void handleRight() {
		controller.playNextSong();
	}
	
}