package niellebeck.remotemusicplayer.old;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/*
 * I used the Java Tutorials' "Hello World" Swing program linked here as a
 * reference:
 * https://docs.oracle.com/javase/tutorial/uiswing/start/compile.html
 */

public class Client {
	private JLabel statusLabel;
	
	private void createWindow() {
		JFrame frame = new JFrame("AudioStreamer client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		statusLabel = new JLabel("Not streaming.");
		frame.getContentPane().add(statusLabel);
		frame.setMinimumSize(new Dimension(640, 480));
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Client client = new Client();
				client.createWindow();
			}
		});
	}
}
