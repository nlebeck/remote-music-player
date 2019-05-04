package niellebeck.audiostreamer.server;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class Server {
	public static void main(String[] args) {
		Server server = new Server();
		server.printMixerInfo();
		server.doJankyMicrophoneLoopback();
	}
	
	private void printMixerInfo() {
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : infos) {
			System.out.println(info.getName());
			Mixer mixer = AudioSystem.getMixer(info);
			Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
			System.out.println("  Source lines:");
			for (Line.Info lineInfo : sourceLineInfos) {
				System.out.println("    " + lineInfo.getLineClass());
			}
			System.out.println("  Target lines:");
			Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
			for (Line.Info lineInfo : targetLineInfos) {
				System.out.println("    " + lineInfo.getLineClass());
			}
		}
	}
	
	private Mixer getMixerByName(String name) {
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : infos) {
			if (info.getName().equals(name)) {
				Mixer mixer = AudioSystem.getMixer(info);
				return mixer;
			}
		}
		return null;
	}
	
	private TargetDataLine getSingleTargetDataLine(Mixer mixer) throws LineUnavailableException {
		Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
		if (targetLineInfos.length != 1) {
			System.err.println("Error: mixer does not have exactly one target line");
			return null;
		}
		Line line = mixer.getLine(targetLineInfos[0]);
		TargetDataLine dataLine = (TargetDataLine)line;
		return dataLine;
	}
	
	private SourceDataLine getFirstSourceDataLine(Mixer mixer) throws LineUnavailableException {
		Line.Info[] sourceLineInfos = mixer.getSourceLineInfo();
		if (sourceLineInfos.length < 1) {
			System.err.println("Error: mixer does not have at least one source line");
			return null;
		}
		Line line = mixer.getLine(sourceLineInfos[0]);
		SourceDataLine dataLine = (SourceDataLine)line;
		return dataLine;
	}
	
	private void doJankyMicrophoneLoopback () {
		final int BUF_SIZE = 1024;
		
		Mixer captureMixer = getMixerByName("Primary Sound Capture Driver");
		Mixer outputMixer = getMixerByName("Primary Sound Driver");
		
		try {
			TargetDataLine targetLine = getSingleTargetDataLine(captureMixer);
			SourceDataLine sourceLine = getFirstSourceDataLine(outputMixer);
			targetLine.open();
			sourceLine.open();
			targetLine.start();
			sourceLine.start();
			byte[] buf = new byte[BUF_SIZE];
			while (true) {
				int bytesRead = targetLine.read(buf, 0,  BUF_SIZE);
				int bytesWritten = sourceLine.write(buf, 0, BUF_SIZE);
				if (bytesRead != BUF_SIZE || bytesWritten != BUF_SIZE) {
					System.err.println("Error: bytesRead = " + bytesRead + ", bytesWritten = " + bytesWritten + ", BUF_SIZE = " + BUF_SIZE);
				}
			}
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
