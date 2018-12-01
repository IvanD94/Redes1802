package udpMicStreaming;

import java.io.*;
import java.net.*;

import javax.sound.sampled.*;

public class AudioUDPClient {

	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;

	public AudioUDPClient() {
		initiateAudio();
	}

	private void playAudio() {
		byte[] buffer = new byte[10000];
		try {
			int count;
			while ((count = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
				if (count > 0) {
					sourceDataLine.write(buffer, 0, count);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initiateAudio() {

		try (DatagramSocket socket = new DatagramSocket(9786)) {

			byte[] audioBuffer = new byte[10000];

			while (true) {

				DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
				socket.receive(packet);

				try {

					byte audioData[] = packet.getData();
					InputStream byteInputStream = new ByteArrayInputStream(audioData);
					AudioFormat audioFormat = AudioFormatInstance.getAudioFormat();
					audioInputStream = new AudioInputStream(byteInputStream, audioFormat,
							audioData.length / audioFormat.getFrameSize());
					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
					sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					sourceDataLine.open(audioFormat);
					sourceDataLine.start();
					playAudio();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new AudioUDPClient();
	}

}
