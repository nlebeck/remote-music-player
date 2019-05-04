package niellebeck.audiostreamer.server;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

public class Server {
	public static void main(String[] args) {
		Server server = new Server();
		server.printMixerInfo();
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
}
