package udpMicStreaming;

import javax.sound.sampled.AudioFormat;

public class AudioFormatInstance {
	
	static public AudioFormat getAudioFormat() {

		float sampleRate = 8000F;
		int sampleSizeInBits = 16;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = false;
		
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
		
	}
	
	public static void main(String[] args) {
		System.out.println(getAudioFormat().toString());
	}

}
