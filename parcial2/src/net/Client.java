package net;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Client extends Thread {

	int caballo;
	double apuesta;
	String[] avance;
	String linea;

	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;

	public Client() {

		avance = new String[6];
		for (int i = 0; i < 6; i++) {
			avance[i] = "";
		}

		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.print("Indique el caballo al que desea apostarle: ");
			caballo = Integer.parseInt(in.readLine());
			System.out.print("Indique la cantidad de dinero a apostar: ");
			apuesta = Double.parseDouble(in.readLine());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void conectar() {

		System.out.println("Conectando con el servidor...");

		try (Socket socket = new Socket("localhost", 11234)) {

			PrintWriter sOut = new PrintWriter(socket.getOutputStream(), true);

			BufferedReader sIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			sOut.println(caballo + " " + apuesta);

			System.out.println(sIn.readLine());
			System.out.println(sIn.readLine());

			System.out.println(sIn.readLine());

			// TODO iniciar otros clientes (musica y narración)

			System.out.println(sIn.readLine());

			String lin = "";

			while (!(lin = sIn.readLine()).equals("Fin carrera")) {
				System.out.print(mostrarCarrera(lin));
			}

			System.out.print("Carrera terminada. ");

			int ganador = Integer.parseInt(sIn.readLine());

			System.out.print("El caballo ganador fue: " + ganador + ". ");

			System.out.print(ganador == caballo ? "Felicitaciones." : "Gracias por participar.");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String mostrarCarrera(String datos) {

		if (datos.equals("null")) {
			return "";
		}

		StringBuilder carrera = new StringBuilder();

		String[] valores = datos.substring(1, datos.length() - 1).split(",");

		if (linea == null) {
			iniciarPatrones();
		}

		carrera.append('\n');
		carrera.append(linea);

		for (int i = 0; i < 6; i++) {

			int longitud = Integer.parseInt(valores[i].trim());

			while (avance[i].length() < longitud) {
				avance[i] += "|";
			}

			if (i == caballo - 1) {
				if (i != 0) {
					carrera.append(linea);
				}
				carrera.append(String.format("¦<%d>¦", i + 1));
				carrera.append(String.format(" %-" + (Server.TIEMPO_OPERACION * 2 / 1000) + "s ¦", avance[i]));
				carrera.append('\n');
				if (i != 5) {
					carrera.append(linea);
				}
			} else {

				carrera.append(String.format("¦ %d ¦", i + 1));
				carrera.append(String.format(" %-" + (Server.TIEMPO_OPERACION * 2 / 1000) + "s ¦", avance[i]));
				carrera.append('\n');

			}

		}

		carrera.append(linea);

		return carrera.toString();
	}

	public void iniciarPatrones() {

		StringBuilder sb = new StringBuilder();

		sb.append("+---+");

		for (int i = 0; i < Server.TIEMPO_OPERACION / 1000 * 2 + 2; i++) {
			sb.append("-");
		}

		sb.append('+');

		sb.append('\n');

		linea = sb.toString();

	}

	@Override
	public void run() {
		conectar();
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
	
	public void iniciarTransmision() {
		HiloTransmision hT = new HiloTransmision();
		hT.start();
	}

	class HiloTransmision extends Thread {

		public void recibirTransmision() {

			try (MulticastSocket multicastSocket = new MulticastSocket(9877)) {

				InetAddress inetAddress = InetAddress.getByName("228.5.6.7");
				multicastSocket.joinGroup(inetAddress);

				byte[] audioBuffer = new byte[10000];

				while (true) {
					
					DatagramPacket packet = new DatagramPacket(audioBuffer, audioBuffer.length);
					multicastSocket.receive(packet);

					try {

						byte audioData[] = packet.getData();
						InputStream byteInputStream = new ByteArrayInputStream(audioData);
						AudioFormat audioFormat = new AudioFormat(8000, 16, 2, true, false);
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
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			recibirTransmision();
		}
		
	}

	public static void main(String[] args) {
		
		Client c = new Client();
		c.start();
		
		c.iniciarTransmision();
		
	}

}
