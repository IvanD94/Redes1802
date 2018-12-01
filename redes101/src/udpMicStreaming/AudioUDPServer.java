package udpMicStreaming;

import java.net.*;

import javax.sound.sampled.*;

public class AudioUDPServer {

	private final byte audioBuffer[] = new byte[10000];
	private TargetDataLine targetDataLine;

	public AudioUDPServer() {
		setupAudio();
		broadcastAudio();
	}

	private void broadcastAudio() {

		try (DatagramSocket socket = new DatagramSocket(8000)) {

			InetAddress inetAddress = InetAddress.getByName("127.0.0.1");

			while (true) {
				int count = targetDataLine.read(audioBuffer, 0, audioBuffer.length);
				if (count > 0) {
					DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length, inetAddress, 9786);
					socket.send(packet);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setupAudio() {

		try {

			AudioFormat audioFormat = AudioFormatInstance.getAudioFormat();
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
			targetDataLine.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new AudioUDPServer();
	}

}
